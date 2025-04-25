package com.landclaim.block.entity;

import com.landclaim.claim.TerritoryChunk;
import com.landclaim.claim.TerritoryType;
import com.landclaim.data.DataManager;
import com.landclaim.guild.Guild;
import com.landclaim.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class GuildDisplayBlockEntity extends BlockEntity {
    private UUID displayedGuildId;
    private boolean forceDisplay = false; // For operator-placed displays
    private Component customName;
    private int updateCounter = 0;
    private static final int UPDATE_FREQUENCY = 20; // Update display once per second
    
    public GuildDisplayBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.GUILD_DISPLAY_ENTITY.get(), pos, state);
    }
    
    public void tick() {
        if (level == null || level.isClientSide) return;
        
        updateCounter++;
        if (updateCounter >= UPDATE_FREQUENCY) {
            updateCounter = 0;
            updateDisplay();
        }
    }
    
    private void updateDisplay() {
        if (level == null) return;
        
        // If this is a forced display, use the set guild ID
        if (forceDisplay && displayedGuildId != null) {
            updateDisplayForGuild(displayedGuildId);
            return;
        }
        
        // Otherwise, determine the guild based on territory
        ChunkPos chunkPos = new ChunkPos(worldPosition);
        TerritoryChunk territory = DataManager.INSTANCE.getTerritoryAt(chunkPos, level.dimension());
        
        if (territory != null && territory.getType() != TerritoryType.WILDERNESS) {
            UUID guildId = territory.getOwnerId();
            if (guildId != null) {
                updateDisplayForGuild(guildId);
            } else {
                clearDisplay();
            }
        } else {
            clearDisplay();
        }
    }
    
    private void updateDisplayForGuild(UUID guildId) {
        Guild guild = DataManager.INSTANCE.getGuild(guildId);
        if (guild == null) {
            clearDisplay();
            return;
        }
        
        // Update the holographic display
        // This would use Display entities in modern Minecraft, but for simplicity
        // we're just storing the data here
        
        displayedGuildId = guildId;
        
        // In a real implementation, this would spawn or update display entities with:
        // 1. Guild name
        // 2. Power points
        // 3. Active effects
        // 4. Member count
    }
    
    private void clearDisplay() {
        displayedGuildId = null;
        // Remove any display entities
    }
    
    public void setForcedGuild(UUID guildId) {
        this.displayedGuildId = guildId;
        this.forceDisplay = true;
        setChanged();
        updateDisplay();
    }
    
    public void clearForcedGuild() {
        this.forceDisplay = false;
        setChanged();
        updateDisplay();
    }
    
    public void onBreak() {
        clearDisplay();
    }
    
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        
        if (tag.contains("DisplayedGuildId")) {
            displayedGuildId = tag.getUUID("DisplayedGuildId");
        }
        
        forceDisplay = tag.getBoolean("ForceDisplay");
        
        if (tag.contains("CustomName")) {
            customName = Component.Serializer.fromJson(tag.getString("CustomName"));
        }
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        
        if (displayedGuildId != null) {
            tag.putUUID("DisplayedGuildId", displayedGuildId);
        }
        
        tag.putBoolean("ForceDisplay", forceDisplay);
        
        if (customName != null) {
            tag.putString("CustomName", Component.Serializer.toJson(customName));
        }
    }
}