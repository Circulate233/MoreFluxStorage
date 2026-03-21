package com.circulation.more_flux_storage;

import com.circulation.more_flux_storage.network.MoreFluxStorageNetwork;
import com.circulation.more_flux_storage.registry.MoreFluxStorageContent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MoreFluxStorage.MOD_ID)
public final class MoreFluxStorage {
    public static final String MOD_ID = "more_flux_storage";

    public MoreFluxStorage() {
        MoreFluxStorageContent.register(FMLJavaModLoadingContext.get().getModEventBus());
        MoreFluxStorageNetwork.register();
    }
}