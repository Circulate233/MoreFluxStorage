package com.circulation.more_flux_storage.tile;

import com.circulation.more_flux_storage.api.IFluxGuiConnector;
import com.circulation.more_flux_storage.util.AbstractFluxTransferHandler;
import com.circulation.more_flux_storage.util.FluxGuiConnectorData;
import io.netty.buffer.ByteBuf;
import mekanism.api.TileNetworkList;
import mekanism.common.tile.multiblock.TileEntityInductionPort;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import org.jetbrains.annotations.NotNull;
import sonar.fluxnetworks.api.network.ConnectionType;
import sonar.fluxnetworks.api.utils.NBTType;

public class TileInductionPortFlux extends TileEntityInductionPort implements IFluxGuiConnector, FluxGuiConnectorData.Host {

    private static final double MEKANISM_TO_FLUX = 0.4D;
    private static final double FLUX_TO_MEKANISM = 1D / MEKANISM_TO_FLUX;

    private final FluxGuiConnectorData data = new FluxGuiConnectorData(this);
    private final AbstractFluxTransferHandler transferHandler = new InductionPortTransferHandler();

    public boolean onActivate(EntityPlayer player, EnumHand hand, ItemStack stack) {
        return false;
    }

    @Override
    public void onUpdate() {
        data.update();
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
    public void readCustomNBT(NBTTagCompound nbtTags) {
        super.readCustomNBT(nbtTags);
        readCustomNBT(nbtTags, NBTType.ALL_SAVE);
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbtTags) {
        super.writeCustomNBT(nbtTags);
        writeCustomNBT(nbtTags, NBTType.ALL_SAVE);
    }

    @Override
    public @NotNull NBTTagCompound getUpdateTag() {
        NBTTagCompound compound = super.getUpdateTag();
        writeCustomNBT(compound, NBTType.TILE_UPDATE);
        return compound;
    }

    @Override
    public void handleUpdateTag(@NotNull NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        readCustomNBT(tag, NBTType.TILE_UPDATE);
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
    public AbstractFluxTransferHandler getFluxGuiTransferHandler() {
        return transferHandler;
    }

    @Override
    public FluxGuiConnectorData getFluxData() {
        return data;
    }

    @Override
    public AbstractFluxTransferHandler getTransferHandler() {
        return transferHandler;
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(getTransferHandler().getBuffer());
        data.add(getTransferHandler().getChange());
        data.add(getFluxData().getNetworkID());
        return data;
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);
        getTransferHandler().setBuffer(dataStream.readLong());
        getTransferHandler().setChange(dataStream.readLong());
        getFluxData().setNetworkID(dataStream.readInt());
    }

    private final class InductionPortTransferHandler extends AbstractFluxTransferHandler {


        private static long toFluxLong(double amount) {
            if (amount <= 0D) {
                return 0L;
            }
            return Math.min(Long.MAX_VALUE, (long) amount);
        }

        private static double fluxToMekanism(long fluxAmount) {
            if (fluxAmount <= 0L) {
                return 0D;
            }
            return fluxAmount * FLUX_TO_MEKANISM;
        }

        private static long mekanismToFlux(double mekanismAmount) {
            if (mekanismAmount <= 0D) {
                return 0L;
            }
            return toFluxLong(mekanismAmount * MEKANISM_TO_FLUX);
        }

        private long getRemainingFluxLimit(long transferredThisCycle) {
            long logicLimit = getLogicLimit();
            if (logicLimit == Long.MAX_VALUE) {
                return Long.MAX_VALUE;
            }
            return Math.max(0L, logicLimit - transferredThisCycle);
        }

        @Override
        public long getBuffer() {
            if (world != null && world.isRemote) {
                return getCachedBuffer();
            }
            return structure == null ? 0L : mekanismToFlux(getEnergy());
        }

        private long getLogicLimit() {
            return Math.min(TileInductionPortFlux.this.getLogicLimit(), structure == null ? 0 : mekanismToFlux(structure.getTransferCap()));
        }

        @Override
        public long getRequest() {
            if (structure == null) return 0L;
            return Math.max(0, Math.min(mekanismToFlux(Math.max(0D, getMaxEnergy() - getEnergy())), getLogicLimit() - getAddedThisCycle()));
        }

        @Override
        public long removeFromBuffer(long amount) {
            long fluxRemainingLimit = getRemainingFluxLimit(getRemovedThisCycle());
            long allowedAmount = amount;
            if (fluxRemainingLimit != Long.MAX_VALUE) {
                allowedAmount = Math.min(allowedAmount, fluxRemainingLimit);
            }
            if (allowedAmount <= 0L) {
                return 0L;
            }
            long extracted = mekanismToFlux(removeEnergy(fluxToMekanism(amount), false));
            markRemoved(extracted);
            return extracted;
        }

        @Override
        public long receiveFromSupplier(long amount, @NotNull EnumFacing enumFacing, boolean simulate) {
            long fluxRemainingLimit = getRemainingFluxLimit(getAddedThisCycle());
            long allowedAmount = amount;
            if (fluxRemainingLimit != Long.MAX_VALUE) {
                allowedAmount = Math.min(allowedAmount, fluxRemainingLimit);
            }
            if (allowedAmount <= 0L) {
                return 0L;
            }
            long received = mekanismToFlux(addEnergy(fluxToMekanism(amount), simulate));
            if (!simulate) {
                markAdded(received);
            }
            return received;
        }

    }
}