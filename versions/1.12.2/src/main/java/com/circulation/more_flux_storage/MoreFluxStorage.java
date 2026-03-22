package com.circulation.more_flux_storage;

import com.circulation.more_flux_storage.proxy.CommonProxy;
import com.circulation.more_flux_storage.registry.RegistryBlocks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION,
    dependencies = "required-after:mixinbooter@[8.0,);" +
        "required-after:fluxnetworks"
)
public class MoreFluxStorage {

    public static final String MOD_ID = Tags.MOD_ID;
    public static final String CLIENT_PROXY = "com.circulation.more_flux_storage.proxy.ClientProxy";
    public static final String COMMON_PROXY = "com.circulation.more_flux_storage.proxy.CommonProxy";
    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    public static final SimpleNetworkWrapper NET_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);
    public static final CreativeTabs CREATIVE_TAB = new CreativeTabs(MOD_ID) {
        @Override
        public @NotNull ItemStack createIcon() {
            return new ItemStack(RegistryBlocks.ENERGY_PYLON_FLUX);
        }
    };
    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = COMMON_PROXY)
    public static CommonProxy proxy;
    @Mod.Instance(MOD_ID)
    public static MoreFluxStorage instance;
    public static MinecraftServer server;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit();
    }


    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        server = event.getServer();
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        server = null;
    }

}
