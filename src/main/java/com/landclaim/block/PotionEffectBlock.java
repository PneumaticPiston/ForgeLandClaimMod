package com.landclaim.block;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class PotionEffectBlock extends Block {
    private final MobEffect effect;
    
    public PotionEffectBlock(MobEffect effect) {
        super(BlockBehaviour.Properties.of()
            .strength(3.0f, 6.0f)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()
            .mapColor(MapColor.COLOR_PURPLE)
        );
        this.effect = effect;
    }
    
    public MobEffect getEffect() {
        return effect;
    }
}