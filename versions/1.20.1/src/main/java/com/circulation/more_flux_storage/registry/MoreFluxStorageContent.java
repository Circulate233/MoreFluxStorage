package com.circulation.more_flux_storage.registry;

import com.circulation.more_flux_storage.api.IFluxGuiConnector;
import com.circulation.more_flux_storage.MoreFluxStorageForge;
import com.circulation.more_flux_storage.block.AbstractFluxGuiBlock;
import com.circulation.more_flux_storage.block.BlockEnergyPylonFlux;
import com.circulation.more_flux_storage.block.BlockInductionPortFlux;
import com.circulation.more_flux_storage.blockentity.TileEnergyPylonFlux;
import com.circulation.more_flux_storage.blockentity.TileInductionPortFlux;
import com.circulation.more_flux_storage.menu.FluxGuiConnectorMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import sonar.fluxnetworks.api.FluxConstants;

public final class MoreFluxStorageContent {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MoreFluxStorageForge.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MoreFluxStorageForge.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MoreFluxStorageForge.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MoreFluxStorageForge.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MoreFluxStorageForge.MOD_ID);

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
    public static final RegistryObject<MenuType<FluxGuiConnectorMenu>> FLUX_GUI_CONNECTOR_MENU = MENU_TYPES.register(
        "flux_gui_connector",
        () -> IForgeMenuType.create((windowId, inv, data) -> {
            var pos = data.readBlockPos();
            CompoundTag tag = data.readNbt();
            IFluxGuiConnector connector = AbstractFluxGuiBlock.resolveFluxConnector(inv.player.level(), pos);
            if (connector != null && tag != null) {
                connector.readCustomTag(tag, FluxConstants.NBT_TILE_UPDATE);
            }
            return new FluxGuiConnectorMenu(windowId, inv, connector);
        })
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