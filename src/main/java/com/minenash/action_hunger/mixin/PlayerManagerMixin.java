package com.minenash.action_hunger.mixin;

import com.minenash.action_hunger.config.Config;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Redirect(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;setHealth(F)V"))
    private void setSpawnHealthAndFood(ServerPlayerEntity player, float _base) {
        player.setHealth(Config.spawnHealth);
        player.getHungerManager().setFoodLevel(Config.spawnHunger);
    }

}
