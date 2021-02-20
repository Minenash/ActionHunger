package com.minenash.action_hunger.mixin;

import com.minenash.action_hunger.ActionHunger;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Inject(method = "onClientCommand", at = @At(value = "INVOKE",target = "Lnet/minecraft/server/network/ServerPlayerEntity;wakeUp(ZZ)V", shift = At.Shift.BEFORE))
    private void test2(CallbackInfo info) {
        ActionHunger.ignoreWake = true;
    }

}
