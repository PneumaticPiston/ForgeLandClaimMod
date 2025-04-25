package com.landclaim.block.entity;

import com.landclaim.claim.TerritoryChunk;
import com.landclaim.config.ModConfig;
import com.landclaim.guild.Guild;
import com.landclaim.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TotemBaseBlockEntity extends BlockEntity {
    private UUID guildId;
    private final List<MobEffect> activeEffects;
    private int powerPointsGenerated;
    private static final int EFFECT_RADIUS = 48; // 3 chunks worth
    private boolean isConnectedToTerritory = false;
    private int maxTotemCheckHeight;
    
    // Totem effect modifiers
    private boolean keepInventoryOnDeath = false;
    private float potionEffectModifier = 1.0f;
    private float mobSpawnModifier = 1.0f;

    public TotemBaseBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.TOTEM_BASE_ENTITY.get(), pos, state);
        this.activeEffects = new ArrayList<>();
        this.powerPointsGenerated = 0;
        this.maxTotemCheckHeight = ModConfig.DEFAULT_TOTEM_CHECK_HEIGHT.get();
    }

    public void setGuild(UUID guildId) {
        this.guildId = guildId;
    }

    public UUID getGuild() {
        return guildId;
    }

    public void addEffect(MobEffect effect) {
        if (!activeEffects.contains(effect)) {
            activeEffects.add(effect);
            setChanged();
        }
    }

    public void removeEffect(MobEffect effect) {
        activeEffects.remove(effect);
        setChanged();
    }
    
    public void setIsConnectedToTerritory(boolean connected) {
        this.isConnectedToTerritory = connected;
    }
    
    public boolean isConnectedToTerritory() {
        return isConnectedToTerritory;
    }

    private void applyEffectsToPlayers() {
        if (level == null || guildId == null || activeEffects.isEmpty() || !isConnectedToTerritory) return;

        AABB effectBox = new AABB(worldPosition).inflate(EFFECT_RADIUS);
        List<Player> players = level.getEntitiesOfClass(Player.class, effectBox);

        for (Player player : players) {
            // TODO: Get Guild from DataManager and check if player belongs to guild
            // For now, just apply to all players in range
            for (MobEffect effect : activeEffects) {
                int amplifier = (int)((potionEffectModifier - 1.0f) * 2.0f); // Convert 1.5x to level 1 (0-based), etc.
                player.addEffect(new MobEffectInstance(effect, 219, amplifier, true, false));
            }
        }
    }

    public void tick() {
        if (level != null && !level.isClientSide) {
            scanTotemStructure();
            applyEffectsToPlayers();
            generatePowerPoints();
        }
    }

    /**
     * Scans the totem structure above this block entity
     * and applies effects based on the blocks found.
     */
    private void scanTotemStructure() {
        if (level == null) return;
        
        // Reset effects and modifiers
        activeEffects.clear();
        potionEffectModifier = 1.0f;
        mobSpawnModifier = 1.0f;
        keepInventoryOnDeath = false;
        
        int heightModifier = 0;
        int blocksToCheck = maxTotemCheckHeight;
        
        // Track the last effect block to apply modifiers
        BlockState lastEffectBlock = null;
        boolean lastWasEnhancer = false;
        boolean lastWasInhibitor = false;
        
        for (int y = 1; y <= blocksToCheck + heightModifier; y++) {
            BlockPos posToCheck = worldPosition.above(y);
            BlockState state = level.getBlockState(posToCheck);
            
            // Check for height extenders (don't count toward the height limit)
            if (state.is(Blocks.DIAMOND_BLOCK)) {
                heightModifier += 4;
                continue;
            } else if (state.is(Blocks.IRON_BLOCK)) {
                heightModifier += 2;
                continue;
            }
            
            // Skip air blocks
            if (state.isAir()) {
                continue;
            }
            
            // Check for effect enhancer/inhibitor
            if (state.is(ModRegistry.ENHANCER_BLOCK.get())) {
                lastWasEnhancer = true;
                lastWasInhibitor = false;
                continue;
            } else if (state.is(ModRegistry.INHIBITOR_BLOCK.get())) {
                lastWasInhibitor = true;
                lastWasEnhancer = false;
                continue;
            }
            
            // Apply modifiers to the last effect block
            if (lastEffectBlock != null) {
                if (lastWasEnhancer) {
                    // Enhance the effect
                    if (lastEffectBlock.getBlock() instanceof CauldronBlock) {
                        potionEffectModifier += 0.5f;
                    } else if (isSpawnModifier(lastEffectBlock)) {
                        mobSpawnModifier += 0.5f;
                    }
                } else if (lastWasInhibitor) {
                    // Inhibit the effect
                    if (lastEffectBlock.getBlock() instanceof CauldronBlock) {
                        potionEffectModifier -= 0.5f;
                    } else if (isSpawnModifier(lastEffectBlock)) {
                        mobSpawnModifier -= 0.5f;
                    }
                }
                lastWasEnhancer = false;
                lastWasInhibitor = false;
                lastEffectBlock = null;
            }
            
            // Keep inventory block
            if (state.is(ModRegistry.INVENTORY_KEEPER_BLOCK.get())) {
                keepInventoryOnDeath = true;
            }
            
            // Process potions in cauldrons
            if (state.getBlock() instanceof CauldronBlock) {
                // TODO: Add potion effect detection from cauldron
                // For now just add a placeholder effect
                lastEffectBlock = state;
            }
            
            // Other block type checks would go here
        }
    }
    
    private boolean isSpawnModifier(BlockState state) {
        // TODO: Implement proper check for blocks that modify mob spawning
        return false;
    }

    private void generatePowerPoints() {
        if (!isConnectedToTerritory) return;
        
        // Get base power points per totem from config
        int basePoints = ModConfig.BASE_POWER_POINTS_PER_TOTEM.get();
        
        powerPointsGenerated++;
        if (powerPointsGenerated >= 1200) { // 1 minute at 20 ticks/second
            // TODO: Get Guild from DataManager and add points
            // DataManager.INSTANCE.addGuildPowerPoints(guildId, basePoints);
            powerPointsGenerated = 0;
            setChanged();
        }
    }

    public boolean shouldKeepInventoryOnDeath() {
        return keepInventoryOnDeath;
    }
    
    public void consumeInventoryKeeperBlock() {
        if (level == null) return;
        
        // Search for the inventory keeper block
        for (int y = 1; y <= maxTotemCheckHeight; y++) {
            BlockPos posToCheck = worldPosition.above(y);
            BlockState state = level.getBlockState(posToCheck);
            
            if (state.is(ModRegistry.INVENTORY_KEEPER_BLOCK.get())) {
                // Remove the block
                level.setBlockAndUpdate(posToCheck, Blocks.AIR.defaultBlockState());
                
                // Move blocks above down to fill the gap
                moveBlocksDown(posToCheck);
                break;
            }
        }
    }
    
    private void moveBlocksDown(BlockPos startPos) {
        if (level == null) return;
        
        // Get the maximum height of the totem (top to bottom)
        int maxHeight = startPos.getY() - worldPosition.getY();
        for (int y = 1; y <= maxHeight; y++) {
            BlockPos currentPos = startPos.above(y);
            BlockState stateAbove = level.getBlockState(currentPos);
            
            if (stateAbove.isAir()) {
                break; // Stop when we hit air
            }
            
            // Move block down
            level.setBlockAndUpdate(currentPos.below(), stateAbove);
            level.setBlockAndUpdate(currentPos, Blocks.AIR.defaultBlockState());
        }
    }

    public void onBreak() {
        // TODO: Handle totem break - remove effects, notify guild, etc.
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("GuildId")) {
            guildId = tag.getUUID("GuildId");
        }
        powerPointsGenerated = tag.getInt("PowerPoints");
        isConnectedToTerritory = tag.getBoolean("IsConnected");
        maxTotemCheckHeight = tag.getInt("MaxCheckHeight");
        // TODO: Load active effects
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (guildId != null) {
            tag.putUUID("GuildId", guildId);
        }
        tag.putInt("PowerPoints", powerPointsGenerated);
        tag.putBoolean("IsConnected", isConnectedToTerritory);
        tag.putInt("MaxCheckHeight", maxTotemCheckHeight);
        // TODO: Save active effects
    }
}