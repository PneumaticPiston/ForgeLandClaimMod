package com.landclaim.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;

public class ChunkViewerItem extends Item {
    public ChunkViewerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        if (!level.isClientSide) {
            ChunkPos playerChunk = new ChunkPos(player.blockPosition());
            // TODO: Get territory info and display boundaries
            // This will need client-side particle effects to show boundaries
            player.displayClientMessage(
                Component.literal("Viewing territory at: " + playerChunk.x + ", " + playerChunk.z), 
                true
            );
        }

        return InteractionResultHolder.success(itemstack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // Always show enchantment glint
    }
}