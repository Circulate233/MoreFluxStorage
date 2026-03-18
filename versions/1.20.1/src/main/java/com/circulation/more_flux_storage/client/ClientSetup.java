package com.circulation.more_flux_storage.client;

import com.circulation.more_flux_storage.MoreFluxStorageForge;
import com.circulation.more_flux_storage.registry.MoreFluxStorageContent;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = MoreFluxStorageForge.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {

    private ClientSetup() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
            MenuScreens.register(MoreFluxStorageContent.FLUX_GUI_CONNECTOR_MENU.get(), FluxGuiConnectorScreen::new)
        );
    }
}
