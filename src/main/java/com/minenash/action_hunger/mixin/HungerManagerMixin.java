package com.minenash.action_hunger.mixin;

import com.minenash.action_hunger.ActionHunger;
import com.minenash.action_hunger.config.Config;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HungerManager.class)
public abstract class HungerManagerMixin {

    @Shadow private int foodLevel;
    @Shadow private float exhaustion;
    @Shadow private float saturationLevel;
    @Shadow private int foodTickTimer;
    @Shadow public abstract void addExhaustion(float exhaustion);
    @Shadow private void addInternal(int food, float exhaustion) {}

    @Unique private int constantRegenTimer = 0;
    @Unique private int constantHungerTimer = 0;
    @Unique private int shieldExhaustionTimer = 0;


    @Redirect(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;addInternal(IF)V"))
    private void actionHunger$modifyFoodComponent(HungerManager manager, int food, float saturation) {
        addInternal(Math.round(food * Config.hungerFromFoodMultiplier), saturation * Config.saturationFromFoodMultiplier);
    }

    /**
     * @author Minenash
     * @reason Too complicated to with normal mixins
     */
    @Overwrite
    public void update(PlayerEntity player) {
        Difficulty difficulty = player.getWorld().getDifficulty();
        if (exhaustion > 4.0F) {
            exhaustion -= 4.0F;
            if (saturationLevel > 0.0F)
                saturationLevel = Math.max(saturationLevel - 1.0F, 0.0F);
            else if (difficulty != Difficulty.PEACEFUL)
                foodLevel = Math.max(foodLevel - 1, 0);
        }

        if (player.getAbilities().invulnerable)
            return;

        double dynamicRegenRateModifier = ActionHunger.getCurveModifier(player.getHealth(), Config.dynamicRegenRateCurve, Config.dynamicRegenRateMultiplier);

        boolean regened = false;

        boolean isPlayerUsingShield = player.getActiveItem().getItem() == Items.SHIELD;

        if (isPlayerUsingShield) {
            ++shieldExhaustionTimer;
            if (shieldExhaustionTimer >= Config.shieldExhaustionRate)
                exhaustion("Shield", Config.shieldExhaustionAmount);
        }
        else
            shieldExhaustionTimer = 0;

        boolean blockRegenFromShield = Config.disableRegenWhenUsingShield && isPlayerUsingShield;
        if (player.getWorld().getGameRules().getBoolean(GameRules.NATURAL_REGENERATION) && !blockRegenFromShield)
            regened = regen(player, dynamicRegenRateModifier);

        constantHungerTimer++;
        if (constantHungerTimer >= Config.constantExhaustionRate * (Config.dynamicRegenOnConstantExhaustion ? dynamicRegenRateModifier : 1.0D)) {
            exhaustion("Const", Config.constantExhaustionAmount);
            constantHungerTimer = 0;
        }

        if (foodLevel <= 0) {
            ++foodTickTimer;
            if (foodTickTimer >= Config.starvationDamageRate) {
                if (player.getHealth() > 10.0F || difficulty == Difficulty.HARD || player.getHealth() > 1.0F && difficulty == Difficulty.NORMAL)
                    player.damage(player.getDamageSources().starve(), Config.starvationDamageAmount);
                foodTickTimer = 0;
            }
        } else if (!regened){
            foodTickTimer = 0;
        }

    }

    private boolean regen(PlayerEntity player, double dynamicRegenRateModifier) {
        constantRegenTimer++;
        if (constantRegenTimer >= Config.constantRegenRate * (Config.dynamicRegenOnConstantRegen ? dynamicRegenRateModifier : 1.0D)) {
            heal(player, "Const", Config.constantRegenAmount);
            constantRegenTimer = 0;
        }

        if (saturationLevel > 0.0F && player.canFoodHeal() && foodLevel >= Config.hyperFoodRegenMinimumHunger) {
            ++foodTickTimer;
            if (foodTickTimer >= Config.hyperFoodRegenRate * (Config.dynamicRegenOnHyperFoodRegen ? dynamicRegenRateModifier : 1.0D)) {
                float f = Math.min(saturationLevel, 6.0F);
                heal(player, "Hyper", f / 6.0F * Config.hyperFoodRegenHealthMultiplier);
                exhaustion("Hyper", f * Config.hyperFoodRegenExhaustionMultiplier);
                foodTickTimer = 0;
            }
        } else if (foodLevel >= Config.foodRegenMinimumHunger && player.canFoodHeal()) {
            ++foodTickTimer;
            if (foodTickTimer >= Config.foodRegenRate * (Config.dynamicRegenOnFoodRegen ? dynamicRegenRateModifier : 1.0D)) {
                heal(player, "Food", Config.foodRegenHealthAmount);
                exhaustion("Food", Config.foodRegenExhaustionAmount);
                foodTickTimer = 0;
            }
        }
        else
            return false;
        return true;
    }


    private void heal(PlayerEntity player, String source, float amount) {
        if (Config.debug)
            System.out.println("Heal from " + source + ": " + amount);
        player.heal(amount);
    }

    private void exhaustion(String source, float amount) {
        if (Config.debug)
            System.out.println("Exhaustion from " + source + ": " + amount);
        addExhaustion(amount);
    }

}
