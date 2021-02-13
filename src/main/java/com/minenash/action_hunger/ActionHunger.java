package com.minenash.action_hunger;

import com.minenash.action_hunger.config.Config;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.LiteralText;

public class ActionHunger implements ModInitializer {

	@Override
	public void onInitialize() {
		Config.init("action_hunger", "ActionHunger", Config.class);

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			dispatcher.register(CommandManager.literal("action_hunger_reload").executes( context -> {
				Config.init("action_hunger", "ActionHunger", Config.class);
				context.getSource().getPlayer().sendMessage(new LiteralText("§2[ActionHunger]:§a Config reload complete"), false);
				return 1;
			} ));
		});

	}
}
