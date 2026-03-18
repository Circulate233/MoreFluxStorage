package com.circulation.more_flux_storage.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import sonar.fluxnetworks.api.network.ITransferHandler;
import sonar.fluxnetworks.api.utils.NBTType;

public abstract class AbstractFluxTransferHandler extends FluxTransferStats implements ITransferHandler {

    protected static final String TAG_BUFFER = "buffer";
    protected static final String TAG_CHANGE = "71";

    public int getLogicPriority() {
        return getStoragePriority();
    }

    public long getLogicLimit() {
        return getLimit();
    }

    protected final boolean shouldSyncTransferNbt(NBTType type) {
        return switch (type) {
            case DEFAULT, TILE_SAVE, TILE_DROP, TILE_UPDATE, ALL_SAVE -> true;
            default -> false;
        };
    }

    @Override
    public void onCycleStart() {
    }

    @Override
    public void onCycleEnd() {
        finishTransferCycle();
    }

    @Override
    public void addToBuffer(long amount) {
        receiveFromSupplier(amount, EnumFacing.UP, false);
    }

    @Override
    public void writeCustomNBT(NBTTagCompound tag, NBTType type) {
        if (shouldSyncTransferNbt(type)) {
            tag.setLong(TAG_BUFFER, getBuffer());
            tag.setLong(TAG_CHANGE, getChange());
            tag.setInteger(FluxGuiConnectorData.TAG_PRIORITY, getRawPriority());
            tag.setLong(FluxGuiConnectorData.TAG_LIMIT, getRawLimit());
            tag.setBoolean(FluxGuiConnectorData.TAG_SURGE, getSurgeMode());
            tag.setBoolean(FluxGuiConnectorData.TAG_DISABLE_LIMIT, getDisableLimit());
        }
    }

    @Override
    public void readCustomNBT(NBTTagCompound tag, NBTType type) {
        if (shouldSyncTransferNbt(type)) {
            setBuffer(tag.getLong(TAG_BUFFER));
            setChange(tag.getLong(TAG_CHANGE));
            if (tag.hasKey(FluxGuiConnectorData.TAG_PRIORITY)) {
                setRawPriority(tag.getInteger(FluxGuiConnectorData.TAG_PRIORITY));
            }
            if (tag.hasKey(FluxGuiConnectorData.TAG_LIMIT)) {
                setRawLimit(tag.getLong(FluxGuiConnectorData.TAG_LIMIT));
            }
            if (tag.hasKey(FluxGuiConnectorData.TAG_SURGE)) {
                setSurgeMode(tag.getBoolean(FluxGuiConnectorData.TAG_SURGE));
            }
            if (tag.hasKey(FluxGuiConnectorData.TAG_DISABLE_LIMIT)) {
                setDisableLimit(tag.getBoolean(FluxGuiConnectorData.TAG_DISABLE_LIMIT));
            }
        }
    }

    @Override
    public void updateTransfers(EnumFacing... enumFacings) {
    }

    @Override
    public void reset() {
        resetTransferStats();
    }

    public long getBuffer() {
        return getStoredBuffer();
    }

    public void setBuffer(long buffer) {
        setStoredBuffer(buffer);
    }

    public long getChange() {
        return getStoredChange();
    }

    public void setChange(long change) {
        setStoredChange(change);
    }
}