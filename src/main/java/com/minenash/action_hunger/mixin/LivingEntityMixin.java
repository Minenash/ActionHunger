package com.minenash.action_hunger.mixin;

import com.minenash.action_hunger.config.Config;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Unique private long startSleep;

    @Inject(method = "sleep", at = @At("TAIL"))
    private void getStartSleepTime(CallbackInfo info) {
        if ((Object)this instanceof PlayerEntity) {
            startSleep = this.world.getTimeOfDay() % 24000;
        }
    }

    @Inject(method = "wakeUp", at = @At("TAIL"))
    private void applyDynamicSleepExhaustion(CallbackInfo info) {
        if ((Object)this instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)(Object)this;
            float exhaustion = Math.round(Math.abs((this.world.getTimeOfDay() % 24000) - startSleep) / 1000.0F) * Config.dynamicSleepExhaustionAmount;

            if (Config.sleepExhaustionAmount < player.getHungerManager().getFoodLevel() + player.getHungerManager().getSaturationLevel() + 1)
                player.addExhaustion(exhaustion);
            else
                player.getHungerManager().setFoodLevel(2);

            startSleep = 0;
        }
    }

}
