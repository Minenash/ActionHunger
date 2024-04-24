package com.minenash.action_hunger;

import com.minenash.action_hunger.config.Config;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.packet.CustomPayload;

public class ClientInitializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(FoodLevelForSprintPacket.PACKET_ID, (payload, context) ->
            Config.foodLevelForSprint = payload.foodLevel()
        );
    }
}
