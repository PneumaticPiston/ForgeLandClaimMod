package com.landclaim.data;

import com.landclaim.claim.TerritoryChunk;
import com.landclaim.guild.Guild;
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
    private final Map<UUID, Guild> guilds;
    private final Map<String, TerritoryChunk> territories; // Key format: "dimension;x;z"
    private static DataManager instance;
    public static DataManager INSTANCE;

    public DataManager() {
        this.guilds = new HashMap<>();
        this.territories = new HashMap<>();
    }

    public static DataManager get(ServerLevel level) {
        if (instance == null) {
            instance = level.getDataStorage().computeIfAbsent(
                DataManager::load,
                DataManager::new,
                DATA_NAME
            );
            INSTANCE = instance;
        }
        return instance;
    }

    public Guild getGuild(UUID guildId) {
        return guilds.get(guildId);
    }

    public Guild getGuild(String guildName) {
        return guilds.values().stream()
            .filter(guild -> guild.getName().equals(guildName))
            .findFirst()
            .orElse(null);
    }

    public Guild getPlayerGuild(UUID playerId) {
        return guilds.values().stream()
            .filter(guild -> guild.isMember(playerId))
            .findFirst()
            .orElse(null);
    }

    public void addGuild(Guild guild) {
        guilds.put(guild.getId(), guild);
        setDirty();
    }

    public void removeGuild(UUID guildId) {
        guilds.remove(guildId);
        setDirty();
    }

    public Collection<Guild> getGuilds() {
        return Collections.unmodifiableCollection(guilds.values());
    }

    public TerritoryChunk getTerritory(ResourceKey<Level> dimension, ChunkPos pos) {
        String key = getTerritoryKey(dimension, pos);
        return territories.get(key);
    }

    public void setTerritory(TerritoryChunk territory) {
        String key = getTerritoryKey(territory.getDimension(), territory.getCenterChunk());
        territories.put(key, territory);
        
        // If this is a newly claimed territory, update connected territories
        if (territory.getType() == com.landclaim.claim.TerritoryType.CLAIMED && territory.getOwnerId() != null) {
            updateConnectedTerritories(territory);
        }
        
        setDirty();
    }
    
    /**
     * Updates the connected status of all territories owned by the same guild
     * A territory is connected if it's adjacent to another connected territory or contains a totem.
     * 
     * @param newTerritory The newly claimed territory that triggered the update
     */
    public void updateConnectedTerritories(TerritoryChunk newTerritory) {
        UUID ownerId = newTerritory.getOwnerId();
        if (ownerId == null) return;
        
        // Get all territories owned by this guild
        Map<String, TerritoryChunk> guildTerritories = new HashMap<>();
        for (Map.Entry<String, TerritoryChunk> entry : territories.entrySet()) {
            if (ownerId.equals(entry.getValue().getOwnerId())) {
                guildTerritories.put(entry.getKey(), entry.getValue());
            }
        }
        
        // First, mark all territories as not connected
        guildTerritories.values().forEach(t -> t.setConnectedToTotem(false));
        
        // Find all territories that contain a totem and mark them as connected
        guildTerritories.values().stream()
            .filter(t -> t.getTotemPos() != null)
            .forEach(t -> t.setConnectedToTotem(true));
            
        // Iteratively mark adjacent territories as connected
        boolean changes;
        do {
            changes = false;
            for (TerritoryChunk territory : guildTerritories.values()) {
                if (territory.isConnectedToTotem()) continue; // Already connected
                
                // Check if this territory is adjacent to any connected territory
                for (TerritoryChunk other : guildTerritories.values()) {
                    if (other.isConnectedToTotem() && territory.isAdjacentTo(other)) {
                        territory.setConnectedToTotem(true);
                        changes = true;
                        break;
                    }
                }
            }
        } while (changes);
        
        // Update all territories in the main map
        for (TerritoryChunk territory : guildTerritories.values()) {
            String key = getTerritoryKey(territory.getDimension(), territory.getCenterChunk());
            territories.put(key, territory);
        }
        
        setDirty();
    }
    
    /**
     * Updates territories when a chunk is unclaimed.
     * This will recalculate which territories are connected to a totem.
     * 
     * @param unclaimedTerritory The territory that was unclaimed
     */
    public void handleUnclaimedTerritory(TerritoryChunk unclaimedTerritory) {
        UUID previousOwnerId = unclaimedTerritory.getOwnerId();
        if (previousOwnerId == null) return;
        
        // Mark as wilderness
        unclaimedTerritory.setType(com.landclaim.claim.TerritoryType.WILDERNESS);
        unclaimedTerritory.setOwner(null);
        unclaimedTerritory.setTotemPos(null);
        unclaimedTerritory.setConnectedToTotem(false);
        
        // Save the unclaimed territory
        String key = getTerritoryKey(unclaimedTerritory.getDimension(), unclaimedTerritory.getCenterChunk());
        territories.put(key, unclaimedTerritory);
        
        // Find any territory owned by the same guild that has a totem
        TerritoryChunk totemTerritory = territories.values().stream()
            .filter(t -> previousOwnerId.equals(t.getOwnerId()) && t.getTotemPos() != null)
            .findFirst()
            .orElse(null);
            
        // If we found a territory with a totem, use it to recalculate connections
        if (totemTerritory != null) {
            updateConnectedTerritories(totemTerritory);
        }
        
        setDirty();
    }

    private String getTerritoryKey(ResourceKey<Level> dimension, ChunkPos pos) {
        return dimension.location() + ";" + pos.x + ";" + pos.z;
    }
    
    public TerritoryChunk getTerritoryAt(ChunkPos pos, ResourceKey<Level> dimension) {
        // First, try to get an exact match for this chunk
        TerritoryChunk exactMatch = getTerritory(dimension, pos);
        if (exactMatch != null) {
            return exactMatch;
        }
        
        // Otherwise, search for territories that contain this chunk
        for (TerritoryChunk territory : territories.values()) {
            if (territory.getDimension().equals(dimension) && territory.containsChunk(pos)) {
                return territory;
            }
        }
        
        return null;
    }
    
    public static void init() {
        // This method is called during mod initialization
        // It doesn't need to do anything as instances are created on demand
    }
    
    public static Guild getGuildForChunk(ChunkPos chunkPos) {
        if (instance == null) return null;
        
        // Search through all territories to find one that contains this chunk
        for (TerritoryChunk territory : instance.territories.values()) {
            if (territory.containsChunk(chunkPos)) {
                return instance.getGuild(territory.getOwnerId());
            }
        }
        
        return null;
    }
    

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag guildsTag = new ListTag();
        guilds.values().forEach(guild -> {
            CompoundTag guildTag = guild.save();
            guildsTag.add(guildTag);
        });
        tag.put("guilds", guildsTag);

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

        ListTag guildsTag = tag.getList("guilds", 10);
        guildsTag.forEach(element -> {
            CompoundTag guildTag = (CompoundTag) element;
            Guild guild = Guild.load(guildTag);
            manager.guilds.put(guild.getId(), guild);
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