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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HungerManager.class)
public abstract class HungerManagerMixin {

    @Shadow private int foodLevel;
    @Shadow private float exhaustion;
    @Shadow private float foodSaturationLevel;
    @Shadow private int foodStarvationTimer;
    @Shadow public abstract void addExhaustion(float exhaustion);

    @Unique private int constantRegenTimer = 0;
    @Unique private int constantHungerTimer = 0;


    @Overwrite
    private void update(PlayerEntity player) {
        Difficulty difficulty = player.world.getDifficulty();
        if (exhaustion > 4.0F) {
            exhaustion -= 4.0F;
            if (foodSaturationLevel > 0.0F)
                foodSaturationLevel = Math.max(foodSaturationLevel - 1.0F, 0.0F);
            else if (difficulty != Difficulty.PEACEFUL)
                foodLevel = Math.max(foodLevel - 1, 0);
        }

        if (player.world.getGameRules().getBoolean(GameRules.NATURAL_REGENERATION))
            regen(player);

        constantHungerTimer++;
        if (constantHungerTimer >= Config.constantHungerRate) {
            addExhaustion(Config.constantHungerAmount);
            constantHungerTimer = 0;
        }

        if (foodLevel <= 0) {
            ++foodStarvationTimer;
            if (foodStarvationTimer >= 80) {
                if (player.getHealth() > 10.0F || difficulty == Difficulty.HARD || player.getHealth() > 1.0F && difficulty == Difficulty.NORMAL)
                    player.damage(DamageSource.STARVE, 1.0F);
                foodStarvationTimer = 0;
            }
        } else {
            foodStarvationTimer = 0;
        }
    }

    private void regen(PlayerEntity player) {

        double dynamicRegenRateModifier = getDynamicRegenRateModifier(player);

        constantRegenTimer++;
        if (constantRegenTimer >= Config.constantRegenRate * dynamicRegenRateModifier) {
            player.heal(Config.constantRegenAmount);
            constantRegenTimer = 0;
        }

        if (foodSaturationLevel > 0.0F && player.canFoodHeal() && this.foodLevel >= 20) {
            ++foodStarvationTimer;
            if (foodStarvationTimer >= 10) {
                float f = Math.min(this.foodSaturationLevel, 6.0F);
                player.heal(f / 6.0F * Config.foodRegenHealthMultiplier);
                addExhaustion(f * Config.foodRegenExhaustionMultiplier);
                foodStarvationTimer = 0;
            }
        } else if (foodLevel >= 18 && player.canFoodHeal()) {
            ++foodStarvationTimer;
            if (foodStarvationTimer >= Config.foodRegenRate * dynamicRegenRateModifier) {
                player.heal(Config.foodRegenHealthMultiplier);
                addExhaustion(6.0F * Config.foodRegenExhaustionMultiplier);
                foodStarvationTimer = 0;
            }
        }
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

}
