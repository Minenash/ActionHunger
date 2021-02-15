package com.minenash.action_hunger.mixin.client;

import com.minenash.action_hunger.config.Config;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.HungerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;getFoodLevel()I"))
    private int canSprintBasedOnFood(HungerManager manager) {
        //20 = true, 0 = false
        return manager.getFoodLevel() >= Config.foodLevelForSprint ? 20 : 0;
    }

}
