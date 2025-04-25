package com.landclaim.claim;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class TerritoryChunk {
    private final ChunkPos centerChunk;
    private final ResourceKey<Level> dimension;
    private UUID ownerId;
    private TerritoryType type;
    private BlockPos totemPos;
    private boolean isConnectedToTotem;

    public TerritoryChunk(ChunkPos centerChunk, ResourceKey<Level> dimension) {
        this.centerChunk = centerChunk;
        this.dimension = dimension;
        this.type = TerritoryType.WILDERNESS;
        this.isConnectedToTotem = false;
    }

    public ChunkPos getCenterChunk() {
        return centerChunk;
    }

    public boolean isWithinTerritory(ChunkPos pos) {
        int xDiff = Math.abs(pos.x - centerChunk.x);
        int zDiff = Math.abs(pos.z - centerChunk.z);
        return xDiff <= 1 && zDiff <= 1; // Check if within 3x3 chunk area
    }
    
    public boolean containsChunk(ChunkPos pos) {
        return isWithinTerritory(pos);
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwner(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public TerritoryType getType() {
        return type;
    }

    public void setType(TerritoryType type) {
        this.type = type;
    }

    public BlockPos getTotemPos() {
        return totemPos;
    }

    public void setTotemPos(BlockPos pos) {
        this.totemPos = pos;
    }
    
    public boolean isConnectedToTotem() {
        return isConnectedToTotem;
    }
    
    public void setConnectedToTotem(boolean connected) {
        this.isConnectedToTotem = connected;
    }

    public ResourceKey<Level> getDimension() {
        return dimension;
    }
    
    /**
     * Checks if this chunk is adjacent to another chunk.
     * Two chunks are adjacent if they share at least one edge.
     * 
     * @param other The other chunk to check adjacency with
     * @return true if the chunks are adjacent, false otherwise
     */
    public boolean isAdjacentTo(TerritoryChunk other) {
        if (!dimension.equals(other.dimension)) return false;
        
        int xDiff = Math.abs(centerChunk.x - other.centerChunk.x);
        int zDiff = Math.abs(centerChunk.z - other.centerChunk.z);
        
        // Adjacent if they share at least one edge
        return (xDiff == 1 && zDiff == 0) || (xDiff == 0 && zDiff == 1);
    }

    // Save territory data to NBT
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("centerX", centerChunk.x);
        tag.putInt("centerZ", centerChunk.z);
        tag.putString("dimension", dimension.location().toString());
        if (ownerId != null) {
            tag.putUUID("owner", ownerId);
        }
        tag.putInt("type", type.ordinal());
        tag.putBoolean("connected", isConnectedToTotem);
        
        if (totemPos != null) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("x", totemPos.getX());
            posTag.putInt("y", totemPos.getY());
            posTag.putInt("z", totemPos.getZ());
            tag.put("totemPos", posTag);
        }
        return tag;
    }

    // Load territory data from NBT
    public static TerritoryChunk load(CompoundTag tag, ResourceKey<Level> dimension) {
        ChunkPos center = new ChunkPos(tag.getInt("centerX"), tag.getInt("centerZ"));
        TerritoryChunk territory = new TerritoryChunk(center, dimension);
        
        if (tag.contains("owner")) {
            territory.ownerId = tag.getUUID("owner");
        }
        territory.type = TerritoryType.values()[tag.getInt("type")];
        territory.isConnectedToTotem = tag.getBoolean("connected");
        
        if (tag.contains("totemPos")) {
            CompoundTag posTag = tag.getCompound("totemPos");
            territory.totemPos = new BlockPos(
                posTag.getInt("x"),
                posTag.getInt("y"),
                posTag.getInt("z")
            );
        }
        
        return territory;
    }
}