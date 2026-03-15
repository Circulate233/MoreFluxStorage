package com.circulation.more_flux_storage.registry;

import com.circulation.more_flux_storage.block.BlockEnergyPylonFlux;
import com.circulation.more_flux_storage.block.BlockInductionPortFlux;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("UnusedReturnValue")
public final class RegistryBlocks {

    private static final List<Block> BLOCKS_TO_REGISTER = new LinkedList<>();
    private static final List<Block> BLOCK_MODELS_TO_REGISTER = new LinkedList<>();

    public static final BlockEnergyPylonFlux ENERGY_PYLON_FLUX;
    public static final BlockInductionPortFlux INDUCTION_PORT_FLUX;

    static {
        if (Loader.isModLoaded("draconicevolution")) {
            prepareItemBlockRegister(ENERGY_PYLON_FLUX = registerBlock(new BlockEnergyPylonFlux()));
        } else {
            ENERGY_PYLON_FLUX = null;
        }

        if (Loader.isModLoaded("mekanism")) {
            prepareItemBlockRegister(INDUCTION_PORT_FLUX = registerBlock(new BlockInductionPortFlux()));
        } else {
            INDUCTION_PORT_FLUX = null;
        }
    }

    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        BLOCKS_TO_REGISTER.forEach(event.getRegistry()::register);
        BLOCKS_TO_REGISTER.clear();
    }

    public static void registerBlockModels() {
        if (FMLCommonHandler.instance().getSide().isServer()) {
            BLOCK_MODELS_TO_REGISTER.clear();
            return;
        }
        BLOCK_MODELS_TO_REGISTER.forEach(RegistryBlocks::registerBlockModel);
        BLOCK_MODELS_TO_REGISTER.clear();
    }

    public static void registerBlockModel(final Block block) {
        Item item = Item.getItemFromBlock(block);
        ResourceLocation registryName = Objects.requireNonNull(item.getRegistryName());
        ModelBakery.registerItemVariants(item, registryName);
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(registryName, "inventory"));
    }

    public static <T extends Block> T registerBlock(T block) {
        BLOCKS_TO_REGISTER.add(block);
        BLOCK_MODELS_TO_REGISTER.add(block);
        return block;
    }

    public static ItemBlock prepareItemBlockRegister(Block block) {
        return prepareItemBlockRegister(new ItemBlock(block));
    }

    public static <T extends ItemBlock> T prepareItemBlockRegister(T item) {
        if (item.getRegistryName() == null) {
            Block block = item.getBlock();
            ResourceLocation registryName = Objects.requireNonNull(block.getRegistryName());
            String translationKey = block.getTranslationKey();
            item.setRegistryName(registryName).setTranslationKey(translationKey);
        }
        RegistryItems.registryItem(item);
        return item;
    }

}