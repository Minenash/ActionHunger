package com.minenash.action_hunger.mixin;

import com.minenash.action_hunger.config.Config;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @ModifyArg(method = "jump", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;addExhaustion(F)V", ordinal = 0), index = 0)
    private float actionHunger$changeSprintJumpExhaustionAmount(float _original) {
        return Config.sprintJumpExhaustionAmount;
    }

    @ModifyArg(method = "jump", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;addExhaustion(F)V", ordinal = 1), index = 0)
    private float actionHunger$changeJumpExhaustionAmount(float _original) {
        return Config.jumpExhaustionAmount;
    }





}
