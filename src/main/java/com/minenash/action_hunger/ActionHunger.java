package com.minenash.action_hunger;

import com.minenash.action_hunger.config.Config;
import com.minenash.action_hunger.config.HealthEffect;
import com.minenash.action_hunger.mixin.LivingEntityAccessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;


public class ActionHunger implements ModInitializer {

	public static boolean ignoreWake = false;

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playS2C().register(FoodLevelForSprintPacket.PACKET_ID, FoodLevelForSprintPacket.PACKET_CODEC);

		Config.init("action_hunger", "ActionHunger", Config.class);
		mapHealthEffects();

		CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> dispatcher.register(CommandManager.literal("action_hunger_reload").executes(context -> {
            Config.init("action_hunger", "ActionHunger", Config.class);
            mapHealthEffects();
            context.getSource().getServer().getPlayerManager().sendToAll(ServerPlayNetworking.createS2CPacket(new FoodLevelForSprintPacket(Config.foodLevelForSprint)));
            context.getSource().sendMessage(Text.literal("§2[ActionHunger]:§a Config reload complete"));
            return 1;
        } )));

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (HealthEffect effect : Config.effects) {
				StatusEffect statusEffect = effect.statusEffect;
				if (statusEffect == null || effect.onSleep) continue;
				for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
					float health = player.getHealth();
					float hunger = player.getHungerManager().getFoodLevel();

					boolean inHealth = health >= effect.healthLowBound && health <= effect.healthHighBound;
					boolean inHunger = hunger >= effect.hungerLowBound && hunger <= effect.hungerHighBound;

					if (effect.requiredBounds == Config.RequiredBounds.BOTH ? inHealth && inHunger : inHealth || inHunger) {
						float stepper = effect.amplifierCurveSource == Config.AmplifierCurveSource.HUNGER ? hunger : health;
						double amplifierModifier = getCurveModifier(stepper, effect.amplifierCurve, effect.amplifierCurveMultiplier);
						player.addStatusEffect(new StatusEffectInstance(RegistryEntry.of(statusEffect), 1, (int) Math.round(amplifierModifier * effect.amplifier)));
					}

				}
			}

		});

		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> ((LivingEntityAccessor)player).setActiveItemStack(ItemStack.EMPTY));

	}

	private void mapHealthEffects() {
		for (HealthEffect effect : Config.effects)
			effect.statusEffect = Registries.STATUS_EFFECT.get(Identifier.of(effect.effect));
	}

	public static double getCurveModifier(float stepper, Config.Curve curve, float multiplier) {
		double step =  (20 - stepper) * multiplier + 1;
		return switch (curve) {
			case DISABLED -> 1.0D;
			case LINEAR -> step;
			case QUADRATIC -> Math.pow(step, 2);
			case EXPONENTIAL -> Math.pow(Math.E, step);
		};
	}
}
