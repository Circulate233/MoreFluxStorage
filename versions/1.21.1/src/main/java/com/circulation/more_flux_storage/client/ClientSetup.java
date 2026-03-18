package com.circulation.more_flux_storage.client;

import com.circulation.more_flux_storage.MoreFluxStorageNeoForge;
import com.circulation.more_flux_storage.registry.MoreFluxStorageContent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@SuppressWarnings("removal")
@EventBusSubscriber(modid = MoreFluxStorageNeoForge.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {

    private ClientSetup() {
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(MoreFluxStorageContent.FLUX_GUI_CONNECTOR_MENU.get(), FluxGuiConnectorScreen::new);
    }
}
