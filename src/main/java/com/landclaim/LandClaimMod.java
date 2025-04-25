package com.landclaim;

import com.landclaim.claim.TerritoryChunk;
import com.landclaim.claim.TerritoryType;
import com.landclaim.data.DataManager;
import com.landclaim.registry.ModRegistry;
import com.landclaim.guild.Guild;
import com.landclaim.command.ClaimCommand;
import com.landclaim.command.GuildCommand;
import com.landclaim.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraftforge.fml.event.config.ModConfigEvent;
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
        ModRegistry.CREATIVE_MODE_TABS.register(modEventBus);
        
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
        GuildCommand.register(event.getDispatcher());
    }

    private void onConfigLoad(final ModConfigEvent event) {
        ModConfig.updateClaimCost();
    }

    private void onConfigReload(final ModConfigEvent event) {
        ModConfig.updateClaimCost();
    }

    private void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        
        ChunkPos chunkPos = new ChunkPos(event.getPos());
        DataManager manager = DataManager.get((ServerLevel) player.level());
        TerritoryChunk territory = manager.getTerritoryAt(chunkPos, player.level().dimension());
        
        if (territory == null) return;
        
        Guild guild = manager.getGuild(territory.getOwnerId());
        
        // Allow members of the guild that owns the chunk to break blocks
        if (guild != null && guild.isMember(player.getUUID())) {
            return;
        }
        
        // Only prevent breaking in Settlement territories for non-guild members
        if (territory.getType() == TerritoryType.SETTLEMENT) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.literal("§cYou cannot break blocks in this settlement!"));
        }
    }

    private void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        ChunkPos chunkPos = new ChunkPos(event.getPos());
        DataManager manager = DataManager.get((ServerLevel) player.level());
        TerritoryChunk territory = manager.getTerritoryAt(chunkPos, player.level().dimension());
        
        if (territory == null) return;
        
        Guild guild = manager.getGuild(territory.getOwnerId());
        
        // Allow members of the guild that owns the chunk to place blocks
        if (guild != null && guild.isMember(player.getUUID())) {
            return;
        }
        
        // Only prevent placing in Settlement territories for non-guild members
        if (territory.getType() == TerritoryType.SETTLEMENT) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.literal("§cYou cannot place blocks in this settlement!"));
        }
    }

    private void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        BlockPos pos = event.getPos();
        ChunkPos chunkPos = new ChunkPos(pos);
        DataManager manager = DataManager.get((ServerLevel) player.level());
        TerritoryChunk territory = manager.getTerritoryAt(chunkPos, player.level().dimension());
        
        if (territory == null) return;
        
        Guild guild = manager.getGuild(territory.getOwnerId());
        
        // Allow members of the guild that owns the chunk to interact with blocks
        if (guild != null && guild.isMember(player.getUUID())) {
            return;
        }
        
        // Only prevent interactions in Settlement territories for non-guild members
        if (territory.getType() == TerritoryType.SETTLEMENT) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.literal("§cYou cannot interact with blocks in this settlement!"));
        }
    }
}