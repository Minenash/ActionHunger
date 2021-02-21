package com.minenash.action_hunger;

import com.minenash.action_hunger.ActionHunger;
import com.minenash.action_hunger.config.Config;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ClientInitializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ActionHunger.SPRINT_PACKET, (client, handler, buf, responseSender) -> Config.foodLevelForSprint = buf.readInt());
    }
}
