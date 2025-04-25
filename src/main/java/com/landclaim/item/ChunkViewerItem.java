package com.landclaim.item;

import com.landclaim.claim.TerritoryChunk;
import com.landclaim.data.DataManager;
import com.landclaim.guild.Guild;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import org.joml.Vector3f;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChunkViewerItem extends Item {
    // Cache of player UUIDs and the last time they activated the viewer
    private static final Map<UUID, Long> ACTIVE_VIEWERS = new HashMap<>();
    // How long particles remain visible (10 seconds)
    private static final long PARTICLE_DURATION = 10000L;
    // Spacing between particles
    private static final float PARTICLE_SPACING = 2.0f;
    
    public ChunkViewerItem(Properties properties) {
        super(properties);
    }

    @Override
    public @Nonnull InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        if (!level.isClientSide && player instanceof ServerPlayer) {
            ChunkPos playerChunk = new ChunkPos(player.blockPosition());
            ServerLevel serverLevel = (ServerLevel) level;
            
            // Check if viewer is already active
            boolean wasActive = ACTIVE_VIEWERS.containsKey(player.getUUID()) && 
                               (System.currentTimeMillis() - ACTIVE_VIEWERS.get(player.getUUID()) < PARTICLE_DURATION);
            
            // Update or add viewer state
            ACTIVE_VIEWERS.put(player.getUUID(), System.currentTimeMillis());
            
            // Show claim info
            TerritoryChunk territory = DataManager.get(serverLevel).getTerritory(level.dimension(), playerChunk);
            if (territory != null) {
                Guild owner = territory.getOwnerId() != null ? 
                    DataManager.get(serverLevel).getGuild(territory.getOwnerId()) : null;
                
                String ownerName = owner != null ? owner.getName() : "None";
                player.displayClientMessage(
                    Component.literal(String.format(
                        "Territory at (%d, %d): %s - Owner: %s", 
                        playerChunk.x, playerChunk.z,
                        territory.getType().name(), 
                        ownerName
                    )), 
                    true
                );
                
                // Only show confirmation if the viewer wasn't already active
                if (!wasActive) {
                    player.displayClientMessage(
                        Component.literal("Showing claim boundaries for 10 seconds..."), 
                        true
                    );
                }
                
                // Get different colored particles based on territory type
                Vector3f particleColor = getParticleColorForTerritory(territory);
                
                // Show claim boundaries with particles
                showClaimBoundaries(serverLevel, territory, particleColor);
            } else {
                player.displayClientMessage(
                    Component.literal(String.format(
                        "Chunk at (%d, %d): Wilderness", 
                        playerChunk.x, playerChunk.z
                    )), 
                    true
                );
            }
        }

        return InteractionResultHolder.success(itemstack);
    }
    
    private void showClaimBoundaries(ServerLevel level, TerritoryChunk territory, Vector3f color) {
        ChunkPos centerChunk = territory.getCenterChunk();
        
        // Calculate the bounds of the territory (assuming 3x3 chunks)
        int minX = (centerChunk.x - 1) * 16;
        int minZ = (centerChunk.z - 1) * 16;
        int maxX = (centerChunk.x + 2) * 16;
        int maxZ = (centerChunk.z + 2) * 16;
        
        DustParticleOptions particleOptions = new DustParticleOptions(color, 1.0f);
        int y = level.getSeaLevel() + 1;  // Slightly above sea level for visibility
        
        // Show boundary lines at each edge
        for (float x = minX; x <= maxX; x += PARTICLE_SPACING) {
            spawnParticle(level, x, y, (float)minZ, particleOptions);
            spawnParticle(level, x, y, (float)maxZ, particleOptions);
        }
        
        for (float z = minZ; z <= maxZ; z += PARTICLE_SPACING) {
            spawnParticle(level, (float)minX, y, z, particleOptions);
            spawnParticle(level, (float)maxX, y, z, particleOptions);
        }
    }
    
    private void spawnParticle(ServerLevel level, float x, float y, float z, DustParticleOptions particleOptions) {
        level.sendParticles(
            particleOptions,
            x, y, z,
            1,  // count
            0.0, 0.0, 0.0,  // offset
            0.0  // speed
        );
    }
    
    private Vector3f getParticleColorForTerritory(TerritoryChunk territory) {
        // Define colors for different territory types
        switch(territory.getType()) {
            case CLAIMED:
                return new Vector3f(0.0f, 0.7f, 0.0f);  // Green for claimed
            case SETTLEMENT:
                return new Vector3f(0.0f, 0.0f, 1.0f);  // Blue for settlements
            case DUNGEON:
                return new Vector3f(1.0f, 0.0f, 0.0f);  // Red for dungeons
            case WILDERNESS:
            default:
                return new Vector3f(1.0f, 1.0f, 1.0f);  // White for wilderness
        }
    }

    @Override
    public boolean isFoil(@Nonnull ItemStack stack) {
        return true; // Always show enchantment glint
    }
}