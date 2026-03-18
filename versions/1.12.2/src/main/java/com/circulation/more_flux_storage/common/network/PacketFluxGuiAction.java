package com.circulation.more_flux_storage.common.network;

import com.circulation.more_flux_storage.api.FluxGuiActionType;
import com.circulation.more_flux_storage.api.IFluxGuiConnector;
import com.circulation.more_flux_storage.util.Packet;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketFluxGuiAction implements Packet<PacketFluxGuiAction> {

    private BlockPos pos;
    private FluxGuiActionType action;
    private String stringValue = "";
    private int intValue;
    private long longValue;
    private boolean booleanValue;

    @SuppressWarnings("unused")
    public PacketFluxGuiAction() {
    }

    public static PacketFluxGuiAction setName(BlockPos pos, String value) {
        PacketFluxGuiAction message = new PacketFluxGuiAction();
        message.pos = pos;
        message.action = FluxGuiActionType.SET_NAME;
        message.stringValue = value;
        return message;
    }

    public static PacketFluxGuiAction setPriority(BlockPos pos, int value) {
        PacketFluxGuiAction message = new PacketFluxGuiAction();
        message.pos = pos;
        message.action = FluxGuiActionType.SET_PRIORITY;
        message.intValue = value;
        return message;
    }

    public static PacketFluxGuiAction setLimit(BlockPos pos, long value) {
        PacketFluxGuiAction message = new PacketFluxGuiAction();
        message.pos = pos;
        message.action = FluxGuiActionType.SET_LIMIT;
        message.longValue = value;
        return message;
    }

    public static PacketFluxGuiAction setSurge(BlockPos pos, boolean value) {
        PacketFluxGuiAction message = new PacketFluxGuiAction();
        message.pos = pos;
        message.action = FluxGuiActionType.SET_SURGE;
        message.booleanValue = value;
        return message;
    }

    public static PacketFluxGuiAction setDisableLimit(BlockPos pos, boolean value) {
        PacketFluxGuiAction message = new PacketFluxGuiAction();
        message.pos = pos;
        message.action = FluxGuiActionType.SET_DISABLE_LIMIT;
        message.booleanValue = value;
        return message;
    }

    public static PacketFluxGuiAction setChunkLoading(BlockPos pos, boolean value) {
        PacketFluxGuiAction message = new PacketFluxGuiAction();
        message.pos = pos;
        message.action = FluxGuiActionType.SET_CHUNK_LOADING;
        message.booleanValue = value;
        return message;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
        action = FluxGuiActionType.values()[buf.readByte()];
        stringValue = ByteBufUtils.readUTF8String(buf);
        intValue = buf.readInt();
        longValue = buf.readLong();
        booleanValue = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        buf.writeByte(action.ordinal());
        ByteBufUtils.writeUTF8String(buf, stringValue);
        buf.writeInt(intValue);
        buf.writeLong(longValue);
        buf.writeBoolean(booleanValue);
    }

    @Override
    public IMessage onMessage(PacketFluxGuiAction message, MessageContext ctx) {
        IThreadListener thread = FMLCommonHandler.instance().getWorldThread(ctx.netHandler);
        thread.addScheduledTask(() -> message.handle(ctx.getServerHandler().player));
        return null;
    }

    private void handle(EntityPlayerMP player) {
        World world = player.world;
        if (!world.isBlockLoaded(pos)) {
            return;
        }

        TileEntity tileEntity = world.getTileEntity(pos);
        if (!(tileEntity instanceof IFluxGuiConnector connector)) {
            return;
        }

        if (!connector.canOpenFluxGui(player)) {
            return;
        }

        switch (action) {
            case SET_NAME:
                connector.setCustomName(stringValue);
                break;
            case SET_PRIORITY:
                connector.setRawPriority(intValue);
                break;
            case SET_LIMIT:
                connector.setRawLimit(connector.sanitizeFluxGuiLimit(longValue));
                break;
            case SET_SURGE:
                connector.setSurgeMode(booleanValue);
                break;
            case SET_DISABLE_LIMIT:
                connector.setDisableLimit(booleanValue);
                break;
            case SET_CHUNK_LOADING:
                connector.setChunkLoading(booleanValue);
                break;
            default:
                return;
        }

        connector.onFluxGuiAction(player, action);
        tileEntity.markDirty();
        IBlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 3);
    }
}
