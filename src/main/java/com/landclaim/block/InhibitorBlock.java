package com.landclaim.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class InhibitorBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 12, 14);
    
    public InhibitorBlock() {
        super(BlockBehaviour.Properties.of()
            .strength(2.0f, 3.0f)
            .requiresCorrectToolForDrops());
    }
    
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}