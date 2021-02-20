package com.minenash.action_hunger.mixin;

import com.minenash.action_hunger.ActionHunger;
import com.minenash.action_hunger.config.Config;
import com.minenash.action_hunger.config.SleepEffects;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Inject(method = "wakeUp(ZZ)V", at = @At(value = "HEAD"))
    private void applyStaticExhaustionForSleep(CallbackInfo _info) {
        if (ActionHunger.ignoreWake) {
            ActionHunger.ignoreWake = false;
            return;
        }

        PlayerEntity p = (PlayerEntity)(Object)this;
        if (Config.sleepExhaustionAmount < p.getHungerManager().getFoodLevel() + p.getHungerManager().getSaturationLevel() + 1)
            p.addExhaustion(Config.sleepExhaustionAmount);
        else
            p.getHungerManager().setFoodLevel(2);

        System.out.println("HIIIISIISII");


        for (SleepEffects effect : Config.sleepEffects)
            p.addStatusEffect(new StatusEffectInstance(effect.statusEffect, effect.duration, effect.amplifier));
    }

}
