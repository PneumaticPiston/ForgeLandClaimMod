package com.landclaim.block;

import com.landclaim.block.entity.TotemBaseBlockEntity;
import com.landclaim.data.DataManager;
import com.landclaim.guild.Guild;
import com.landclaim.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TotemBaseBlock extends Block implements EntityBlock {
    public TotemBaseBlock() {
        super(BlockBehaviour.Properties.of()
            .strength(50.0f, 1200.0f) // Same as obsidian
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()
            .mapColor(net.minecraft.world.level.material.MapColor.STONE)
        );
    }

    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new TotemBaseBlockEntity(pos, state);
    }
    
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockEntityType<T> type) {
        return type == ModRegistry.TOTEM_BASE_ENTITY.get() ? 
            (world, pos, blockState, entity) -> ((TotemBaseBlockEntity) entity).tick() : null;
    }

    @Override
    public void setPlacedBy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable LivingEntity placer, @Nonnull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        
        if (placer instanceof ServerPlayer player && !level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            
            if (blockEntity instanceof TotemBaseBlockEntity totemEntity) {
                // Get the DataManager instance properly
                DataManager dataManager = DataManager.get((ServerLevel)level);
                if (dataManager != null) {
                    Guild playerGuild = dataManager.getPlayerGuild(player.getUUID());
                    if (playerGuild != null) {
                        totemEntity.setGuild(playerGuild.getId());
                    }
                }
            }
        }
    }

    @Override
    @SuppressWarnings("deprecated")
    public void onRemove(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TotemBaseBlockEntity) {
                ((TotemBaseBlockEntity) blockEntity).onBreak();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
    
    @Override
    public void playerWillDestroy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull Player player) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TotemBaseBlockEntity) {
            ((TotemBaseBlockEntity) blockEntity).onBreak();
        }
        super.playerWillDestroy(level, pos, state, player);
    }
}