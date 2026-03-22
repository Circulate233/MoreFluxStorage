package com.circulation.more_flux_storage.util;

import sonar.fluxnetworks.common.device.FluxStorageHandler;

public abstract class AbstractFluxTransferHandler extends FluxStorageHandler {

    private long added;
    private long removed;

    protected AbstractFluxTransferHandler() {
        super(200000L);
    }

    @Override
    public void onCycleStart() {
    }

    @Override
    public long getMaxEnergyStorage() {
        return 0;
    }

    @Override
    public void onCycleEnd() {
        mChange = added - removed;
        added = 0L;
        removed = 0L;
    }

    @Override
    public void addToBuffer(long amount) {
        long accepted = Math.max(0L, amount);
        mBuffer += accepted;
        added += accepted;
    }

    @Override
    public long removeFromBuffer(long amount) {
        long extracted = Math.min(Math.max(0L, amount), mBuffer);
        mBuffer -= extracted;
        removed += extracted;
        return extracted;
    }

    @Override
    public long getRequest() {
        return 0L;
    }

    @Override
    public void onNetworkChanged() {
        super.onNetworkChanged();
        added = 0L;
        removed = 0L;
    }

    public void setBuffer(long buffer) {
        mBuffer = Math.max(0L, buffer);
    }

    public long getAdded() {
        return added;
    }
}