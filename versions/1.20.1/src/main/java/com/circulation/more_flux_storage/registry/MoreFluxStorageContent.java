package com.circulation.more_flux_storage.registry;

import com.circulation.more_flux_storage.MoreFluxStorage;
import com.circulation.more_flux_storage.block.BlockEnergyPylonFlux;
import com.circulation.more_flux_storage.block.BlockInductionPortFlux;
import com.circulation.more_flux_storage.blockentity.TileEnergyPylonFlux;
import com.circulation.more_flux_storage.blockentity.TileInductionPortFlux;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class MoreFluxStorageContent {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MoreFluxStorage.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MoreFluxStorage.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MoreFluxStorage.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MoreFluxStorage.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MoreFluxStorage.MOD_ID);

    public static final RegistryObject<BlockEnergyPylonFlux> ENERGY_PYLON_FLUX = BLOCKS.register("energy_pylon_flux", BlockEnergyPylonFlux::new);
    public static final RegistryObject<Item> ENERGY_PYLON_FLUX_ITEM = ITEMS.register("energy_pylon_flux", () -> new BlockItem(ENERGY_PYLON_FLUX.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<TileEnergyPylonFlux>> ENERGY_PYLON_FLUX_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
        "energy_pylon_flux",
        () -> BlockEntityType.Builder.of(TileEnergyPylonFlux::new, ENERGY_PYLON_FLUX.get()).build(null)
    );
    public static final RegistryObject<BlockInductionPortFlux> INDUCTION_PORT_FLUX = BLOCKS.register("induction_port_flux", BlockInductionPortFlux::new);
    public static final RegistryObject<Item> INDUCTION_PORT_FLUX_ITEM = ITEMS.register("induction_port_flux", () -> new BlockItem(INDUCTION_PORT_FLUX.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<TileInductionPortFlux>> INDUCTION_PORT_FLUX_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
        "induction_port_flux",
        () -> BlockEntityType.Builder.of(TileInductionPortFlux::new, INDUCTION_PORT_FLUX.get()).build(null)
    );

    private MoreFluxStorageContent() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITY_TYPES.register(modBus);
        MENU_TYPES.register(modBus);
        CREATIVE_MODE_TABS.register(modBus);
    }
}