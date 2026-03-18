package com.circulation.more_flux_storage.compat.flux;

public final class FluxNeoForgeBootstrap {

    private FluxNeoForgeBootstrap() {
    }

    public static void init() {
        FluxNeoForgeApiHandle.INSTANCE.isSupported();
    }
}