package com.circulation.more_flux_storage.registry;

import com.circulation.more_flux_storage.MoreFluxStorage;
import com.circulation.more_flux_storage.block.BlockEnergyPylonFlux;
import com.circulation.more_flux_storage.block.BlockFluxAccessorFlux;
import com.circulation.more_flux_storage.block.BlockInductionPortFlux;
import com.circulation.more_flux_storage.blockentity.TileEnergyPylonFlux;
import com.circulation.more_flux_storage.blockentity.TileFluxAccessorFlux;
import com.circulation.more_flux_storage.blockentity.TileInductionPortFlux;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MoreFluxStorageContent {

    public static final String ENERGY_PYLON_FLUX_DESCRIPTION_ID = "block.more_flux_storage.energy_pylon_flux";
    public static final String FLUX_ACCESSOR_FLUX_DESCRIPTION_ID = "block.more_flux_storage.flux_accessor_flux";
    public static final String INDUCTION_PORT_FLUX_DESCRIPTION_ID = "block.more_flux_storage.induction_port_flux";
    public static final boolean HAS_APPFLUX = ModList.get().isLoaded("appflux");
    public static final boolean HAS_DRACONIC_EVOLUTION = ModList.get().isLoaded("draconicevolution");
    public static final boolean HAS_MEKANISM = ModList.get().isLoaded("mekanism");

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MoreFluxStorage.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MoreFluxStorage.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MoreFluxStorage.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MoreFluxStorage.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MoreFluxStorage.MOD_ID);

    public static final @Nullable RegistryObject<BlockEnergyPylonFlux> ENERGY_PYLON_FLUX = HAS_DRACONIC_EVOLUTION
        ? BLOCKS.register("energy_pylon_flux", BlockEnergyPylonFlux::new)
        : null;
    public static final @Nullable RegistryObject<Item> ENERGY_PYLON_FLUX_ITEM = HAS_DRACONIC_EVOLUTION
        ? ITEMS.register("energy_pylon_flux", () -> new BlockItem(ENERGY_PYLON_FLUX.get(), new Item.Properties()))
        : null;
    public static final @Nullable RegistryObject<BlockEntityType<TileEnergyPylonFlux>> ENERGY_PYLON_FLUX_BLOCK_ENTITY = HAS_DRACONIC_EVOLUTION
        ? BLOCK_ENTITY_TYPES.register("energy_pylon_flux", () -> BlockEntityType.Builder.of(TileEnergyPylonFlux::new, ENERGY_PYLON_FLUX.get()).build(null))
        : null;
    public static final @Nullable RegistryObject<BlockFluxAccessorFlux> FLUX_ACCESSOR_FLUX = HAS_APPFLUX
        ? BLOCKS.register("flux_accessor_flux", BlockFluxAccessorFlux::new)
        : null;
    public static final @Nullable RegistryObject<Item> FLUX_ACCESSOR_FLUX_ITEM = HAS_APPFLUX
        ? ITEMS.register("flux_accessor_flux", () -> new BlockItem(FLUX_ACCESSOR_FLUX.get(), new Item.Properties()))
        : null;
    public static final @Nullable RegistryObject<BlockInductionPortFlux> INDUCTION_PORT_FLUX = HAS_MEKANISM
        ? BLOCKS.register("induction_port_flux", BlockInductionPortFlux::new)
        : null;    public static final @Nullable RegistryObject<BlockEntityType<TileFluxAccessorFlux>> FLUX_ACCESSOR_FLUX_BLOCK_ENTITY = HAS_APPFLUX
        ? BLOCK_ENTITY_TYPES.register("flux_accessor_flux", () -> BlockEntityType.Builder.of(TileFluxAccessorFlux::new, FLUX_ACCESSOR_FLUX.get()).build(null))
        : null;
    public static final @Nullable RegistryObject<Item> INDUCTION_PORT_FLUX_ITEM = HAS_MEKANISM
        ? ITEMS.register("induction_port_flux", () -> new BlockItem(INDUCTION_PORT_FLUX.get(), new Item.Properties()))
        : null;
    public static final RegistryObject<CreativeModeTab> MAIN_CREATIVE_MODE_TAB = CREATIVE_MODE_TABS.register(
        "main",
        () -> CreativeModeTab.builder()
                             .title(Component.translatable("itemGroup.more_flux_storage"))
                             .icon(MoreFluxStorageContent::createCreativeTabIcon)
                             .displayItems((parameters, output) -> {
                                 acceptIfPresent(output, ENERGY_PYLON_FLUX_ITEM);
                                 acceptIfPresent(output, FLUX_ACCESSOR_FLUX_ITEM);
                                 acceptIfPresent(output, INDUCTION_PORT_FLUX_ITEM);
                             })
                             .build()
    );
    public static final @Nullable RegistryObject<BlockEntityType<TileInductionPortFlux>> INDUCTION_PORT_FLUX_BLOCK_ENTITY = HAS_MEKANISM
        ? BLOCK_ENTITY_TYPES.register("induction_port_flux", () -> BlockEntityType.Builder.of(TileInductionPortFlux::new, INDUCTION_PORT_FLUX.get()).build(null))
        : null;
    private MoreFluxStorageContent() {
    }

    public static @Nullable BlockEntityType<TileEnergyPylonFlux> getEnergyPylonFluxBlockEntityType() {
        return ENERGY_PYLON_FLUX_BLOCK_ENTITY == null ? null : ENERGY_PYLON_FLUX_BLOCK_ENTITY.get();
    }

    public static @Nullable BlockEntityType<TileFluxAccessorFlux> getFluxAccessorFluxBlockEntityType() {
        return FLUX_ACCESSOR_FLUX_BLOCK_ENTITY == null ? null : FLUX_ACCESSOR_FLUX_BLOCK_ENTITY.get();
    }

    public static @Nullable BlockEntityType<TileInductionPortFlux> getInductionPortFluxBlockEntityType() {
        return INDUCTION_PORT_FLUX_BLOCK_ENTITY == null ? null : INDUCTION_PORT_FLUX_BLOCK_ENTITY.get();
    }

    public static @NotNull ItemStack getEnergyPylonFluxStack() {
        return ENERGY_PYLON_FLUX_ITEM == null ? ItemStack.EMPTY : new ItemStack(ENERGY_PYLON_FLUX_ITEM.get());
    }

    public static @NotNull ItemStack getFluxAccessorFluxStack() {
        return FLUX_ACCESSOR_FLUX_ITEM == null ? ItemStack.EMPTY : new ItemStack(FLUX_ACCESSOR_FLUX_ITEM.get());
    }

    public static @NotNull ItemStack getInductionPortFluxStack() {
        return INDUCTION_PORT_FLUX_ITEM == null ? ItemStack.EMPTY : new ItemStack(INDUCTION_PORT_FLUX_ITEM.get());
    }

    private static @NotNull ItemStack createCreativeTabIcon() {
        ItemStack energyPylon = getEnergyPylonFluxStack();
        if (!energyPylon.isEmpty()) {
            return energyPylon;
        }

        ItemStack fluxAccessor = getFluxAccessorFluxStack();
        if (!fluxAccessor.isEmpty()) {
            return fluxAccessor;
        }

        ItemStack inductionPort = getInductionPortFluxStack();
        if (!inductionPort.isEmpty()) {
            return inductionPort;
        }

        return new ItemStack(Items.REDSTONE);
    }

    private static void acceptIfPresent(CreativeModeTab.Output output, @Nullable RegistryObject<Item> item) {
        if (item != null) {
            output.accept(item.get());
        }
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITY_TYPES.register(modBus);
        MENU_TYPES.register(modBus);
        CREATIVE_MODE_TABS.register(modBus);
    }


}