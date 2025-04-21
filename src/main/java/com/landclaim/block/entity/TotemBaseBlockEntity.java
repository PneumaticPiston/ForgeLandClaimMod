package com.landclaim.block.entity;

import com.landclaim.claim.TerritoryChunk;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TotemBaseBlockEntity extends BlockEntity {
    private UUID teamId;
    private final List<MobEffect> activeEffects;
    private int powerPointsGenerated;
    private static final int EFFECT_RADIUS = 48; // 3 chunks worth

    public TotemBaseBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.BEACON, pos, state); // Using beacon type temporarily, will need custom type
        this.activeEffects = new ArrayList<>();
        this.powerPointsGenerated = 0;
    }

    public void setTeam(UUID teamId) {
        this.teamId = teamId;
    }

    public UUID getTeam() {
        return teamId;
    }

    public void addEffect(MobEffect effect) {
        if (!activeEffects.contains(effect)) {
            activeEffects.add(effect);
            setChanged();
        }
    }

    public void removeEffect(MobEffect effect) {
        activeEffects.remove(effect);
        setChanged();
    }

    private void applyEffectsToPlayers() {
        if (level == null || teamId == null || activeEffects.isEmpty()) return;

        AABB effectBox = new AABB(worldPosition).inflate(EFFECT_RADIUS);
        List<Player> players = level.getEntitiesOfClass(Player.class, effectBox);

        for (Player player : players) {
            // TODO: Check if player is in same team
            for (MobEffect effect : activeEffects) {
                player.addEffect(new MobEffectInstance(effect, 219, 0, true, false));
            }
        }
    }

    public void tick() {
        if (level != null && !level.isClientSide) {
            applyEffectsToPlayers();
            generatePowerPoints();
        }
    }

    private void generatePowerPoints() {
        // TODO: Implement power point generation based on totem structure
        powerPointsGenerated++;
        if (powerPointsGenerated >= 1200) { // 1 minute at 20 ticks/second
            // TODO: Add points to team
            powerPointsGenerated = 0;
            setChanged();
        }
    }

    public void onBreak() {
        // TODO: Handle totem break - remove effects, notify team, etc.
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("TeamId")) {
            teamId = tag.getUUID("TeamId");
        }
        powerPointsGenerated = tag.getInt("PowerPoints");
        // TODO: Load active effects
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (teamId != null) {
            tag.putUUID("TeamId", teamId);
        }
        tag.putInt("PowerPoints", powerPointsGenerated);
        // TODO: Save active effects
    }
}