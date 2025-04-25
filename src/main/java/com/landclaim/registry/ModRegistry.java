package com.landclaim.registry;

import com.landclaim.LandClaimMod;
import com.landclaim.block.TotemBaseBlock;
import com.landclaim.block.EnhancerBlock;
import com.landclaim.block.HeightExtenderBlock;
import com.landclaim.block.InhibitorBlock;
import com.landclaim.block.InventoryKeeperBlock;
import com.landclaim.block.GuildDisplayBlock;
import com.landclaim.block.PotionEffectBlock;
import com.landclaim.block.entity.TotemBaseBlockEntity;
import com.landclaim.block.entity.GuildDisplayBlockEntity;
import com.landclaim.item.ChunkViewerItem;
import com.landclaim.item.GuildStatsItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, LandClaimMod.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, LandClaimMod.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, LandClaimMod.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LandClaimMod.MOD_ID);
    
    // Block Items (declared early so they can be referenced before their implementation)
    public static final RegistryObject<Item> TOTEM_BASE_ITEM = ITEMS.register("totem_base",
            () -> new BlockItem(TOTEM_BASE.get(), new Item.Properties()));
                    
    // Creative Tab
    public static final RegistryObject<CreativeModeTab> LANDCLAIM_TAB = CREATIVE_MODE_TABS.register("landclaim_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(TOTEM_BASE_ITEM.get()))
                    .title(Component.translatable("itemGroup.landclaim"))
                    .displayItems((parameters, output) -> {
                        // Add all items from the landclaim namespace
                        ITEMS.getEntries().forEach(item -> output.accept(item.get()));
                    })
                    .build());

    // Blocks
    public static final RegistryObject<Block> TOTEM_BASE = BLOCKS.register("totem_base",
            TotemBaseBlock::new);
            
    public static final RegistryObject<Block> ENHANCER_BLOCK = BLOCKS.register("enhancer_block",
            EnhancerBlock::new);
            
    public static final RegistryObject<Block> INHIBITOR_BLOCK = BLOCKS.register("inhibitor_block",
            InhibitorBlock::new);
            
    public static final RegistryObject<Block> INVENTORY_KEEPER_BLOCK = BLOCKS.register("inventory_keeper_block",
            InventoryKeeperBlock::new);
            
    public static final RegistryObject<Block> GUILD_DISPLAY_BLOCK = BLOCKS.register("guild_display_block",
            GuildDisplayBlock::new);
            
    public static final RegistryObject<Block> HEIGHT_EXTENDER_DIAMOND = BLOCKS.register("height_extender_diamond",
            () -> new HeightExtenderBlock(4));
            
    public static final RegistryObject<Block> HEIGHT_EXTENDER_IRON = BLOCKS.register("height_extender_iron",
            () -> new HeightExtenderBlock(2));

    // Block Items (remaining items)
            
    public static final RegistryObject<Item> ENHANCER_BLOCK_ITEM = ITEMS.register("enhancer_block",
            () -> new BlockItem(ENHANCER_BLOCK.get(), new Item.Properties()));
            
    public static final RegistryObject<Item> INHIBITOR_BLOCK_ITEM = ITEMS.register("inhibitor_block",
            () -> new BlockItem(INHIBITOR_BLOCK.get(), new Item.Properties()));
            
    public static final RegistryObject<Item> INVENTORY_KEEPER_BLOCK_ITEM = ITEMS.register("inventory_keeper_block",
            () -> new BlockItem(INVENTORY_KEEPER_BLOCK.get(), new Item.Properties()));
            
    public static final RegistryObject<Item> GUILD_DISPLAY_BLOCK_ITEM = ITEMS.register("guild_display_block",
            () -> new BlockItem(GUILD_DISPLAY_BLOCK.get(), new Item.Properties()));
            
    public static final RegistryObject<Item> HEIGHT_EXTENDER_DIAMOND_ITEM = ITEMS.register("height_extender_diamond",
            () -> new BlockItem(HEIGHT_EXTENDER_DIAMOND.get(), new Item.Properties()));
            
    public static final RegistryObject<Item> HEIGHT_EXTENDER_IRON_ITEM = ITEMS.register("height_extender_iron",
            () -> new BlockItem(HEIGHT_EXTENDER_IRON.get(), new Item.Properties()));

    // Items
    public static final RegistryObject<Item> CHUNK_VIEWER = ITEMS.register("chunk_viewer",
            () -> new ChunkViewerItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> GUILD_STATS = ITEMS.register("guild_stats",
            () -> new GuildStatsItem(new Item.Properties().stacksTo(1)));

    // Block Entities
    public static final RegistryObject<BlockEntityType<TotemBaseBlockEntity>> TOTEM_BASE_ENTITY = BLOCK_ENTITIES.register(
            "totem_base",
            () -> BlockEntityType.Builder.of(TotemBaseBlockEntity::new, TOTEM_BASE.get()).build(null));
            
    public static final RegistryObject<BlockEntityType<GuildDisplayBlockEntity>> GUILD_DISPLAY_ENTITY = BLOCK_ENTITIES.register(
            "guild_display",
            () -> BlockEntityType.Builder.of(GuildDisplayBlockEntity::new, GUILD_DISPLAY_BLOCK.get()).build(null));
}