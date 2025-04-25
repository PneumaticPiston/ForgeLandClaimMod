package com.landclaim.command;

import com.landclaim.data.DataManager;
import com.landclaim.guild.Guild;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;

public class GuildCommand {
    private static final SimpleCommandExceptionType ERROR_GUILD_EXISTS = 
        new SimpleCommandExceptionType(Component.translatable("commands.guild.create.failed.exists"));
    private static final SimpleCommandExceptionType ERROR_GUILD_NOT_FOUND =
        new SimpleCommandExceptionType(Component.translatable("commands.guild.join.failed.not_found"));
    private static final SimpleCommandExceptionType ERROR_ALREADY_IN_GUILD =
        new SimpleCommandExceptionType(Component.translatable("commands.guild.join.failed.already_in_guild"));
    private static final SimpleCommandExceptionType ERROR_NOT_IN_GUILD =
        new SimpleCommandExceptionType(Component.translatable("commands.guild.leave.failed.not_in_guild"));
    private static final SimpleCommandExceptionType ERROR_NOT_OWNER =
        new SimpleCommandExceptionType(Component.translatable("commands.guild.promote.failed.not_owner"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("guild")
            .then(Commands.literal("create")
                .then(Commands.argument("name", StringArgumentType.word())
                    .executes(ctx -> createGuild(ctx.getSource(), StringArgumentType.getString(ctx, "name")))))
            .then(Commands.literal("invite")
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(ctx -> invitePlayer(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
            .then(Commands.literal("join")
                .then(Commands.argument("name", StringArgumentType.word())
                    .executes(ctx -> joinGuild(ctx.getSource(), StringArgumentType.getString(ctx, "name")))))
            .then(Commands.literal("leave")
                .executes(ctx -> leaveGuild(ctx.getSource())))
            .then(Commands.literal("promote")
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(ctx -> promotePlayer(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"))))));
    }

    private static int createGuild(CommandSourceStack source, String guildName) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();
        DataManager manager = DataManager.get(level);
        
        if (manager.getGuild(guildName) != null) {
            throw ERROR_GUILD_EXISTS.create();
        }

        Guild guild = new Guild(guildName, player.getUUID());
        manager.addGuild(guild);
        source.sendSuccess(() -> Component.translatable("commands.guild.create.success", guildName), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int invitePlayer(CommandSourceStack source, ServerPlayer target) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();
        DataManager manager = DataManager.get(level);
        Guild guild = manager.getPlayerGuild(player.getUUID());
        
        if (guild == null) {
            throw ERROR_NOT_IN_GUILD.create();
        }
        
        if (!guild.isOwner(player.getUUID())) {
            throw ERROR_NOT_OWNER.create();
        }

        guild.invitePlayer(target.getUUID());
        source.sendSuccess(() -> Component.translatable("commands.guild.invite.success", target.getDisplayName()), true);
        target.sendSystemMessage(Component.translatable("commands.guild.invite.received", guild.getName()));
        return Command.SINGLE_SUCCESS;
    }

    private static int joinGuild(CommandSourceStack source, String guildName) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();
        DataManager manager = DataManager.get(level);
        Guild guild = manager.getGuild(guildName);
        
        if (guild == null) {
            throw ERROR_GUILD_NOT_FOUND.create();
        }
        
        if (manager.getPlayerGuild(player.getUUID()) != null) {
            throw ERROR_ALREADY_IN_GUILD.create();
        }

        if (!guild.isInvited(player.getUUID())) {
            source.sendFailure(Component.translatable("commands.guild.join.failed.not_invited"));
            return 0;
        }

        guild.addMember(player.getUUID());
        source.sendSuccess(() -> Component.translatable("commands.guild.join.success", guildName), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int leaveGuild(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();
        DataManager manager = DataManager.get(level);
        Guild guild = manager.getPlayerGuild(player.getUUID());
        
        if (guild == null) {
            throw ERROR_NOT_IN_GUILD.create();
        }

        guild.removeMember(player.getUUID());
        source.sendSuccess(() -> Component.translatable("commands.guild.leave.success", guild.getName()), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int promotePlayer(CommandSourceStack source, ServerPlayer target) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();
        DataManager manager = DataManager.get(level);
        Guild guild = manager.getPlayerGuild(player.getUUID());
        
        if (guild == null) {
            throw ERROR_NOT_IN_GUILD.create();
        }
        
        if (!guild.isOwner(player.getUUID())) {
            throw ERROR_NOT_OWNER.create();
        }

        if (!guild.isMember(target.getUUID())) {
            source.sendFailure(Component.translatable("commands.guild.promote.failed.not_in_guild"));
            return 0;
        }

        guild.setOwner(target.getUUID());
        source.sendSuccess(() -> Component.translatable("commands.guild.promote.success", target.getDisplayName()), true);
        target.sendSystemMessage(Component.translatable("commands.guild.promote.received", guild.getName()));
        return Command.SINGLE_SUCCESS;
    }
}