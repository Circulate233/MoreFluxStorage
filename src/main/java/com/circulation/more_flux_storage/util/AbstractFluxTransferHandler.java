package com.circulation.more_flux_storage.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import sonar.fluxnetworks.api.network.ITransferHandler;
import sonar.fluxnetworks.api.utils.NBTType;

public abstract class AbstractFluxTransferHandler implements ITransferHandler {

    protected static final String TAG_BUFFER = "buffer";
    protected static final String TAG_CHANGE = "71";

    private long added;
    private long removed;
    private long buffer;
    private long change;

    protected final void markAdded(long amount) {
        added += amount;
    }

    protected final void markRemoved(long amount) {
        removed += amount;
    }

    protected final long getAddedThisCycle() {
        return added;
    }

    protected final long getRemovedThisCycle() {
        return removed;
    }

    protected final long getCachedBuffer() {
        return buffer;
    }

    protected final void setCachedBuffer(long buffer) {
        this.buffer = buffer;
    }

    protected final void setCachedChange(long change) {
        this.change = change;
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
        change = added - removed;
        added = 0L;
        removed = 0L;
    }

    @Override
    public long getBuffer() {
        return buffer;
    }

    @Override
    public long getChange() {
        return change;
    }

    @Override
    public void addToBuffer(long amount) {
        receiveFromSupplier(amount, EnumFacing.UP, false);
    }

    @Override
    public void writeCustomNBT(NBTTagCompound tag, NBTType type) {
        if (shouldSyncTransferNbt(type)) {
            tag.setLong(TAG_BUFFER, getBuffer());
            tag.setLong(TAG_CHANGE, change);
        }
    }

    @Override
    public void readCustomNBT(NBTTagCompound tag, NBTType type) {
        if (shouldSyncTransferNbt(type)) {
            buffer = tag.getLong(TAG_BUFFER);
            change = tag.getLong(TAG_CHANGE);
        }
    }

    @Override
    public void updateTransfers(EnumFacing... enumFacings) {
    }

    @Override
    public void reset() {
        added = 0L;
        removed = 0L;
        buffer = 0L;
        change = 0L;
    }
}