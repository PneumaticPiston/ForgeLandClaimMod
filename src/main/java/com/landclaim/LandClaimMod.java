package com.landclaim;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(LandClaimMod.MOD_ID)
public class LandClaimMod {
    public static final String MOD_ID = "landclaim";
    private static final Logger LOGGER = LogManager.getLogger();

    public LandClaimMod() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        
        bus.addListener(this::setup);
        bus.addListener(this::clientSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("LandClaim Mod: Initializing");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("LandClaim Mod: Client Setup");
    }
}