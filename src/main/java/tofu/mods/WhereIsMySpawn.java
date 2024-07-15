package tofu.mods;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tofu.mods.command.HomeCommand;
import tofu.mods.command.WorldSpawnCommand;

public class WhereIsMySpawn implements ModInitializer {
	public static final String MOD_ID = "where-is-my-spawn";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register(HomeCommand::register);
		CommandRegistrationCallback.EVENT.register(WorldSpawnCommand::register);
	}
}