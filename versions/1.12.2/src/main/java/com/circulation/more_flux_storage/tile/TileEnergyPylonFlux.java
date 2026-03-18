package com.circulation.more_flux_storage.tile;

import com.brandon3055.draconicevolution.blocks.tileentity.TileEnergyPylon;
import com.brandon3055.draconicevolution.blocks.tileentity.TileEnergyStorageCore;
import com.circulation.more_flux_storage.api.IFluxGuiConnector;
import com.circulation.more_flux_storage.util.AbstractFluxTransferHandler;
import com.circulation.more_flux_storage.util.FluxGuiConnectorData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.NotNull;
import sonar.fluxnetworks.api.network.ConnectionType;
import sonar.fluxnetworks.api.network.ITransferHandler;
import sonar.fluxnetworks.api.utils.NBTType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Objects;

public final class TileEnergyPylonFlux extends TileEnergyPylon implements IFluxGuiConnector, FluxGuiConnectorData.Host {

    private static final MethodHandle GET_CORE_HANDLE = createGetCoreHandle();

    private final FluxGuiConnectorData data = new FluxGuiConnectorData(this);
    private final ITransferHandler transferHandler = new EnergyPylonTransferHandler();
    private int tick;

    private static MethodHandle createGetCoreHandle() {
        try {
            Method method = TileEnergyPylon.class.getDeclaredMethod("getCore");
            method.setAccessible(true);
            return MethodHandles.lookup().unreflect(method);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        data.invalidate();
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        data.onChunkUnload();
    }

    @Override
    public void update() {
        if (tick++ % 20 == 0) {
            var c = getFluxCore();
            if (c == null) selectNextCore();
        }
        data.update();
        super.update();
    }

    public TileEnergyStorageCore getFluxCore() {
        TileEnergyStorageCore core;
        try {
            core = (TileEnergyStorageCore) GET_CORE_HANDLE.invoke(this);
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to access TileEnergyPylon#getCore", e);
        }
        if (core == null || core.isInvalid()) return null;
        if (!core.active.value) return null;
        return core;
    }

    private long receiveLongEnergy(TileEnergyStorageCore core, long maxReceive, boolean simulate) {
        if (core == null || core.getWorld() == null || core.getWorld().isRemote || maxReceive <= 0L) {
            return 0L;
        }
        long stored = core.getExtendedStorage();
        long capacity = core.getExtendedCapacity();
        long received = Math.min(Math.max(0L, capacity - stored), maxReceive);
        if (!simulate && received > 0L) {
            core.energy.value += received;
            core.markDirty();
        }
        return received;
    }

    private long extractLongEnergy(TileEnergyStorageCore core, long maxExtract) {
        if (core == null || core.getWorld() == null || core.getWorld().isRemote || maxExtract <= 0L) {
            return 0L;
        }
        long extracted = Math.min(core.getExtendedStorage(), maxExtract);
        if (extracted > 0L) {
            core.energy.value -= extracted;
            core.markDirty();
        }
        return extracted;
    }

    @Override
    public TileEntity getFluxGuiTileEntity() {
        return this;
    }

    @Override
    public ConnectionType getFluxGuiConnectionType() {
        return ConnectionType.STORAGE;
    }

    @Override
    public ItemStack getFluxGuiDisplayStack() {
        Item item = Item.getItemFromBlock(getBlockType());
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    @Override
    public ITransferHandler getFluxGuiTransferHandler() {
        return transferHandler;
    }

    @Override
    public int getFluxGuiFolderId() {
        return 0;
    }

    @Override
    public void onFluxGuiDataChanged() {
        markDirty();
        if (world != null && !world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    @Override
    public FluxGuiConnectorData getFluxData() {
        return data;
    }

    @Override
    public ITransferHandler getTransferHandler() {
        return transferHandler;
    }

    public void writeExtraNBT(NBTTagCompound compound) {
        super.writeExtraNBT(compound);
        writeCustomNBT(compound, NBTType.ALL_SAVE);
    }

    public void readExtraNBT(NBTTagCompound compound) {
        super.readExtraNBT(compound);
        readCustomNBT(compound, NBTType.ALL_SAVE);
    }

    @Override
    public @NotNull NBTTagCompound getUpdateTag() {
        NBTTagCompound compound = super.getUpdateTag();
        writeCustomNBT(compound, NBTType.TILE_UPDATE);
        return compound;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(@NotNull NetworkManager net, SPacketUpdateTileEntity pkt) {
        readCustomNBT(Objects.requireNonNull(pkt.getNbtCompound()), NBTType.TILE_UPDATE);
    }

    @Override
    public void handleUpdateTag(@NotNull NBTTagCompound tag) {
        readCustomNBT(tag, NBTType.TILE_UPDATE);
    }

    private final class EnergyPylonTransferHandler extends AbstractFluxTransferHandler {

        @Override
        public long getBuffer() {
            if (world.isRemote) {
                return getCachedBuffer();
            }
            TileEnergyStorageCore core = getFluxCore();
            return core == null ? 0L : core.getExtendedStorage();
        }

        @Override
        public long getRequest() {
            TileEnergyStorageCore core = getFluxCore();
            return core == null ? 0L : Math.max(0L, Math.min(core.getExtendedCapacity() - core.getExtendedStorage(), getLogicLimit() - getAddedThisCycle()));
        }

        @Override
        public long removeFromBuffer(long l) {
            TileEnergyStorageCore core = getFluxCore();
            if (core == null || l <= 0L) {
                return 0L;
            }
            long extracted = extractLongEnergy(core, l);
            markRemoved(extracted);
            return extracted;
        }

        @Override
        public long receiveFromSupplier(long l, @NotNull EnumFacing enumFacing, boolean b) {
            TileEnergyStorageCore core = getFluxCore();
            if (core == null || l <= 0L) {
                return 0L;
            }
            long received = receiveLongEnergy(core, l, b);
            if (!b) {
                markAdded(received);
            }
            return received;
        }

    }
}
