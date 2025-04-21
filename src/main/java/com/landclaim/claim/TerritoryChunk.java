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

    public TerritoryChunk(ChunkPos centerChunk, ResourceKey<Level> dimension) {
        this.centerChunk = centerChunk;
        this.dimension = dimension;
        this.type = TerritoryType.WILDERNESS;
    }

    public ChunkPos getCenterChunk() {
        return centerChunk;
    }

    public boolean isWithinTerritory(ChunkPos pos) {
        int xDiff = Math.abs(pos.x - centerChunk.x);
        int zDiff = Math.abs(pos.z - centerChunk.z);
        return xDiff <= 1 && zDiff <= 1; // Check if within 3x3 chunk area
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

    public ResourceKey<Level> getDimension() {
        return dimension;
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