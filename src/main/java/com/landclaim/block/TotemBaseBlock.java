package com.landclaim.block;

import com.landclaim.block.entity.TotemBaseBlockEntity;
import com.landclaim.registry.ModRegistry;
import net.minecraft.core.BlockPos;
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

import javax.annotation.Nullable;

public class TotemBaseBlock extends Block implements EntityBlock {
    public TotemBaseBlock() {
        super(BlockBehaviour.Properties.of()
            .strength(50.0f, 1200.0f) // Same as obsidian
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()
        );
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TotemBaseBlockEntity(pos, state);
    }
    
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModRegistry.TOTEM_BASE_ENTITY.get() ? 
            (world, pos, blockState, entity) -> ((TotemBaseBlockEntity) entity).tick() : null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        
        if (placer instanceof Player && !level.isClientSide) {
            Player player = (Player) placer;
            BlockEntity blockEntity = level.getBlockEntity(pos);
            
            if (blockEntity instanceof TotemBaseBlockEntity) {
                TotemBaseBlockEntity totemEntity = (TotemBaseBlockEntity) blockEntity;
                
                // TODO: Get player's guild and set it for the totem
                // UUID guildId = DataManager.INSTANCE.getPlayerGuild(player.getUUID());
                // if (guildId != null) {
                //    totemEntity.setGuild(guildId);
                // }
            }
        }
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
    
    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TotemBaseBlockEntity) {
            ((TotemBaseBlockEntity) blockEntity).onBreak();
        }
        super.playerWillDestroy(level, pos, state, player);
    }
}