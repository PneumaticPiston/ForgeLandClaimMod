package com.landclaim.command;

import com.landclaim.data.DataManager;
import com.landclaim.team.Team;
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

public class TeamCommand {
    private static final SimpleCommandExceptionType ERROR_TEAM_EXISTS = 
        new SimpleCommandExceptionType(Component.translatable("commands.team.create.failed.exists"));
    private static final SimpleCommandExceptionType ERROR_TEAM_NOT_FOUND =
        new SimpleCommandExceptionType(Component.translatable("commands.team.join.failed.not_found"));
    private static final SimpleCommandExceptionType ERROR_ALREADY_IN_TEAM =
        new SimpleCommandExceptionType(Component.translatable("commands.team.join.failed.already_in_team"));
    private static final SimpleCommandExceptionType ERROR_NOT_IN_TEAM =
        new SimpleCommandExceptionType(Component.translatable("commands.team.leave.failed.not_in_team"));
    private static final SimpleCommandExceptionType ERROR_NOT_OWNER =
        new SimpleCommandExceptionType(Component.translatable("commands.team.promote.failed.not_owner"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("team")
            .then(Commands.literal("create")
                .then(Commands.argument("name", StringArgumentType.word())
                    .executes(ctx -> createTeam(ctx.getSource(), StringArgumentType.getString(ctx, "name")))))
            .then(Commands.literal("invite")
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(ctx -> invitePlayer(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
            .then(Commands.literal("join")
                .then(Commands.argument("name", StringArgumentType.word())
                    .executes(ctx -> joinTeam(ctx.getSource(), StringArgumentType.getString(ctx, "name")))))
            .then(Commands.literal("leave")
                .executes(ctx -> leaveTeam(ctx.getSource())))
            .then(Commands.literal("promote")
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(ctx -> promotePlayer(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"))))));
    }

    private static int createTeam(CommandSourceStack source, String teamName) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();
        DataManager manager = DataManager.get(level);
        
        if (manager.getTeam(teamName) != null) {
            throw ERROR_TEAM_EXISTS.create();
        }

        Team team = new Team(teamName, player.getUUID());
        manager.addTeam(team);
        source.sendSuccess(() -> Component.translatable("commands.team.create.success", teamName), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int invitePlayer(CommandSourceStack source, ServerPlayer target) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();
        DataManager manager = DataManager.get(level);
        Team team = manager.getPlayerTeam(player.getUUID());
        
        if (team == null) {
            throw ERROR_NOT_IN_TEAM.create();
        }
        
        if (!team.isOwner(player.getUUID())) {
            throw ERROR_NOT_OWNER.create();
        }

        team.invitePlayer(target.getUUID());
        source.sendSuccess(() -> Component.translatable("commands.team.invite.success", target.getDisplayName()), true);
        target.sendSystemMessage(Component.translatable("commands.team.invite.received", team.getName()));
        return Command.SINGLE_SUCCESS;
    }

    private static int joinTeam(CommandSourceStack source, String teamName) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();
        DataManager manager = DataManager.get(level);
        Team team = manager.getTeam(teamName);
        
        if (team == null) {
            throw ERROR_TEAM_NOT_FOUND.create();
        }
        
        if (manager.getPlayerTeam(player.getUUID()) != null) {
            throw ERROR_ALREADY_IN_TEAM.create();
        }

        if (!team.isInvited(player.getUUID())) {
            source.sendFailure(Component.translatable("commands.team.join.failed.not_invited"));
            return 0;
        }

        team.addMember(player.getUUID());
        source.sendSuccess(() -> Component.translatable("commands.team.join.success", teamName), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int leaveTeam(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();
        DataManager manager = DataManager.get(level);
        Team team = manager.getPlayerTeam(player.getUUID());
        
        if (team == null) {
            throw ERROR_NOT_IN_TEAM.create();
        }

        team.removeMember(player.getUUID());
        source.sendSuccess(() -> Component.translatable("commands.team.leave.success", team.getName()), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int promotePlayer(CommandSourceStack source, ServerPlayer target) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();
        DataManager manager = DataManager.get(level);
        Team team = manager.getPlayerTeam(player.getUUID());
        
        if (team == null) {
            throw ERROR_NOT_IN_TEAM.create();
        }
        
        if (!team.isOwner(player.getUUID())) {
            throw ERROR_NOT_OWNER.create();
        }

        if (!team.isMember(target.getUUID())) {
            source.sendFailure(Component.translatable("commands.team.promote.failed.not_in_team"));
            return 0;
        }

        team.setOwner(target.getUUID());
        source.sendSuccess(() -> Component.translatable("commands.team.promote.success", target.getDisplayName()), true);
        target.sendSystemMessage(Component.translatable("commands.team.promote.received", team.getName()));
        return Command.SINGLE_SUCCESS;
    }
}