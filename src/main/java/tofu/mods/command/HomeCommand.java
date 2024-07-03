package tofu.mods.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

// TODO add set worldspawn and set home spawn
// TODO: add compatibility with other minecraft and fabric versions
// TODO: add support for other mod loaders (quilt, forge, neoforge) and plugin loaders (paper, bukkit, spigot)
// TODO: various spawn points and beds, such as having a cave bed and sleeping in home bed (events)
// TODO: update gradle properties (contact)
// TODO: add client slide (maybe?)
public class HomeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("home")
                .executes(HomeCommand::home)
                .then(CommandManager.literal("worldspawn").executes(HomeCommand::worldSpawn))
                .then(CommandManager.literal("go").executes(HomeCommand::go)));
    }

    private static int home(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        BlockPos currentPos = BlockPos.ofFloored(source.getPosition());
        BlockPos respawnPoint = source.getPlayer().getSpawnPointPosition();
        if(respawnPoint != null) {
            int blocksAway = getDistance(currentPos.getX(), currentPos.getY(), respawnPoint.getX(), respawnPoint.getY());
            Text coords = Texts.bracketed(Text.translatable("chat.coordinates", respawnPoint.getX(), respawnPoint.getY(), respawnPoint.getZ())).styled((style) -> style.withColor(Formatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp " + respawnPoint.getX() + " " + respawnPoint.getY() + " " + respawnPoint.getZ())).withHoverEvent(new HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, Text.translatable("chat.coordinates.tooltip"))));
            source.sendFeedback(() -> Text.translatable("commands.respawn.success", coords, blocksAway), false);
        } else {
            source.sendFeedback(() -> Text.literal("No previous home spawn point was found").styled((style) -> style.withColor(Formatting.RED)), false);
        }
        return 1;
    }

    private static int worldSpawn(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        BlockPos currentPos = BlockPos.ofFloored(source.getPosition());
        BlockPos worldSpawn = source.getWorld().getSpawnPos();
        int blocksAway = getDistance(currentPos.getX(), currentPos.getY(), worldSpawn.getX(), worldSpawn.getY());
        Text coords = Texts.bracketed(Text.translatable("chat.coordinates", worldSpawn.getX(), worldSpawn.getY(), worldSpawn.getZ())).styled((style) -> style.withColor(Formatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp " + worldSpawn.getX() + " " + worldSpawn.getY() + " " + worldSpawn.getZ())).withHoverEvent(new HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, Text.translatable("chat.coordinates.tooltip"))));
        source.sendFeedback(() -> Text.translatable("commands.spawn.success", coords, blocksAway), false);

        return 1;
    }

    private static int go(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        BlockPos respawnPoint = context.getSource().getPlayer().getSpawnPointPosition();
        player.teleport(respawnPoint.getX(), respawnPoint.getY(), respawnPoint.getZ(), false);

        return 1;
    }

    private static int getDistance(int x1, int y1, int x2, int y2) {
        return Math.round(MathHelper.sqrt((x2 - x1)*(x2 - x1) + (y2 - y1)*(y2 - y1)));
    }
}
