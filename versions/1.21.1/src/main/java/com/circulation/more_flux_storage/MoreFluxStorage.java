package com.circulation.more_flux_storage;

import com.circulation.more_flux_storage.registry.MoreFluxStorageContent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(MoreFluxStorage.MOD_ID)
public final class MoreFluxStorage {

    public static final String MOD_ID = "more_flux_storage";

    public MoreFluxStorage(IEventBus modBus) {
        MoreFluxStorageContent.register(modBus);
    }
}
