package com.landclaim.registry;

import com.landclaim.LandClaimMod;
import com.landclaim.block.TotemBaseBlock;
import com.landclaim.block.entity.TotemBaseBlockEntity;
import com.landclaim.item.ChunkViewerItem;
import com.landclaim.item.TeamStatsItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, LandClaimMod.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, LandClaimMod.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, LandClaimMod.MOD_ID);

    // Blocks
    public static final RegistryObject<Block> TOTEM_BASE = BLOCKS.register("totem_base",
            TotemBaseBlock::new);

    // Block Items
    public static final RegistryObject<Item> TOTEM_BASE_ITEM = ITEMS.register("totem_base",
            () -> new BlockItem(TOTEM_BASE.get(), new Item.Properties()));

    // Items
    public static final RegistryObject<Item> CHUNK_VIEWER = ITEMS.register("chunk_viewer",
            () -> new ChunkViewerItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> TEAM_STATS = ITEMS.register("team_stats",
            () -> new TeamStatsItem(new Item.Properties().stacksTo(1)));

    // Block Entities
    public static final RegistryObject<BlockEntityType<TotemBaseBlockEntity>> TOTEM_BASE_ENTITY = BLOCK_ENTITIES.register(
            "totem_base",
            () -> BlockEntityType.Builder.of(TotemBaseBlockEntity::new, TOTEM_BASE.get()).build(null));
}