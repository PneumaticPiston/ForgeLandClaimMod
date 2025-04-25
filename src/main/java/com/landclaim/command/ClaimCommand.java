package com.landclaim.command;

import com.landclaim.claim.TerritoryChunk;
import com.landclaim.claim.TerritoryType;
import com.landclaim.config.ModConfig;
import com.landclaim.data.DataManager;
import com.landclaim.guild.Guild;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import java.util.UUID;

public class ClaimCommand {
    private static int claimCost = ModConfig.CLAIM_COST.get();

    public static void setClaimCost(int cost) {
        claimCost = cost;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("claim")
            .then(Commands.literal("territory")
                .executes(ctx -> claimTerritory(ctx.getSource())))
            .then(Commands.literal("info")
                .executes(ctx -> getClaimInfo(ctx.getSource())))
            .then(Commands.literal("settlement")
                .requires(source -> source.hasPermission(2))
                .executes(ctx -> setSettlementTerritory(ctx.getSource())))
            .then(Commands.literal("dungeon")
                .requires(source -> source.hasPermission(2))
                .executes(ctx -> setDungeonTerritory(ctx.getSource())))
            .then(Commands.literal("forGuild")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("guildName", StringArgumentType.word())
                    .executes(ctx -> claimForGuild(ctx.getSource(), StringArgumentType.getString(ctx, "guildName")))))
            .then(Commands.literal("abandon")
                .executes(ctx -> abandonTerritory(ctx.getSource()))));
    }

    private static boolean isAdjacentToGuildTerritory(DataManager manager, ServerLevel level, ChunkPos chunk, UUID guildId) {
        ChunkPos[] adjacent = new ChunkPos[] {
            new ChunkPos(chunk.x + 1, chunk.z),
            new ChunkPos(chunk.x - 1, chunk.z),
            new ChunkPos(chunk.x, chunk.z + 1),
            new ChunkPos(chunk.x, chunk.z - 1)
        };

        for (ChunkPos pos : adjacent) {
            TerritoryChunk territory = manager.getTerritory(level.dimension(), pos);
            if (territory != null && territory.getType() != TerritoryType.WILDERNESS 
                && territory.getOwnerId().equals(guildId)) {
                return true;
            }
        }
        return false;
    }

    private static int claimTerritory(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            ServerLevel level = player.serverLevel();
            DataManager manager = DataManager.get(level);
            ChunkPos playerChunk = new ChunkPos(player.blockPosition());

            Guild playerGuild = manager.getPlayerGuild(player.getUUID());
            if (playerGuild == null) {
                source.sendFailure(Component.literal("§cYou must be in a guild to claim territory"));
                return 0;
            }

            TerritoryChunk territory = manager.getTerritory(level.dimension(), playerChunk);
            if (territory != null && territory.getType() != TerritoryType.WILDERNESS) {
                source.sendFailure(
                    Component.literal("§cThis territory is already claimed by " +
                        manager.getGuild(territory.getOwnerId()).getName()));
                return 0;
            }

            if (!playerGuild.isOwner(player.getUUID()) && !player.hasPermissions(2)) {
                source.sendFailure(Component.literal("§cOnly guild owners can claim territory"));
                return 0;
            }

            // Calculate claim cost based on connectedness 
            int actualClaimCost = claimCost;
            boolean isConnected = playerGuild.getClaimedChunks() == 0 || 
                                  isAdjacentToGuildTerritory(manager, level, playerChunk, playerGuild.getId());
            
            if (!isConnected) {
                actualClaimCost *= ModConfig.DISCONNECTED_CLAIM_COST_MULTIPLIER.get();
            }

            if (playerGuild.getPowerPoints() < actualClaimCost) {
                source.sendFailure(
                    Component.literal("§cYour guild needs " + actualClaimCost +
                        " power points to claim territory (Current: " +
                        playerGuild.getPowerPoints() + ")")
                );
                return 0;
            }

            territory = new TerritoryChunk(playerChunk, level.dimension());
            territory.setOwner(playerGuild.getId());
            territory.setType(TerritoryType.CLAIMED);
            playerGuild.spendPowerPoints(actualClaimCost);
            playerGuild.incrementClaimedChunks();
            manager.setTerritory(territory);

            source.sendSuccess(() ->
                Component.literal("§aSuccessfully claimed territory for " + playerGuild.getName() +
                    "\nRemaining power points: " + playerGuild.getPowerPoints()), true);
                    
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cAn error occurred while claiming territory"));
            return 0;
        }
    }

    private static int claimForGuild(CommandSourceStack source, String guildName) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            ServerLevel level = player.serverLevel();
            DataManager manager = DataManager.get(level);
            ChunkPos playerChunk = new ChunkPos(player.blockPosition());

            Guild guild = manager.getGuild(guildName);
            if (guild == null) {
                source.sendFailure(Component.literal("§cGuild not found: " + guildName));
                return 0;
            }

            TerritoryChunk territory = manager.getTerritory(level.dimension(), playerChunk);
            if (territory != null && territory.getType() != TerritoryType.WILDERNESS) {
                source.sendFailure(
                    Component.literal("§cThis territory is already claimed by " +
                        manager.getGuild(territory.getOwnerId()).getName()));
                return 0;
            }

            territory = new TerritoryChunk(playerChunk, level.dimension());
            territory.setOwner(guild.getId());
            territory.setType(TerritoryType.CLAIMED);
            guild.incrementClaimedChunks();
            manager.setTerritory(territory);

            source.sendSuccess(() ->
                Component.literal("§aSuccessfully claimed territory for " + guild.getName()), true);
                    
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cAn error occurred while claiming territory"));
            return 0;
        }
    }

    private static int getClaimInfo(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            ServerLevel level = player.serverLevel();
            DataManager manager = DataManager.get(level);
            ChunkPos playerChunk = new ChunkPos(player.blockPosition());

            TerritoryChunk territory = manager.getTerritory(level.dimension(), playerChunk);
            if (territory == null || territory.getType() == TerritoryType.WILDERNESS) {
                source.sendSuccess(() -> 
                    Component.literal("This is unclaimed wilderness"), false);
                return Command.SINGLE_SUCCESS;
            }

            Guild owner = manager.getGuild(territory.getOwnerId());
            String ownerName = owner != null ? owner.getName() : "Unknown";
            String type = territory.getType().name();

            source.sendSuccess(() -> 
                Component.literal("Territory Info:\nType: " + type + "\nOwner: " + ownerName), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to get territory info"));
            return 0;
        }
    }

    private static int setSettlementTerritory(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            ServerLevel level = player.serverLevel();
            DataManager manager = DataManager.get(level);
            ChunkPos playerChunk = new ChunkPos(player.blockPosition());

            TerritoryChunk territory = manager.getTerritory(level.dimension(), playerChunk);
            if (territory == null) {
                territory = new TerritoryChunk(playerChunk, level.dimension());
            }

            territory.setType(TerritoryType.SETTLEMENT);
            manager.setTerritory(territory);

            source.sendSuccess(() -> 
                Component.literal("Set territory as settlement"), true);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to set settlement territory"));
            return 0;
        }
    }

    private static int setDungeonTerritory(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            ServerLevel level = player.serverLevel();
            DataManager manager = DataManager.get(level);
            ChunkPos playerChunk = new ChunkPos(player.blockPosition());

            TerritoryChunk territory = manager.getTerritory(level.dimension(), playerChunk);
            if (territory == null) {
                territory = new TerritoryChunk(playerChunk, level.dimension());
            }

            territory.setType(TerritoryType.DUNGEON);
            manager.setTerritory(territory);

            source.sendSuccess(() -> 
                Component.literal("Set territory as dungeon"), true);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to set dungeon territory"));
            return 0;
        }
    }

    private static int abandonTerritory(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            ServerLevel level = player.serverLevel();
            DataManager manager = DataManager.get(level);
            ChunkPos playerChunk = new ChunkPos(player.blockPosition());

            TerritoryChunk territory = manager.getTerritory(level.dimension(), playerChunk);
            if (territory == null || territory.getType() == TerritoryType.WILDERNESS) {
                source.sendFailure(Component.literal("§cThis territory is not claimed"));
                return 0;
            }

            Guild guild = manager.getGuild(territory.getOwnerId());
            if (guild == null || (!guild.isOwner(player.getUUID()) && !player.hasPermissions(2))) {
                source.sendFailure(Component.literal("§cYou don't have permission to abandon this territory"));
                return 0;
            }

            territory.setType(TerritoryType.WILDERNESS);
            territory.setOwner(null);
            manager.setTerritory(territory);
            guild.decrementClaimedChunks();

            source.sendSuccess(() -> 
                Component.literal("§aTerritory abandoned. Guild now has " + guild.getClaimedChunks() + " claimed chunks"), true);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cAn unexpected error occurred while abandoning territory"));
            return 0;
        }
    }
}