package tofu.mods.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.AngleArgumentType;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

// TODO: various spawn points and beds, such as having a cave bed and sleeping in home bed (events)
// TODO: add compatibility with other minecraft and fabric versions
// TODO: add support for other mod loaders (quilt, forge, neoforge) and plugin loaders (paper, bukkit, spigot)
// TODO: update gradle properties (contact)
// TODO: add client slide (maybe?)
public class HomeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("home")
                .requires((source -> source.hasPermissionLevel(2)))
                .executes(HomeCommand::sendCoords)
                .then(CommandManager.literal("go").executes(HomeCommand::go))
                .then(CommandManager.literal("set")
                .then(CommandManager.argument("newPos", BlockPosArgumentType.blockPos())
                        .executes(context -> set(context.getSource(), BlockPosArgumentType.getValidBlockPos(context, "newPos"), 0.0F))
                        .then(CommandManager.argument("angle", AngleArgumentType.angle())
                                .executes(context -> set(context.getSource(), BlockPosArgumentType.getValidBlockPos(context, "newPos"), AngleArgumentType.getAngle(context, "angle")))))));
    }

    private static int sendCoords(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        BlockPos currentPos = BlockPos.ofFloored(source.getPosition());
        BlockPos respawnPoint = source.getPlayer().getSpawnPointPosition();
        if(respawnPoint != null) {
            int blocksAway = getDistance(currentPos.getX(), currentPos.getY(), respawnPoint.getX(), respawnPoint.getY());
            Text coords = Texts.bracketed(Text.translatable("chat.coordinates", respawnPoint.getX(), respawnPoint.getY(), respawnPoint.getZ())).styled((style) -> style.withColor(Formatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp " + respawnPoint.getX() + " " + respawnPoint.getY() + " " + respawnPoint.getZ())).withHoverEvent(new HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, Text.translatable("chat.coordinates.tooltip"))));
            source.sendFeedback(() -> Text.translatable("commands.respawn.success", coords, blocksAway), false);
            return 0;
        } else {
            source.sendFeedback(() -> Text.literal("No previous home spawn point was found").styled((style) -> style.withColor(Formatting.RED)), false);
        }
        return 1;
    }

    private static int go(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        BlockPos respawnPoint = player.getSpawnPointPosition();
        player.teleport(respawnPoint.getX(), respawnPoint.getY(), respawnPoint.getZ(), true);

        return 1;
    }

    private static int set(ServerCommandSource source, BlockPos newSpawn, float angle) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ServerWorld world = source.getWorld();
        player.setSpawnPoint(source.getWorld().getRegistryKey(), newSpawn, angle, false, true);
        if (world.getRegistryKey() != World.OVERWORLD) {
            source.sendError(Text.translatable("commands.respawn.set.failure.not_overworld"));
            return 0;
        } else {
            player.setSpawnPoint(source.getWorld().getRegistryKey(), newSpawn, angle, false, true);
            source.sendFeedback(() -> Text.translatable("commands.respawn.set.success", newSpawn.getX(), newSpawn.getY(), newSpawn.getZ(), angle), true);
            return 1;
        }
    }

    private static int getDistance(int x1, int y1, int x2, int y2) {
        return Math.round(MathHelper.sqrt((x2 - x1)*(x2 - x1) + (y2 - y1)*(y2 - y1)));
    }
}
