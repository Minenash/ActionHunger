package com.minenash.action_hunger.mixin;

import com.minenash.action_hunger.config.Config;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
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
    @Shadow private float foodSaturationLevel;
    @Shadow private int foodStarvationTimer;
    @Shadow public abstract void addExhaustion(float exhaustion);

    @Unique private int constantRegenTimer = 0;
    @Unique private int constantHungerTimer = 0;


    @Redirect(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;add(IF)V"))
    private void modifyFoodComponent(HungerManager manager, int food, float saturation) {
        manager.add(Math.round(food * Config.hungerFromFoodMultiplier), saturation * Config.saturationFromFoodMultiplier);
    }

    /**
     * @author Minenash
     */
    @Overwrite
    public void update(PlayerEntity player) {
        Difficulty difficulty = player.world.getDifficulty();
        if (exhaustion > 4.0F) {
            exhaustion -= 4.0F;
            if (foodSaturationLevel > 0.0F)
                foodSaturationLevel = Math.max(foodSaturationLevel - 1.0F, 0.0F);
            else if (difficulty != Difficulty.PEACEFUL)
                foodLevel = Math.max(foodLevel - 1, 0);
        }

        double dynamicRegenRateModifier = getDynamicRegenRateModifier(player);

        boolean regened = false;
        if (player.world.getGameRules().getBoolean(GameRules.NATURAL_REGENERATION))
            regened = regen(player, dynamicRegenRateModifier);

        constantHungerTimer++;
        if (constantHungerTimer >= Config.constantExhaustionRate * (Config.dynamicRegenOnConstantExhaustion ? dynamicRegenRateModifier : 1.0D)) {
            exhaustion("Const", Config.constantExhaustionAmount);
            constantHungerTimer = 0;
        }

        if (foodLevel <= 0) {
            ++foodStarvationTimer;
            if (foodStarvationTimer >= Config.starvationDamageRate) {
                if (player.getHealth() > 10.0F || difficulty == Difficulty.HARD || player.getHealth() > 1.0F && difficulty == Difficulty.NORMAL)
                    player.damage(DamageSource.STARVE, Config.starvationDamageAmount);
                foodStarvationTimer = 0;
            }
        } else if (!regened){
            foodStarvationTimer = 0;
        }

    }

    private boolean regen(PlayerEntity player, double dynamicRegenRateModifier) {
        constantRegenTimer++;
        if (constantRegenTimer >= Config.constantRegenRate * (Config.dynamicRegenOnConstantRegen ? dynamicRegenRateModifier : 1.0D)) {
            heal(player, "Const", Config.constantRegenAmount);
            constantRegenTimer = 0;
        }

        if (foodSaturationLevel > 0.0F && player.canFoodHeal() && foodLevel >= Config.hyperFoodRegenMinimumHunger) {
            ++foodStarvationTimer;
            if (foodStarvationTimer >= Config.hyperFoodRegenRate * (Config.dynamicRegenOnHyperFoodRegen ? dynamicRegenRateModifier : 1.0D)) {
                float f = Math.min(foodSaturationLevel, 6.0F);
                heal(player, "Hyper", f / 6.0F * Config.hyperFoodRegenHealthMultiplier);
                exhaustion("Hyper", f * Config.hyperFoodRegenExhaustionMultiplier);
                foodStarvationTimer = 0;
            }
        } else if (foodLevel >= Config.foodRegenMinimumHunger && player.canFoodHeal()) {
            ++foodStarvationTimer;
            if (foodStarvationTimer >= Config.foodRegenRate * (Config.dynamicRegenOnFoodRegen ? dynamicRegenRateModifier : 1.0D)) {
                heal(player, "Food", Config.foodRegenHealthAmount);
                exhaustion("Food", Config.foodRegenExhaustionAmount);
                foodStarvationTimer = 0;
            }
        }
        else
            return false;
        return true;
    }

    private double getDynamicRegenRateModifier(PlayerEntity player) {
        double step =  (20 - player.getHealth()) * Config.dynamicRegenRateMultiplier + 1;
        switch (Config.dynamicRegenRateCurve) {
            case DISABLED: return 1.0D;
            case LINEAR: return step;
            case QUADRATIC: return Math.pow(step, 2);
            case EXPONENTIAL: return Math.pow(Math.E, step);
        }
        return 1.0D;
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
