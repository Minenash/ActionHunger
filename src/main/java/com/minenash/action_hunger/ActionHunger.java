package com.minenash.action_hunger;

import com.minenash.action_hunger.config.Config;
import com.minenash.action_hunger.config.HealthEffect;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;


public class ActionHunger implements ModInitializer {

	@Override
	public void onInitialize() {
		Config.init("action_hunger", "ActionHunger", Config.class);
		mapHealthEffects();

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			dispatcher.register(CommandManager.literal("action_hunger_reload").executes( context -> {
				Config.init("action_hunger", "ActionHunger", Config.class);
				mapHealthEffects();
				context.getSource().getPlayer().sendMessage(new LiteralText("§2[ActionHunger]:§a Config reload complete"), false);
				return 1;
			} ));
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {

			for (HealthEffect effect : Config.effects) {
				StatusEffect statusEffect = effect.statusEffect;
				if (statusEffect == null) continue;
				for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {

					float health = player.getHealth();
					float hunger = player.getHungerManager().getFoodLevel();
					if (health >= effect.healthLowBound && health <= effect.healthHighBound
					 && hunger >= effect.hungerLowBound && hunger <= effect.hungerHighBound) {
						float stepper = effect.useFoodInsteadOfHealthForAmplifierCurve ? hunger : health;
						double amplifierModifier = getCurveModifier(stepper, effect.amplifierCurve, effect.amplifierCurveMultiplier);
						player.addStatusEffect(new StatusEffectInstance(statusEffect, 1, (int) Math.round(amplifierModifier * effect.amplifier)));
					}

				}
			}



		});

	}

	private void mapHealthEffects() {
		for (HealthEffect effect : Config.effects)
			effect.statusEffect = Registry.STATUS_EFFECT.get(new Identifier(effect.effect));
	}

	public static double getCurveModifier(float stepper, Config.Curve curve, float multiplier) {
		double step =  (20 - stepper) * multiplier + 1;
		switch (curve) {
			case DISABLED: return 1.0D;
			case LINEAR: return step;
			case QUADRATIC: return Math.pow(step, 2);
			case EXPONENTIAL: return Math.pow(Math.E, step);
		}
		return 1.0D;
	}
}
