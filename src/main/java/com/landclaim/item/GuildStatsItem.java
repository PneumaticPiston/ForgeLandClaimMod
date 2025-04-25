package com.landclaim.item;

import com.landclaim.data.DataManager;
import com.landclaim.guild.Guild;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;

public class GuildStatsItem extends Item {
    public GuildStatsItem(Properties properties) {
        super(properties);
    }

    @Override
    public @Nonnull InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            ServerLevel serverLevel = serverPlayer.serverLevel();
            DataManager manager = DataManager.get(serverLevel);
            Guild guild = manager.getPlayerGuild(player.getUUID());
            
            if (guild == null) {
                player.displayClientMessage(
                    Component.literal("§cYou are not a member of any guild."), 
                    false
                );
                return InteractionResultHolder.fail(itemstack);
            }
            
            // Display guild information
            List<Component> messages = new ArrayList<>();
            messages.add(Component.literal("§6=== Guild Information ==="));
            messages.add(Component.literal("§eGuild Name: §f" + guild.getName()));
            messages.add(Component.literal("§ePower Points: §f" + guild.getPowerPoints()));
            messages.add(Component.literal("§eClaimed Chunks: §f" + guild.getClaimedChunks()));
            messages.add(Component.literal("§eMembers: §f" + guild.getMembers().size()));
            
            // Display active effects from totems if any
            messages.add(Component.literal("§6=== Active Totem Effects ==="));
            // No effects tracked yet
            
            // In a full implementation, you'd store and track totem effects at the guild level
            // For this implementation, we'll just say "None" since there's no effect tracking yet
            messages.add(Component.literal("§7None"));
            
            // Display all messages
            for (Component message : messages) {
                player.displayClientMessage(message, false);
            }
        }

        return InteractionResultHolder.success(itemstack);
    }
}