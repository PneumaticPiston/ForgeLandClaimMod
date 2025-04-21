package com.landclaim.team;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;

public class Team {
    private final String name;
    private UUID id;
    private final Set<UUID> members;
    private UUID owner;
    private int powerPoints;
    private final Map<UUID, TeamRole> memberRoles;

    public Team(String name, ServerPlayer owner) {
        this.name = name;
        this.id = UUID.randomUUID();
        this.members = new HashSet<>();
        this.owner = owner.getUUID();
        this.members.add(owner.getUUID());
        this.powerPoints = 0;
        this.memberRoles = new HashMap<>();
        this.memberRoles.put(owner.getUUID(), TeamRole.OWNER);
    }

    public String getName() {
        return name;
    }

    public UUID getId() {
        return id;
    }

    public Set<UUID> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    public boolean isMember(UUID playerId) {
        return members.contains(playerId);
    }

    public boolean isOwner(UUID playerId) {
        return owner.equals(playerId);
    }

    public TeamRole getMemberRole(UUID playerId) {
        return memberRoles.getOrDefault(playerId, TeamRole.NONE);
    }

    public void addMember(ServerPlayer player) {
        members.add(player.getUUID());
        memberRoles.put(player.getUUID(), TeamRole.MEMBER);
    }

    public void removeMember(UUID playerId) {
        members.remove(playerId);
        memberRoles.remove(playerId);
    }

    public int getPowerPoints() {
        return powerPoints;
    }

    public void addPowerPoints(int points) {
        this.powerPoints += points;
    }

    public boolean spendPowerPoints(int points) {
        if (this.powerPoints >= points) {
            this.powerPoints -= points;
            return true;
        }
        return false;
    }

    // Save team data to NBT
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", name);
        tag.putUUID("id", id);
        tag.putUUID("owner", owner);
        tag.putInt("powerPoints", powerPoints);
        
        CompoundTag membersTag = new CompoundTag();
        members.forEach(uuid -> membersTag.putBoolean(uuid.toString(), true));
        tag.put("members", membersTag);

        CompoundTag rolesTag = new CompoundTag();
        memberRoles.forEach((uuid, role) -> rolesTag.putInt(uuid.toString(), role.ordinal()));
        tag.put("roles", rolesTag);

        return tag;
    }

    // Load team data from NBT
    public static Team load(CompoundTag tag) {
        Team team = new Team(tag.getString("name"), null);
        team.id = tag.getUUID("id");
        team.owner = tag.getUUID("owner");
        team.powerPoints = tag.getInt("powerPoints");

        CompoundTag membersTag = tag.getCompound("members");
        membersTag.getAllKeys().forEach(key -> team.members.add(UUID.fromString(key)));

        CompoundTag rolesTag = tag.getCompound("roles");
        rolesTag.getAllKeys().forEach(key -> {
            UUID memberId = UUID.fromString(key);
            TeamRole role = TeamRole.values()[rolesTag.getInt(key)];
            team.memberRoles.put(memberId, role);
        });

        return team;
    }
}