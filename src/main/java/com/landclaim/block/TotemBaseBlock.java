package com.landclaim.block;

import com.landclaim.block.entity.TotemBaseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TotemBaseBlock extends Block implements EntityBlock {
    public TotemBaseBlock() {
        super(Properties.of(BlockBehaviour.Properties.STONE)
            .strength(3.0f, 6.0f)
            .requiresCorrectToolForDrops()
        );
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TotemBaseBlockEntity(pos, state);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TotemBaseBlockEntity) {
                ((TotemBaseBlockEntity) blockEntity).onBreak();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}