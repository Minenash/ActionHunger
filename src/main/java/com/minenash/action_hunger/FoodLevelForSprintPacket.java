package com.minenash.action_hunger;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record FoodLevelForSprintPacket(int foodLevel) implements CustomPayload {
    public static final CustomPayload.Id<FoodLevelForSprintPacket> PACKET_ID = new CustomPayload.Id<>(new Identifier("action_hunger", "food_level_for_sprint"));
    public static final PacketCodec<RegistryByteBuf, FoodLevelForSprintPacket> PACKET_CODEC = PacketCodecs.VAR_INT.xmap(FoodLevelForSprintPacket::new, FoodLevelForSprintPacket::foodLevel).cast();

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
