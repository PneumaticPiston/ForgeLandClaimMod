package com.landclaim.command;

import com.landclaim.claim.TerritoryChunk;
import com.landclaim.claim.TerritoryType;
import com.landclaim.data.DataManager;
import com.landclaim.team.Team;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

public class ClaimCommand {
    private static final int DEFAULT_CLAIM_COST = 100;
    private static int claimCost = DEFAULT_CLAIM_COST;

    public static void setClaimCost(int cost) {
        claimCost = cost;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("claim")
            .then(Commands.literal("territory")
                .executes(ctx -> claimTerritory(ctx.getSource())))
            .then(Commands.literal("info")
                .executes(ctx -> getClaimInfo(ctx.getSource())))
            .then(Commands.literal("settown")
                .requires(source -> source.hasPermission(2))
                .executes(ctx -> setTownTerritory(ctx.getSource())))
            .then(Commands.literal("abandon")
                .executes(ctx -> abandonTerritory(ctx.getSource()))));
    }

    private static boolean isAdjacentToTeamTerritory(DataManager manager, ServerLevel level, ChunkPos chunk, String teamId) {
        ChunkPos[] adjacent = new ChunkPos[] {
            new ChunkPos(chunk.x + 1, chunk.z),
            new ChunkPos(chunk.x - 1, chunk.z),
            new ChunkPos(chunk.x, chunk.z + 1),
            new ChunkPos(chunk.x, chunk.z - 1)
        };

        for (ChunkPos pos : adjacent) {
            TerritoryChunk territory = manager.getTerritory(level.dimension(), pos);
            if (territory != null && territory.getType() != TerritoryType.WILDERNESS 
                && territory.getOwnerId().equals(teamId)) {
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

            Team playerTeam = manager.getPlayerTeam(player.getUUID());
            if (playerTeam == null) {
                source.sendFailure(Component.literal("§cYou must be in a team to claim territory"));
                return 0;
            }

            TerritoryChunk territory = manager.getTerritory(level.dimension(), playerChunk);
            if (territory != null && territory.getType() != TerritoryType.WILDERNESS) {
                source.sendFailure(
                    Component.literal("§cThis territory is already claimed by " +
                        manager.getTeam(territory.getOwnerId()).getName()));
                return 0;
            }

            if (!playerTeam.isOwner(player.getUUID()) && !player.hasPermissions(2)) {
                source.sendFailure(Component.literal("§cOnly team owners can claim territory"));
                return 0;
            }

            if (playerTeam.getPowerPoints() < claimCost) {
                source.sendFailure(
                    Component.literal("§cYour team needs " + claimCost +
                        " power points to claim territory (Current: " +
                        playerTeam.getPowerPoints() + ")")
                );
                return 0;
            }

            if (playerTeam.getClaimedChunks() > 0 &&
                !isAdjacentToTeamTerritory(manager, level, playerChunk, playerTeam.getId())) {
                source.sendFailure(Component.literal("§cNew claims must be adjacent to existing territory"));
                return 0;
            }

            territory = new TerritoryChunk(playerChunk, level.dimension());
            territory.setOwner(playerTeam.getId());
            territory.setType(TerritoryType.CLAIMED);
            playerTeam.spendPowerPoints(claimCost);
            playerTeam.incrementClaimedChunks();
            manager.setTerritory(territory);

            source.sendSuccess(() ->
                Component.literal("§aSuccessfully claimed territory for " + playerTeam.getName() +
                    "\nRemaining power points: " + playerTeam.getPowerPoints()), true);
                    
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

            Team owner = manager.getTeam(territory.getOwnerId());
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

    private static int setTownTerritory(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            ServerLevel level = player.serverLevel();
            DataManager manager = DataManager.get(level);
            ChunkPos playerChunk = new ChunkPos(player.blockPosition());

            TerritoryChunk territory = manager.getTerritory(level.dimension(), playerChunk);
            if (territory == null) {
                territory = new TerritoryChunk(playerChunk, level.dimension());
            }

            territory.setType(TerritoryType.TOWN);
            manager.setTerritory(territory);

            source.sendSuccess(() -> 
                Component.literal("Set territory as town"), true);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to set town territory"));
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

            Team team = manager.getTeam(territory.getOwnerId());
            if (team == null || (!team.isOwner(player.getUUID()) && !player.hasPermissions(2))) {
                source.sendFailure(Component.literal("§cYou don't have permission to abandon this territory"));
                return 0;
            }

            territory.setType(TerritoryType.WILDERNESS);
            territory.setOwner(null);
            manager.setTerritory(territory);
            team.decrementClaimedChunks();

            source.sendSuccess(() -> 
                Component.literal("§aTerritory abandoned. Team now has " + team.getClaimedChunks() + " claimed chunks"), true);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cAn unexpected error occurred while abandoning territory"));
            return 0;
        }
    }
}