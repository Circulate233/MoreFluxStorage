package com.circulation.more_flux_storage.network;

import com.circulation.more_flux_storage.MoreFluxStorageForge;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class MoreFluxStorageNetwork {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(MoreFluxStorageForge.MOD_ID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private MoreFluxStorageNetwork() {
    }

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, FluxGuiActionPacket.class,
            FluxGuiActionPacket::encode,
            FluxGuiActionPacket::decode,
            FluxGuiActionPacket::handle);
    }
}
