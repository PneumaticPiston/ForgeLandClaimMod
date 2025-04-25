package com.landclaim.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GuildStatsItem extends Item {
    public GuildStatsItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        if (!level.isClientSide) {
            // TODO: Implement guild stats display
            // This should show:
            // - Guild name
            // - Power points
            // - Number of claimed chunks
            // - List of guild members
            // - Active totem effects
            player.displayClientMessage(
                Component.literal("Opening guild stats..."), 
                true
            );
        }

        return InteractionResultHolder.success(itemstack);
    }
}