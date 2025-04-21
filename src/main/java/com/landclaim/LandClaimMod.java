package com.landclaim;

import com.landclaim.data.DataManager;
import com.landclaim.registry.ModRegistry;
import com.landclaim.team.Team;
import com.landclaim.command.ClaimCommand;
import com.landclaim.command.TeamCommand;
import com.landclaim.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.config.ModConfig.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(LandClaimMod.MOD_ID)
public class LandClaimMod {
    public static final String MOD_ID = "landclaim";
    private static final Logger LOGGER = LogManager.getLogger();

    public LandClaimMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        // Register mod content
        ModRegistry.BLOCKS.register(modEventBus);
        ModRegistry.ITEMS.register(modEventBus);
        ModRegistry.BLOCK_ENTITIES.register(modEventBus);
        
        // Register config
        ModConfig.register();
        modEventBus.addListener(this::onConfigLoad);
        modEventBus.addListener(this::onConfigReload);
        
        // Register setup events
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::clientSetup);
        
        // Register event handlers
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, this::onBlockBreak);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, this::onBlockPlace);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, this::onRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(this::onCommandsRegister);
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("LandClaim Mod: Initializing");
        event.enqueueWork(() -> {
            DataManager.init();
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("LandClaim Mod: Client Setup");
    }

    private void onCommandsRegister(RegisterCommandsEvent event) {
        LOGGER.info("LandClaim Mod: Registering commands");
        ClaimCommand.register(event.getDispatcher());
        TeamCommand.register(event.getDispatcher());
    }

    private void onConfigLoad(final ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == ModConfig.SPEC) {
            ModConfig.updateClaimCost();
        }
    }

    private void onConfigReload(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == ModConfig.SPEC) {
            ModConfig.updateClaimCost();
        }
    }

    private void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        
        ChunkPos chunkPos = new ChunkPos(event.getPos());
        Team team = DataManager.getTeamForChunk(chunkPos);
        
        if (team != null && !team.isMember(player.getUUID())) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.literal("§cYou cannot break blocks in this claimed territory!"));
        }
    }

    private void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        ChunkPos chunkPos = new ChunkPos(event.getPos());
        Team team = DataManager.getTeamForChunk(chunkPos);
        
        if (team != null && !team.isMember(player.getUUID())) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.literal("§cYou cannot place blocks in this claimed territory!"));
        }
    }

    private void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        BlockPos pos = event.getPos();
        ChunkPos chunkPos = new ChunkPos(pos);
        Team team = DataManager.getTeamForChunk(chunkPos);
        
        if (team != null && !team.isMember(player.getUUID())) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.literal("§cYou cannot interact with blocks in this claimed territory!"));
        }
    }
}