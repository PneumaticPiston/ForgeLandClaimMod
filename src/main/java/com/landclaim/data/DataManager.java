package com.landclaim.data;

import com.landclaim.claim.TerritoryChunk;
import com.landclaim.team.Team;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataManager extends SavedData {
    private static final String DATA_NAME = "landclaim_data";
    private final Map<UUID, Team> teams;
    private final Map<String, TerritoryChunk> territories; // Key format: "dimension;x;z"
    private static DataManager instance;

    public DataManager() {
        this.teams = new HashMap<>();
        this.territories = new HashMap<>();
    }

    public static DataManager get(ServerLevel level) {
        if (instance == null) {
            instance = level.getDataStorage().computeIfAbsent(
                DataManager::load,
                DataManager::new,
                DATA_NAME
            );
        }
        return instance;
    }

    public Team getTeam(UUID teamId) {
        return teams.get(teamId);
    }

    public Team getTeam(String teamName) {
        return teams.values().stream()
            .filter(team -> team.getName().equals(teamName))
            .findFirst()
            .orElse(null);
    }

    public Team getPlayerTeam(UUID playerId) {
        return teams.values().stream()
            .filter(team -> team.isMember(playerId))
            .findFirst()
            .orElse(null);
    }

    public void addTeam(Team team) {
        teams.put(team.getId(), team);
        setDirty();
    }

    public void removeTeam(UUID teamId) {
        teams.remove(teamId);
        setDirty();
    }

    public Collection<Team> getTeams() {
        return Collections.unmodifiableCollection(teams.values());
    }

    public TerritoryChunk getTerritory(ResourceKey<Level> dimension, ChunkPos pos) {
        String key = getTerritoryKey(dimension, pos);
        return territories.get(key);
    }

    public void setTerritory(TerritoryChunk territory) {
        String key = getTerritoryKey(territory.getDimension(), territory.getCenterChunk());
        territories.put(key, territory);
        setDirty();
    }

    private String getTerritoryKey(ResourceKey<Level> dimension, ChunkPos pos) {
        return dimension.location() + ";" + pos.x + ";" + pos.z;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag teamsTag = new ListTag();
        teams.values().forEach(team -> {
            CompoundTag teamTag = team.save();
            teamsTag.add(teamTag);
        });
        tag.put("teams", teamsTag);

        ListTag territoriesTag = new ListTag();
        territories.values().forEach(territory -> {
            CompoundTag territoryTag = territory.save();
            territoriesTag.add(territoryTag);
        });
        tag.put("territories", territoriesTag);

        return tag;
    }

    public static DataManager load(CompoundTag tag) {
        DataManager manager = new DataManager();

        ListTag teamsTag = tag.getList("teams", 10);
        teamsTag.forEach(element -> {
            CompoundTag teamTag = (CompoundTag) element;
            Team team = Team.load(teamTag);
            manager.teams.put(team.getId(), team);
        });

        ListTag territoriesTag = tag.getList("territories", 10);
        territoriesTag.forEach(element -> {
            CompoundTag territoryTag = (CompoundTag) element;
            TerritoryChunk territory = TerritoryChunk.load(territoryTag, Level.OVERWORLD); // TODO: Handle different dimensions
            String key = manager.getTerritoryKey(territory.getDimension(), territory.getCenterChunk());
            manager.territories.put(key, territory);
        });

        return manager;
    }
}