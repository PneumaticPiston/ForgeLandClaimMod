package com.landclaim.mixin;

import com.landclaim.claim.TerritoryChunk;
import com.landclaim.data.DataManager;
import com.landclaim.block.entity.TotemBaseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ServerPlayer.class)
public abstract class PlayerDeathMixin {

    @Inject(method = "die(Lnet/minecraft/world/damagesource/DamageSource;)V", at = @At("HEAD"), cancellable = true)
    private void onPlayerDeath(DamageSource source, CallbackInfo ci) {
        Player player = (Player)(Object)this;
        
        if (player.level().isClientSide()) return;
        
        ServerLevel level = (ServerLevel) player.level();
        ChunkPos chunkPos = new ChunkPos(player.blockPosition());
        TerritoryChunk territory = DataManager.INSTANCE.getTerritoryAt(chunkPos, level.dimension());
        
        if (territory != null && territory.isConnectedToTotem()) {
            UUID guildId = territory.getOwnerId();
            UUID playerGuild = DataManager.INSTANCE.getPlayerGuild(player.getUUID()).getId();
            
            // Check if player is in the same guild as the territory
            if (guildId != null && guildId.equals(playerGuild)) {
                BlockPos totemPos = territory.getTotemPos();
                if (totemPos != null) {
                    BlockEntity blockEntity = level.getBlockEntity(totemPos);
                    if (blockEntity instanceof TotemBaseBlockEntity) {
                        TotemBaseBlockEntity totem = (TotemBaseBlockEntity) blockEntity;
                        
                        // Check if the totem has keep inventory effect
                        if (totem.shouldKeepInventoryOnDeath()) {
                            // Consume the inventory keeper block
                            totem.consumeInventoryKeeperBlock();
                            
                            // Skip regular death handling - keep inventory
                            player.setHealth(1.0f);
                            ci.cancel();
                        }
                    }
                }
            }
        }
    }
}