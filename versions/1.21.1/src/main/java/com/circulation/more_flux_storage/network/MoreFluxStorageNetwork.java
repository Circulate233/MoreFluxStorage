package com.circulation.more_flux_storage.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class MoreFluxStorageNetwork {

    private MoreFluxStorageNetwork() {
    }

    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
            FluxGuiActionPayload.TYPE,
            FluxGuiActionPayload.STREAM_CODEC,
            FluxGuiActionPayload::handleServer
        );
    }
}
