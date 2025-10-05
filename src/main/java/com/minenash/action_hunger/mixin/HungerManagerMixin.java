package com.minenash.action_hunger.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.minenash.action_hunger.ActionHunger;
import com.minenash.action_hunger.config.Config;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Debug(export = true)
@Mixin(HungerManager.class)
public abstract class HungerManagerMixin {

    @Shadow public abstract void addExhaustion(float exhaustion);

    @Shadow private int prevFoodLevel;
    @Unique private int constantRegenTimer = 0;
    @Unique private int constantHungerTimer = 0;
    @Unique private int shieldExhaustionTimer = 0;


    @ModifyExpressionValue(
            method = "eat",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/component/type/FoodComponent;nutrition()I")
    )
    private int actionHunger$modifyFoodNutrition(int nutrition) {
        return (int) (nutrition * Config.hungerFromFoodMultiplier);
    }

    @ModifyExpressionValue(
            method = "eat",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/component/type/FoodComponent;saturation()F")
    )
    private float actionHunger$modifyFoodSaturation(float saturation) {
        return saturation * Config.saturationFromFoodMultiplier;
    }

    @WrapOperation( // no idea why this was in the original mixin
            method = "update",
            at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/HungerManager;foodLevel:I", ordinal = 0)
    )
    private int actionHunger$dontSetPrevFoodLevel(HungerManager instance, Operation<Integer> original) {
        return this.prevFoodLevel;
    }

    @Inject(
            method = "update",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"),
            cancellable = true,
            order = 999 // go before shield exhaustion
    )
    private void actionHunger$earlyExitIfInvuln(PlayerEntity player, CallbackInfo ci) {
        if (player.getAbilities().invulnerable)
            ci.cancel();
    }
    @Inject(
            method = "update",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z")
    )
    private void actionHunger$updateShieldExhaustion(PlayerEntity player, CallbackInfo ci) {

        if (isIsPlayerUsingShield(player)) {
            ++this.shieldExhaustionTimer;
            if (this.shieldExhaustionTimer >= Config.shieldExhaustionRate) {
                if (Config.debug)
                    System.out.println("Exhaustion from " + "Shield" + ": " + Config.shieldExhaustionAmount);
                this.addExhaustion(Config.shieldExhaustionAmount);
            }
        } else
            this.shieldExhaustionTimer = 0;
    }

    @Definition(id = "bl", local = @Local(type = boolean.class))
    @Expression("bl")
    @ModifyExpressionValue(
            method = "update",
            at = @At("MIXINEXTRAS:EXPRESSION"),
            slice = @Slice(to = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/HungerManager;foodTickTimer:I"))
    )
    private boolean actionHunger$constantRegen(boolean bl, PlayerEntity player) {
        if (bl && !shouldBlockRegenFromShield(player)) {
            this.constantRegenTimer++;
            if (this.constantRegenTimer >= Config.constantRegenRate * (Config.dynamicRegenOnConstantRegen ? getCurveModifier(player) : 1.0D)) {
                if (Config.debug)
                    System.out.println("Heal from " + "Const" + ": " + Config.constantRegenAmount);
                player.heal(Config.constantRegenAmount);
                this.constantRegenTimer = 0;
            }
            return true;
        }

        return false;
    }

    @ModifyConstant( // explicitly conflict with anyone else trying to do this.
            method = "update",
            constant = @Constant(intValue = 20)
    )
    private int actionHunger$replaceMinHunger(int original) {
        return Config.hyperFoodRegenMinimumHunger;
    }


    @ModifyConstant( // explicitly conflict with anyone else trying to do this.
            method = "update",
            constant = @Constant(intValue = 10)
    )
    private int actionHunger$replaceFoodTickRate(int original, PlayerEntity player) {
        return (int) (Config.hyperFoodRegenRate * (Config.dynamicRegenOnHyperFoodRegen ? getCurveModifier(player) : 1));
    }

    @ModifyArg(
            method = "update",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;heal(F)V", ordinal = 0),
            require = 1, expect = 1, allow = 1 // extra assertions to make sure this only matches once
    )
    private float actionHunger$multiplyHyperFoodRegen(float health) {
        health *= Config.hyperFoodRegenHealthMultiplier;
        if (Config.debug) System.out.println("Heal from Hyper: " + health);
        return health;
    }

    @ModifyArg(
            method = "update",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;addExhaustion(F)V", ordinal = 0),
            require = 1, expect = 1, allow = 1 // extra assertions to make sure this only matches once
    )
    private float actionHunger$multiplyHyperExhaustion(float exhaustion) {
        exhaustion *= Config.hyperFoodRegenExhaustionMultiplier;
        if (Config.debug) System.out.println("Exhaustion from " + "Hyper" + ": " + exhaustion);
        return exhaustion;
    }

    @Definition(id = "bl", local = @Local(type = boolean.class))
    @Expression("bl")
    @ModifyExpressionValue( // idk why the original mixin had this
            method = "update",
            at = @At("MIXINEXTRAS:EXPRESSION"),
            slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/HungerManager;foodTickTimer:I"))
    )
    private boolean actionHunger$removeCondition(boolean bl) {
        return true; // it is used in an AND so true is the noop value
    }

    @ModifyConstant( // explicitly conflict with anyone else trying to do this.
            method = "update",
            constant = @Constant(intValue = 18)
    )
    private int actionHunger$replaceMinSlowHealHunger(int original) {
        return Config.foodRegenMinimumHunger;
    }

    @ModifyConstant(
            method = "update",
            constant = @Constant(intValue = 80, ordinal = 0),
            require = 1, expect = 1, allow = 1 // extra assertions to make sure this only matches once.
    )
    private int actionHunger$replaceFoodRegenTicks(int original, PlayerEntity player) {
        return (int) (Config.foodRegenRate * (Config.dynamicRegenOnFoodRegen ? getCurveModifier(player) : 1.0D));
    }

    @ModifyArg( // do not conflict with anyone trying to do this, instead ignore them.
            method = "update",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;heal(F)V", ordinal = 1),
            require = 1, expect = 1, allow = 1 // extra assertions to make sure this only matches once
    )
    private float actionHunger$replaceSlowFoodHealAmount(float amount) {
        amount = Config.foodRegenHealthAmount;
        if (Config.debug)
            System.out.println("Heal from " + "Food" + ": " + amount);
        return amount;
    }

    @ModifyArg(
            method = "update",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;addExhaustion(F)V", ordinal = 1),
            require = 1, expect = 1, allow = 1 // extra assertions to make sure this only matches once.
    )
    private float actionHunger$replaceSlowFoodExhaustionAmount(float amount) {
        amount = Config.foodRegenExhaustionAmount;

        if (Config.debug)
            System.out.println("Exhaustion from " + "Food" + ": " + Config.foodRegenExhaustionAmount);
        return amount;
    }


    @Inject( // in the original mixin this comes before the starving damage check, but because that does not use exhaustion we can safely put this at the end of the mixin as it is impossible to put it before the starving check
            method = "update",
            at = @At(value = "TAIL")
    )
    private void actionHunger$updateConstantExhaustion(PlayerEntity player, CallbackInfo ci) {
        this.constantHungerTimer++;
        if (this.constantHungerTimer >= Config.constantExhaustionRate * (Config.dynamicRegenOnConstantExhaustion ? getCurveModifier(player) : 1.0D)) {
            if (Config.debug)
                System.out.println("Exhaustion from " + "Const" + ": " + Config.constantExhaustionAmount);
            this.addExhaustion(Config.constantExhaustionAmount);
            this.constantHungerTimer = 0;
        }
    }

    @ModifyConstant(
            method = "update",
            constant = @Constant(intValue = 80, ordinal = 1),
            require = 1, expect = 1, allow = 1 // extra assertions to make sure this only matches once.
    )
    private int actionHunger$replaceStarvationDamageRate(int ticks) {
        return Config.starvationDamageRate;
    }

    @ModifyArg( // do not conflict with anyone trying to do this, instead ignore them.
            method = "update",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z")
    )
    private float actionHunger$replaceStarvationDamageAmount(float amount) {
        // we could instead return amount * starvationDamageAmount, which would be the same in vanilla but if other mods modifyied this we would stack
        return Config.starvationDamageAmount;
    }

    @Unique
    private static double getCurveModifier(PlayerEntity player) {
        return ActionHunger.getCurveModifier(player.getHealth(), Config.dynamicRegenRateCurve, Config.dynamicRegenRateMultiplier);
    }


    @Unique
    private static boolean isIsPlayerUsingShield(PlayerEntity player) {
        return player.getActiveItem().getItem() == Items.SHIELD;
    }

    @Unique
    private static boolean shouldBlockRegenFromShield(PlayerEntity player) {
        return Config.disableRegenWhenUsingShield && isIsPlayerUsingShield(player);
    }


}
