package com.minenash.action_hunger.mixin;

import com.minenash.action_hunger.ActionHunger;
import com.minenash.action_hunger.FoodLevelForSprintPacket;
import com.minenash.action_hunger.config.Config;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.impl.networking.FabricCustomPayloadPacketCodec;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Redirect(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;setHealth(F)V"))
    private void actionHunger$setSpawnHealthAndFood(ServerPlayerEntity player, float _base) {
        player.setHealth(Config.spawnHealth);
        player.getHungerManager().setFoodLevel(Config.spawnHunger);
    }

    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    private void actionHunger$sendFoodLevelForSprint(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
        ServerPlayNetworking.send(player, new FoodLevelForSprintPacket(Config.foodLevelForSprint));
    }

}
