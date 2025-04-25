package com.landclaim.command;

import com.landclaim.claim.TerritoryChunk;
import com.landclaim.claim.TerritoryType;
import com.landclaim.config.ModConfig;
import com.landclaim.data.DataManager;
import com.landclaim.guild.Guild;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
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
            .then(Commands.literal("claimDungeon")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("guildName", StringArgumentType.word())
                    .executes(ctx -> claimDungeonForGuild(ctx.getSource(), StringArgumentType.getString(ctx, "guildName")))))
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
            
            // Only allow claiming in the overworld
            if (!level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) {
                source.sendFailure(Component.literal("§cTerritories can only be claimed in the overworld"));
                return 0;
            }

            Guild playerGuild = manager.getPlayerGuild(player.getUUID());
            if (playerGuild == null) {
                source.sendFailure(Component.literal("§cYou must be in a guild to claim territory"));
                return 0;
            }

            // Check if any of the chunks in the 2x2 territory are already claimed
            ChunkPos[] chunksInTerritory = getChunksIn2x2Territory(playerChunk);
            for (ChunkPos chunk : chunksInTerritory) {
                TerritoryChunk existingTerritory = manager.getTerritory(level.dimension(), chunk);
                if (existingTerritory != null && existingTerritory.getType() != TerritoryType.WILDERNESS) {
                    Guild existingOwner = manager.getGuild(existingTerritory.getOwnerId());
                    String ownerName = existingOwner != null ? existingOwner.getName() : "unknown";
                    source.sendFailure(
                        Component.literal("§cPart of this 2x2 territory is already claimed by " + ownerName));
                    return 0;
                }
            }

            if (!playerGuild.isOwner(player.getUUID()) && !player.hasPermissions(2)) {
                source.sendFailure(Component.literal("§cOnly guild owners can claim territory"));
                return 0;
            }

            // Check if the player is in creative/spectator mode and is an operator
            boolean freeClaimForOperator = player.hasPermissions(2) && 
                (player.isCreative() || player.isSpectator());

            // Calculate claim cost for all 4 chunks based on connectedness
            int actualClaimCost = claimCost * 4; // 4 chunks total
            boolean isConnected = playerGuild.getClaimedChunks() == 0;
            
            if (!isConnected) {
                // Check if any of the chunks are connected to existing territory
                for (ChunkPos chunk : chunksInTerritory) {
                    if (isAdjacentToGuildTerritory(manager, level, chunk, playerGuild.getId())) {
                        isConnected = true;
                        break;
                    }
                }
            }
            
            if (!isConnected) {
                actualClaimCost *= ModConfig.DISCONNECTED_CLAIM_COST_MULTIPLIER.get();
            }

            // Operators in creative/spectator mode don't need to pay
            if (!freeClaimForOperator && playerGuild.getPowerPoints() < actualClaimCost) {
                source.sendFailure(
                    Component.literal("§cYour guild needs " + actualClaimCost +
                        " power points to claim this 2x2 territory (Current: " +
                        playerGuild.getPowerPoints() + ")")
                );
                return 0;
            }

            // Claim all 4 chunks in the 2x2 territory
            for (ChunkPos chunk : chunksInTerritory) {
                TerritoryChunk territory = new TerritoryChunk(chunk, level.dimension());
                territory.setOwner(playerGuild.getId());
                territory.setType(TerritoryType.CLAIMED);
                manager.setTerritory(territory);
                playerGuild.incrementClaimedChunks();
            }
            
            // Only charge if not a free claim for operator
            if (!freeClaimForOperator) {
                playerGuild.spendPowerPoints(actualClaimCost);
            }

            source.sendSuccess(() ->
                Component.literal("§aSuccessfully claimed 2x2 territory for " + playerGuild.getName() +
                    "\nRemaining power points: " + playerGuild.getPowerPoints()), true);
                    
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cAn error occurred while claiming territory"));
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * Returns all ChunkPos that form a 2x2 territory with the center chunk as the bottom-left
     */
    private static ChunkPos[] getChunksIn2x2Territory(ChunkPos centerChunk) {
        return new ChunkPos[] {
            centerChunk,                                      // Bottom-left
            new ChunkPos(centerChunk.x + 1, centerChunk.z),    // Bottom-right
            new ChunkPos(centerChunk.x, centerChunk.z + 1),    // Top-left
            new ChunkPos(centerChunk.x + 1, centerChunk.z + 1)  // Top-right
        };
    }

    private static int claimForGuild(CommandSourceStack source, String guildName) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            ServerLevel level = player.serverLevel();
            DataManager manager = DataManager.get(level);
            ChunkPos playerChunk = new ChunkPos(player.blockPosition());
            
            // Only allow claiming in the overworld
            if (!level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) {
                source.sendFailure(Component.literal("§cTerritories can only be claimed in the overworld"));
                return 0;
            }

            Guild guild = manager.getGuild(guildName);
            if (guild == null) {
                source.sendFailure(Component.literal("§cGuild not found: " + guildName));
                return 0;
            }

            // Check if any of the chunks in the 2x2 territory are already claimed
            ChunkPos[] chunksInTerritory = getChunksIn2x2Territory(playerChunk);
            for (ChunkPos chunk : chunksInTerritory) {
                TerritoryChunk existingTerritory = manager.getTerritory(level.dimension(), chunk);
                if (existingTerritory != null && existingTerritory.getType() != TerritoryType.WILDERNESS) {
                    Guild existingOwner = manager.getGuild(existingTerritory.getOwnerId());
                    String ownerName = existingOwner != null ? existingOwner.getName() : "unknown";
                    source.sendFailure(
                        Component.literal("§cPart of this 2x2 territory is already claimed by " + ownerName));
                    return 0;
                }
            }

            // Claim all 4 chunks for the guild (operators don't pay power points)
            for (ChunkPos chunk : chunksInTerritory) {
                TerritoryChunk territory = new TerritoryChunk(chunk, level.dimension());
                territory.setOwner(guild.getId());
                territory.setType(TerritoryType.CLAIMED);
                guild.incrementClaimedChunks();
                manager.setTerritory(territory);
            }

            source.sendSuccess(() ->
                Component.literal("§aSuccessfully claimed 2x2 territory for " + guild.getName()), true);
                    
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
            
            // Only allow claiming in the overworld
            if (!level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) {
                source.sendFailure(Component.literal("§cTerritories can only be claimed in the overworld"));
                return 0;
            }

            // Set all chunks in the 2x2 territory as settlement
            ChunkPos[] chunksInTerritory = getChunksIn2x2Territory(playerChunk);
            for (ChunkPos chunk : chunksInTerritory) {
                TerritoryChunk territory = manager.getTerritory(level.dimension(), chunk);
                if (territory == null) {
                    territory = new TerritoryChunk(chunk, level.dimension());
                }

                territory.setType(TerritoryType.SETTLEMENT);
                manager.setTerritory(territory);
            }

            source.sendSuccess(() -> 
                Component.literal("Set 2x2 territory as settlement"), true);
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
            
            // Only allow claiming in the overworld
            if (!level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) {
                source.sendFailure(Component.literal("§cTerritories can only be claimed in the overworld"));
                return 0;
            }

            // Set all chunks in the 2x2 territory as dungeon
            ChunkPos[] chunksInTerritory = getChunksIn2x2Territory(playerChunk);
            for (ChunkPos chunk : chunksInTerritory) {
                TerritoryChunk territory = manager.getTerritory(level.dimension(), chunk);
                if (territory == null) {
                    territory = new TerritoryChunk(chunk, level.dimension());
                }

                territory.setType(TerritoryType.DUNGEON);
                manager.setTerritory(territory);
            }

            source.sendSuccess(() -> 
                Component.literal("Set 2x2 territory as dungeon"), true);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to set dungeon territory"));
            return 0;
        }
    }
    
    private static int claimDungeonForGuild(CommandSourceStack source, String guildName) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            ServerLevel level = player.serverLevel();
            DataManager manager = DataManager.get(level);
            ChunkPos playerChunk = new ChunkPos(player.blockPosition());
            
            // Only allow claiming in the overworld
            if (!level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) {
                source.sendFailure(Component.literal("§cTerritories can only be claimed in the overworld"));
                return 0;
            }

            Guild guild = manager.getGuild(guildName);
            if (guild == null) {
                source.sendFailure(Component.literal("§cGuild not found: " + guildName));
                return 0;
            }

            // Check if any of the chunks in the 2x2 territory are already claimed
            ChunkPos[] chunksInTerritory = getChunksIn2x2Territory(playerChunk);
            for (ChunkPos chunk : chunksInTerritory) {
                TerritoryChunk existingTerritory = manager.getTerritory(level.dimension(), chunk);
                if (existingTerritory != null && existingTerritory.getType() != TerritoryType.WILDERNESS) {
                    Guild existingOwner = manager.getGuild(existingTerritory.getOwnerId());
                    String ownerName = existingOwner != null ? existingOwner.getName() : "unknown";
                    source.sendFailure(
                        Component.literal("§cPart of this 2x2 territory is already claimed by " + ownerName));
                    return 0;
                }
            }

            // Claim all 4 chunks for the guild as DUNGEON type
            for (ChunkPos chunk : chunksInTerritory) {
                TerritoryChunk territory = new TerritoryChunk(chunk, level.dimension());
                territory.setOwner(guild.getId());
                territory.setType(TerritoryType.DUNGEON);
                guild.incrementClaimedChunks();
                manager.setTerritory(territory);
            }

            source.sendSuccess(() ->
                Component.literal("§aSuccessfully claimed 2x2 territory as dungeon for " + guild.getName()), true);
                    
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cAn error occurred while claiming dungeon territory"));
            return 0;
        }
    }

    private static int abandonTerritory(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            ServerLevel level = player.serverLevel();
            DataManager manager = DataManager.get(level);
            ChunkPos playerChunk = new ChunkPos(player.blockPosition());

            // Get the current chunk's territory
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

            // Abandon all chunks in the 2x2 territory if they belong to the same guild
            ChunkPos[] chunksInTerritory = getChunksIn2x2Territory(playerChunk);
            final int[] chunksAbandoned = {0}; // Use an array to make it effectively final
            
            for (ChunkPos chunk : chunksInTerritory) {
                TerritoryChunk chunkTerritory = manager.getTerritory(level.dimension(), chunk);
                if (chunkTerritory != null && chunkTerritory.getType() != TerritoryType.WILDERNESS) {
                    // Only abandon if it belongs to the same guild
                    if (chunkTerritory.getOwnerId() != null && 
                        chunkTerritory.getOwnerId().equals(guild.getId())) {
                        
                        manager.handleUnclaimedTerritory(chunkTerritory);
                        guild.decrementClaimedChunks();
                        chunksAbandoned[0]++;
                    }
                }
            }

            final Guild finalGuild = guild; // Create a final reference to guild
            source.sendSuccess(() -> 
                Component.literal("§aTerritory abandoned. Abandoned " + chunksAbandoned[0] + 
                " chunks. Guild now has " + finalGuild.getClaimedChunks() + " claimed chunks"), true);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cAn unexpected error occurred while abandoning territory"));
            return 0;
        }
    }
}