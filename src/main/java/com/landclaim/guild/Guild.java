package com.landclaim.guild;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;

public class Guild {
    private final String name;
    private UUID id;
    private final Set<UUID> members;
    private UUID owner;
    private int powerPoints;
    private final Map<UUID, GuildRole> memberRoles;
    private int claimedChunks;
    private final Set<UUID> invitedPlayers = new HashSet<>();

    public Guild(String name, UUID ownerId) {
        this.name = name;
        this.id = UUID.randomUUID();
        this.members = new HashSet<>();
        this.owner = ownerId;
        this.members.add(ownerId);
        this.powerPoints = 0;
        this.memberRoles = new HashMap<>();
        this.memberRoles.put(ownerId, GuildRole.OWNER);
        this.claimedChunks = 0;
    }

    public Guild(String name, ServerPlayer owner) {
        this(name, owner.getUUID());
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

    public GuildRole getMemberRole(UUID playerId) {
        return memberRoles.getOrDefault(playerId, GuildRole.NONE);
    }

    public void addMember(ServerPlayer player) {
        addMember(player.getUUID());
    }

    public void addMember(UUID playerId) {
        members.add(playerId);
        memberRoles.put(playerId, GuildRole.MEMBER);
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

    public int getClaimedChunks() {
        return claimedChunks;
    }

    public void incrementClaimedChunks() {
        claimedChunks++;
    }

    public void decrementClaimedChunks() {
        if (claimedChunks > 0) {
            claimedChunks--;
        }
    }

    public void invitePlayer(UUID playerId) {
        invitedPlayers.add(playerId);
    }

    public boolean isInvited(UUID playerId) {
        return invitedPlayers.contains(playerId);
    }

    public void setOwner(UUID playerId) {
        if (members.contains(playerId)) {
            memberRoles.put(this.owner, GuildRole.MEMBER);
            this.owner = playerId;
            memberRoles.put(playerId, GuildRole.OWNER);
        }
    }

    // Save guild data to NBT
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", name);
        tag.putUUID("id", id);
        tag.putUUID("owner", owner);
        tag.putInt("powerPoints", powerPoints);
        tag.putInt("claimedChunks", claimedChunks);
        
        CompoundTag membersTag = new CompoundTag();
        members.forEach(uuid -> membersTag.putBoolean(uuid.toString(), true));
        tag.put("members", membersTag);

        CompoundTag rolesTag = new CompoundTag();
        memberRoles.forEach((uuid, role) -> rolesTag.putInt(uuid.toString(), role.ordinal()));
        tag.put("roles", rolesTag);

        return tag;
    }

    // Load guild data from NBT
    public static Guild load(CompoundTag tag) {
        Guild guild = new Guild(tag.getString("name"), (UUID)null);
        guild.id = tag.getUUID("id");
        guild.owner = tag.getUUID("owner");
        guild.powerPoints = tag.getInt("powerPoints");
        guild.claimedChunks = tag.getInt("claimedChunks");

        CompoundTag membersTag = tag.getCompound("members");
        membersTag.getAllKeys().forEach(key -> guild.members.add(UUID.fromString(key)));

        CompoundTag rolesTag = tag.getCompound("roles");
        rolesTag.getAllKeys().forEach(key -> {
            UUID memberId = UUID.fromString(key);
            GuildRole role = GuildRole.values()[rolesTag.getInt(key)];
            guild.memberRoles.put(memberId, role);
        });

        return guild;
    }
}