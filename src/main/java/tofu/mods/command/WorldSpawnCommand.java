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


public class WorldSpawnCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("worldspawn")
                .requires((source -> source.hasPermissionLevel(2)))
                .executes(WorldSpawnCommand::sendCoords)
                .then(CommandManager.literal("go").executes(WorldSpawnCommand::go))
                .then(CommandManager.literal("set")
                .then(CommandManager.argument("newPos", BlockPosArgumentType.blockPos())
                        .executes(context -> set(context.getSource(), BlockPosArgumentType.getValidBlockPos(context, "newPos"), 0.0F))
                            .then(CommandManager.argument("angle", AngleArgumentType.angle())
                                    .executes(context -> set(context.getSource(), BlockPosArgumentType.getValidBlockPos(context, "newPos"), AngleArgumentType.getAngle(context, "angle")))))));
    }

    private static int sendCoords(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        BlockPos currentPos = BlockPos.ofFloored(source.getPosition());
        BlockPos worldSpawn = source.getWorld().getSpawnPos();
        int blocksAway = getDistance(currentPos.getX(), currentPos.getY(), worldSpawn.getX(), worldSpawn.getY());
        Text coords = Texts.bracketed(Text.translatable("chat.coordinates", worldSpawn.getX(), worldSpawn.getY(), worldSpawn.getZ())).styled((style) -> style.withColor(Formatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp " + worldSpawn.getX() + " " + worldSpawn.getY() + " " + worldSpawn.getZ())).withHoverEvent(new HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, Text.translatable("chat.coordinates.tooltip"))));
        source.sendFeedback(() -> Text.translatable("commands.worldspawn.success", coords, blocksAway), false);

        return 1;
    }

    private static int go(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        BlockPos spawnPoint = context.getSource().getWorld().getSpawnPos();
        player.teleport(spawnPoint.getX(), spawnPoint.getY(), spawnPoint.getZ(), true);

        return 1;
    }

    private static int set(ServerCommandSource source, BlockPos newSpawn, float angle) {
        ServerWorld serverWorld = source.getWorld();
        if (serverWorld.getRegistryKey() != World.OVERWORLD) {
            source.sendError(Text.translatable("commands.worldspawn.set.failure.not_overworld"));
            return 0;
        } else {
            serverWorld.setSpawnPos(newSpawn, angle);
            source.sendFeedback(() -> Text.translatable("commands.worldspawn.set.success", newSpawn.getX(), newSpawn.getY(), newSpawn.getZ(), angle), true);
            return 1;
        }
    }

    private static int getDistance(int x1, int y1, int x2, int y2) {
        return Math.round(MathHelper.sqrt((x2 - x1)*(x2 - x1) + (y2 - y1)*(y2 - y1)));
    }
}
