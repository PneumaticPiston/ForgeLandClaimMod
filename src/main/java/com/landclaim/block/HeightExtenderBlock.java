package com.landclaim.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class HeightExtenderBlock extends Block {
    private final int heightIncrease;
    
    public HeightExtenderBlock(int heightIncrease) {
        super(BlockBehaviour.Properties.of()
            .strength(5.0f, 6.0f)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()
            .mapColor(heightIncrease == 4 ? MapColor.DIAMOND : MapColor.METAL)
        );
        this.heightIncrease = heightIncrease;
    }
    
    public int getHeightIncrease() {
        return heightIncrease;
    }
}