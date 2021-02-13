package com.minenash.action_hunger.mixin;

import com.minenash.action_hunger.config.Config;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HungerManager.class)
public class HungerManagerMixin {

    @Unique
    private int constantRegenTimer = 0;

    @Inject(method = "update", at = @At("HEAD"))
    private void constantRegen(PlayerEntity player, CallbackInfo _info) {
        constantRegenTimer++;
        if (constantRegenTimer >= 80) {
            player.heal(Config.constantRegen);
            constantRegenTimer = 0;
        }
    }

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;heal(F)V"))
    private void foodRegenMultiplier(PlayerEntity player, float healAmount) {
        player.heal(healAmount * Config.foodRegenMultiplier);
    }

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;addExhaustion(F)V"))
    private void foodRegenMultiplierExhaustion(HungerManager hungerManager, float exhaustionAmount) {
        hungerManager.addExhaustion(exhaustionAmount * Config.foodRegenMultiplier);
    }

}
