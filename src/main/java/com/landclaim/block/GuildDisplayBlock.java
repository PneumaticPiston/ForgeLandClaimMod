package com.landclaim.block;

import com.landclaim.block.entity.GuildDisplayBlockEntity;
import com.landclaim.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GuildDisplayBlock extends Block implements EntityBlock {
    public GuildDisplayBlock() {
        super(BlockBehaviour.Properties.of()
            .strength(3.0f, 5.0f)
            .requiresCorrectToolForDrops());
    }

    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new GuildDisplayBlockEntity(pos, state);
    }
    
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockEntityType<T> type) {
        return type == ModRegistry.GUILD_DISPLAY_ENTITY.get() ? 
            (world, pos, blockState, entity) -> ((GuildDisplayBlockEntity) entity).tick() : null;
    }

    @Override
    @SuppressWarnings("deprecated")
    public void onRemove(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof GuildDisplayBlockEntity) {
                ((GuildDisplayBlockEntity) blockEntity).onBreak();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
    
    @Override
    public void playerWillDestroy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull Player player) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof GuildDisplayBlockEntity) {
            ((GuildDisplayBlockEntity) blockEntity).onBreak();
        }
        super.playerWillDestroy(level, pos, state, player);
    }
}