package com.circulation.more_flux_storage.registry;

import com.circulation.more_flux_storage.MoreFluxStorageNeoForge;
import com.circulation.more_flux_storage.api.IFluxGuiConnector;
import com.circulation.more_flux_storage.block.AbstractFluxGuiBlock;
import com.circulation.more_flux_storage.block.BlockEnergyPylonFlux;
import com.circulation.more_flux_storage.block.BlockInductionPortFlux;
import com.circulation.more_flux_storage.blockentity.TileEnergyPylonFlux;
import com.circulation.more_flux_storage.blockentity.TileInductionPortFlux;
import com.circulation.more_flux_storage.menu.FluxGuiConnectorMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import sonar.fluxnetworks.api.FluxConstants;

public final class MoreFluxStorageContent {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MoreFluxStorageNeoForge.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MoreFluxStorageNeoForge.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MoreFluxStorageNeoForge.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, MoreFluxStorageNeoForge.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MoreFluxStorageNeoForge.MOD_ID);

    public static final DeferredBlock<BlockEnergyPylonFlux> ENERGY_PYLON_FLUX = BLOCKS.register("energy_pylon_flux", BlockEnergyPylonFlux::new);
    public static final DeferredItem<BlockItem> ENERGY_PYLON_FLUX_ITEM = ITEMS.registerSimpleBlockItem("energy_pylon_flux", ENERGY_PYLON_FLUX);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEnergyPylonFlux>> ENERGY_PYLON_FLUX_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
        "energy_pylon_flux",
        () -> BlockEntityType.Builder.of(TileEnergyPylonFlux::new, ENERGY_PYLON_FLUX.get()).build(null)
    );

    public static final DeferredBlock<BlockInductionPortFlux> INDUCTION_PORT_FLUX = BLOCKS.register("induction_port_flux", BlockInductionPortFlux::new);
    public static final DeferredItem<BlockItem> INDUCTION_PORT_FLUX_ITEM = ITEMS.registerSimpleBlockItem("induction_port_flux", INDUCTION_PORT_FLUX);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileInductionPortFlux>> INDUCTION_PORT_FLUX_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
        "induction_port_flux",
        () -> BlockEntityType.Builder.of(TileInductionPortFlux::new, INDUCTION_PORT_FLUX.get()).build(null)
    );

    public static final DeferredHolder<MenuType<?>, MenuType<FluxGuiConnectorMenu>> FLUX_GUI_CONNECTOR_MENU = MENU_TYPES.register(
        "flux_gui_connector",
        () -> IMenuTypeExtension.create((windowId, inv, data) -> {
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