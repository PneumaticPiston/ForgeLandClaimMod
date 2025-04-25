package com.landclaim.mixin;

import com.landclaim.claim.TerritoryChunk;
import com.landclaim.claim.TerritoryType;
import com.landclaim.config.ModConfig;
import com.landclaim.data.DataManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NaturalSpawner.class)
public class MobSpawningMixin {

    // Targeting the correct method for validating mob spawn positions
    @Inject(
        method = "isValidSpawnPosition(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/MobSpawnType;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void onMobsCanSpawnAtLocation(
            ServerLevel level, 
            MobCategory category,
            net.minecraft.world.entity.EntityType<?> entityType,
            BlockPos pos, 
            net.minecraft.world.entity.MobSpawnType spawnType,
            CallbackInfoReturnable<Boolean> cir) {
        
        // Skip if the instance is null or we're on the client
        if (DataManager.INSTANCE == null || level.isClientSide()) {
            return;
        }
        
        // Get territory information
        ChunkPos chunkPos = new ChunkPos(pos);
        TerritoryChunk territory = DataManager.INSTANCE.getTerritoryAt(chunkPos, level.dimension());
        
        // Allow all spawning in normal wilderness and claimed territories
        if (territory == null || 
            territory.getType() == TerritoryType.WILDERNESS || 
            territory.getType() == TerritoryType.CLAIMED) {
            return;
        }
        
        // Check Settlement territories - prevent hostile mobs if configured
        if (territory.getType() == TerritoryType.SETTLEMENT && 
            ModConfig.SETTLEMENT_DISABLE_HOSTILE_SPAWNS.get()) {
            
            // Only block hostile mobs and only on the surface
            if (category == MobCategory.MONSTER && pos.getY() >= level.getSeaLevel()) {
                cir.setReturnValue(false);
                return;
            }
        }
        
        // Dungeon territories have no spawning restrictions
        if (territory.getType() == TerritoryType.DUNGEON) {
            // If configured to always allow spawning, don't modify the result
            if (ModConfig.DUNGEON_ALLOW_ALL_SPAWNS.get()) {
                return;
            }
        }
    }
}