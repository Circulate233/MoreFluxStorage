package com.circulation.more_flux_storage.network;

import com.circulation.more_flux_storage.MoreFluxStorageNeoForge;
import com.circulation.more_flux_storage.api.FluxGuiActionType;
import com.circulation.more_flux_storage.api.IFluxGuiConnector;
import com.circulation.more_flux_storage.block.AbstractFluxGuiBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record FluxGuiActionPayload(
    BlockPos pos,
    FluxGuiActionType action,
    String stringValue,
    int intValue,
    long longValue,
    boolean booleanValue
) implements CustomPacketPayload {

    public static final Type<FluxGuiActionPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(MoreFluxStorageNeoForge.MOD_ID, "flux_gui_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluxGuiActionPayload> STREAM_CODEC =
        StreamCodec.of((buf, val) -> val.write(buf), FluxGuiActionPayload::read);

    public static FluxGuiActionPayload setName(BlockPos pos, String name) {
        return new FluxGuiActionPayload(pos, FluxGuiActionType.SET_NAME, name, 0, 0L, false);
    }

    public static FluxGuiActionPayload setPriority(BlockPos pos, int priority) {
        return new FluxGuiActionPayload(pos, FluxGuiActionType.SET_PRIORITY, "", priority, 0L, false);
    }

    public static FluxGuiActionPayload setLimit(BlockPos pos, long limit) {
        return new FluxGuiActionPayload(pos, FluxGuiActionType.SET_LIMIT, "", 0, limit, false);
    }

    public static FluxGuiActionPayload setSurge(BlockPos pos, boolean surge) {
        return new FluxGuiActionPayload(pos, FluxGuiActionType.SET_SURGE, "", 0, 0L, surge);
    }

    public static FluxGuiActionPayload setDisableLimit(BlockPos pos, boolean disable) {
        return new FluxGuiActionPayload(pos, FluxGuiActionType.SET_DISABLE_LIMIT, "", 0, 0L, disable);
    }

    public static FluxGuiActionPayload setChunkLoading(BlockPos pos, boolean loading) {
        return new FluxGuiActionPayload(pos, FluxGuiActionType.SET_CHUNK_LOADING, "", 0, 0L, loading);
    }

    private static FluxGuiActionPayload read(RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        int ordinal = buf.readByte();
        FluxGuiActionType[] values = FluxGuiActionType.values();
        FluxGuiActionType action = ordinal >= 0 && ordinal < values.length ? values[ordinal] : FluxGuiActionType.SET_NAME;
        String str = buf.readUtf(256);
        int intVal = buf.readInt();
        long longVal = buf.readLong();
        boolean boolVal = buf.readBoolean();
        return new FluxGuiActionPayload(pos, action, str, intVal, longVal, boolVal);
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeByte(action.ordinal());
        buf.writeUtf(stringValue, 256);
        buf.writeInt(intValue);
        buf.writeLong(longValue);
        buf.writeBoolean(booleanValue);
    }

    public static void handleServer(FluxGuiActionPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            Level level = player.level();
            if (!level.isLoaded(payload.pos())) return;

            IFluxGuiConnector connector = AbstractFluxGuiBlock.resolveFluxConnector(level, payload.pos());
            if (connector == null || !connector.canOpenFluxGui(player)) return;

            switch (payload.action()) {
                case SET_NAME -> connector.setCustomName(payload.stringValue());
                case SET_PRIORITY -> connector.setRawPriority(payload.intValue());
                case SET_LIMIT -> connector.setRawLimit(connector.sanitizeFluxGuiLimit(payload.longValue()));
                case SET_SURGE -> connector.setSurgeMode(payload.booleanValue());
                case SET_DISABLE_LIMIT -> connector.setDisableLimit(payload.booleanValue());
                case SET_CHUNK_LOADING -> connector.setChunkLoading(payload.booleanValue());
            }

            connector.onFluxGuiAction(payload.action());
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
