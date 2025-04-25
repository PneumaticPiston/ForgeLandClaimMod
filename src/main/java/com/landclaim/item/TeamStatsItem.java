package com.landclaim.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class TeamStatsItem extends Item {
    public TeamStatsItem(Properties properties) {
        super(properties);
    }

    @Override
    public @Nonnull InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        if (!level.isClientSide) {
            // TODO: Implement team stats display
            // This should show:
            // - Team name
            // - Power points
            // - Number of claimed chunks
            // - List of team members
            // - Active totem effects
            player.displayClientMessage(
                Component.literal("Opening team stats..."), 
                true
            );
        }

        return InteractionResultHolder.success(itemstack);
    }
}