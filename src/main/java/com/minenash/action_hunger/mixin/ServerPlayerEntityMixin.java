package com.minenash.action_hunger.mixin;

import com.minenash.action_hunger.ActionHunger;
import com.minenash.action_hunger.config.Config;
import com.minenash.action_hunger.config.HealthEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Inject(method = "wakeUp(ZZ)V", at = @At(value = "HEAD"))
    private void actionHunger$applyStaticExhaustionForSleep(CallbackInfo _info) {
        if (ActionHunger.ignoreWake) {
            ActionHunger.ignoreWake = false;
            return;
        }

        PlayerEntity p = (PlayerEntity)(Object)this;
        if (Config.sleepExhaustionAmount < p.getHungerManager().getFoodLevel() + p.getHungerManager().getSaturationLevel() + 1)
            p.addExhaustion(Config.sleepExhaustionAmount);
        else
            p.getHungerManager().setFoodLevel(2);

        for (HealthEffect effect : Config.effects)
            if (effect.onSleep)
                p.addStatusEffect(new StatusEffectInstance(RegistryEntry.of(effect.statusEffect), effect.onSleepDuration, effect.amplifier));
    }

    @Unique
    private float movementForExhaustion;

    @Inject(method = "increaseTravelMotionStats", at = @At("HEAD"))
    private void actionHunger$setMovementForExhaustion(double dx, double dy, double dz, CallbackInfo info) {
        movementForExhaustion = Math.round(MathHelper.sqrt((float) (dx * dx + dy * dy + dz * dz)) * 100.0F) * 0.01F;
    }

    @ModifyArg(method = "increaseTravelMotionStats", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/server/network/ServerPlayerEntity;addExhaustion(F)V"), index = 0)
    private float actionHunger$changeSwimExhaustionAmount(float _original) {
        return movementForExhaustion * Config.swimmingExhaustionMultiplier;
    }

    @ModifyArg(method = "increaseTravelMotionStats", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/server/network/ServerPlayerEntity;addExhaustion(F)V"), index = 0)
    private float actionHunger$changeWalkUnderWaterExhaustionAmount(float _original) {
        return movementForExhaustion * Config.walkingUnderwaterExhaustionMultiplier;
    }

    @ModifyArg(method = "increaseTravelMotionStats", at = @At(value = "INVOKE", ordinal = 2, target = "Lnet/minecraft/server/network/ServerPlayerEntity;addExhaustion(F)V"), index = 0)
    private float actionHunger$changeWalkOnWaterExhaustionAmount(float _original) {
        return movementForExhaustion * Config.walkingOnWaterExhaustionMultiplier;
    }

    @ModifyArg(method = "increaseTravelMotionStats", at = @At(value = "INVOKE", ordinal = 3, target = "Lnet/minecraft/server/network/ServerPlayerEntity;addExhaustion(F)V"), index = 0)
    private float actionHunger$changeSprintExhaustionAmount(float _original) {
        return movementForExhaustion * Config.sprintingExhaustionMultiplier;
    }

    @ModifyArg(method = "increaseTravelMotionStats", at = @At(value = "INVOKE", ordinal = 4, target = "Lnet/minecraft/server/network/ServerPlayerEntity;addExhaustion(F)V"), index = 0)
    private float actionHunger$changeCrouchExhaustionAmount(float _original) {
        return movementForExhaustion * Config.crouchingExhaustionMultiplier;
    }

    @ModifyArg(method = "increaseTravelMotionStats", at = @At(value = "INVOKE", ordinal = 5, target = "Lnet/minecraft/server/network/ServerPlayerEntity;addExhaustion(F)V"), index = 0)
    private float actionHunger$changeWalkExhaustionAmount(float _original) {
        return movementForExhaustion * Config.walkingExhaustionMultiplier;
    }

}
