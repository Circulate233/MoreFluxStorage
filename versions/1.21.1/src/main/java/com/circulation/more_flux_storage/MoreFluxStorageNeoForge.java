package com.circulation.more_flux_storage;

import com.circulation.more_flux_storage.compat.flux.FluxNeoForgeBootstrap;
import com.circulation.more_flux_storage.network.MoreFluxStorageNetwork;
import com.circulation.more_flux_storage.registry.MoreFluxStorageContent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(MoreFluxStorageNeoForge.MOD_ID)
public final class MoreFluxStorageNeoForge {
    public static final String MOD_ID = "more_flux_storage";

    public MoreFluxStorageNeoForge(IEventBus modBus) {
        MoreFluxStorageContent.register(modBus);
        modBus.addListener(MoreFluxStorageNetwork::onRegisterPayloads);
        FluxNeoForgeBootstrap.init();
    }
}