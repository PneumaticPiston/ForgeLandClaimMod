package com.landclaim.mixin;

import com.landclaim.claim.TerritoryChunk;
import com.landclaim.claim.TerritoryType;
import com.landclaim.config.ModConfig;
import com.landclaim.data.DataManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NaturalSpawner.class)
public class MobSpawningMixin {

    @Inject(method = "isValidSpawnPosition(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/MobSpawnType;)Z", at = @At("HEAD"), cancellable = true)
    private static void onCheckSpawn(ServerLevel level, MobCategory mobCategory, EntityType<?> entityType, 
                                   BlockPos pos, MobSpawnType spawnType, CallbackInfoReturnable<Boolean> cir) {
        if (level.isClientSide()) return;
        
        // Only handle MONSTER category for now
        if (mobCategory != MobCategory.MONSTER) return;
        
        ChunkPos chunkPos = new ChunkPos(pos);
        TerritoryChunk territory = DataManager.INSTANCE.getTerritoryAt(chunkPos, level.dimension());
        
        if (territory != null) {
            if (territory.getType() == TerritoryType.SETTLEMENT && ModConfig.SETTLEMENT_DISABLE_HOSTILE_SPAWNS.get()) {
                // Check if the spawn position is on the surface
                if (pos.getY() >= level.getSeaLevel()) {
                    cir.setReturnValue(false);
                }
            } else if (territory.getType() == TerritoryType.CLAIMED) {
                // Apply totem spawn modifiers for claimed territories
                // This would require getting the totem for this territory and checking modifiers
            } else if (territory.getType() == TerritoryType.DUNGEON && ModConfig.DUNGEON_ALLOW_ALL_SPAWNS.get()) {
                // Dungeons should allow all spawns as normal
                // We don't need to modify anything
            }
        }
    }
}