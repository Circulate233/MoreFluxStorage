package com.circulation.more_flux_storage.client;

import com.circulation.more_flux_storage.api.IFluxGuiConnector;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import sonar.fluxnetworks.register.Channel;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class ClientMessagesM {

    public static void editTile(int token, IFluxGuiConnector device, CompoundTag tag) {
        FriendlyByteBuf buf = buffer();
        buf.writeByte(token);
        buf.writeBlockPos(device.getFluxGuiPos());
        buf.writeNbt(tag);
        Channel.get().sendToServer(buf);
    }

    @Nonnull
    static FriendlyByteBuf buffer() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeShort(4);
        return buffer;
    }
}
