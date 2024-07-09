package tofu.mods;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import net.minecraft.server.command.ServerCommandSource;
import tofu.mods.command.HomeCommand;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;


public class WhereIsMySpawn implements ModInitializer {
	public static final String MOD_ID = "where-is-my-spawn";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register(HomeCommand::register);
	}
}