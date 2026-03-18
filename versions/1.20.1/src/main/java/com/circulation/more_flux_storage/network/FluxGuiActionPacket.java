package com.circulation.more_flux_storage.network;

import com.circulation.more_flux_storage.api.FluxGuiActionType;
import com.circulation.more_flux_storage.api.IFluxGuiConnector;
import com.circulation.more_flux_storage.block.AbstractFluxGuiBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FluxGuiActionPacket {

    private final BlockPos pos;
    private final FluxGuiActionType action;
    private final String stringValue;
    private final int intValue;
    private final long longValue;
    private final boolean booleanValue;

    private FluxGuiActionPacket(BlockPos pos, FluxGuiActionType action,
                                String stringValue, int intValue, long longValue, boolean booleanValue) {
        this.pos = pos;
        this.action = action;
        this.stringValue = stringValue;
        this.intValue = intValue;
        this.longValue = longValue;
        this.booleanValue = booleanValue;
    }

    public static FluxGuiActionPacket setName(BlockPos pos, String name) {
        return new FluxGuiActionPacket(pos, FluxGuiActionType.SET_NAME, name, 0, 0L, false);
    }

    public static FluxGuiActionPacket setPriority(BlockPos pos, int priority) {
        return new FluxGuiActionPacket(pos, FluxGuiActionType.SET_PRIORITY, "", priority, 0L, false);
    }

    public static FluxGuiActionPacket setLimit(BlockPos pos, long limit) {
        return new FluxGuiActionPacket(pos, FluxGuiActionType.SET_LIMIT, "", 0, limit, false);
    }

    public static FluxGuiActionPacket setSurge(BlockPos pos, boolean surge) {
        return new FluxGuiActionPacket(pos, FluxGuiActionType.SET_SURGE, "", 0, 0L, surge);
    }

    public static FluxGuiActionPacket setDisableLimit(BlockPos pos, boolean disable) {
        return new FluxGuiActionPacket(pos, FluxGuiActionType.SET_DISABLE_LIMIT, "", 0, 0L, disable);
    }

    public static FluxGuiActionPacket setChunkLoading(BlockPos pos, boolean loading) {
        return new FluxGuiActionPacket(pos, FluxGuiActionType.SET_CHUNK_LOADING, "", 0, 0L, loading);
    }

    public static FluxGuiActionPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        int ordinal = buf.readByte();
        FluxGuiActionType[] values = FluxGuiActionType.values();
        FluxGuiActionType action = ordinal >= 0 && ordinal < values.length ? values[ordinal] : FluxGuiActionType.SET_NAME;
        String stringValue = buf.readUtf(256);
        int intValue = buf.readInt();
        long longValue = buf.readLong();
        boolean booleanValue = buf.readBoolean();
        return new FluxGuiActionPacket(pos, action, stringValue, intValue, longValue, booleanValue);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeByte(action.ordinal());
        buf.writeUtf(stringValue, 256);
        buf.writeInt(intValue);
        buf.writeLong(longValue);
        buf.writeBoolean(booleanValue);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            Level level = player.level();
            if (!level.isLoaded(pos)) return;

            IFluxGuiConnector connector = AbstractFluxGuiBlock.resolveFluxConnector(level, pos);
            if (connector == null || !connector.canOpenFluxGui(player)) return;

            switch (action) {
                case SET_NAME -> connector.setCustomName(stringValue);
                case SET_PRIORITY -> connector.setRawPriority(intValue);
                case SET_LIMIT -> connector.setRawLimit(connector.sanitizeFluxGuiLimit(longValue));
                case SET_SURGE -> connector.setSurgeMode(booleanValue);
                case SET_DISABLE_LIMIT -> connector.setDisableLimit(booleanValue);
                case SET_CHUNK_LOADING -> connector.setChunkLoading(booleanValue);
            }

            connector.onFluxGuiAction(action);
        });
        context.setPacketHandled(true);
    }
}
