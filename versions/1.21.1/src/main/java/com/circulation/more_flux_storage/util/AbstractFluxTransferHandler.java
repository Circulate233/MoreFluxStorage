package com.circulation.more_flux_storage.util;

import sonar.fluxnetworks.common.connection.TransferHandler;

public abstract class AbstractFluxTransferHandler extends TransferHandler {

    private long added;
    private long removed;

    protected AbstractFluxTransferHandler() {
        super(200000L);
    }

    public int getLogicPriority() {
        return getSurgeMode() ? -PRI_GAIN_MIN : Math.min(getRawPriority() - STORAGE_PRI_DIFF, -PRI_GAIN_MIN);
    }

    public void setRawPriority(int priority) {
        setPriority(priority);
    }

    public long getLogicLimit() {
        return getLimit();
    }

    public void setRawLimit(long limit) {
        setLimit(limit);
    }

    @Override
    public void onCycleStart() {
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

    public void reset() {
        added = 0L;
        removed = 0L;
        mBuffer = 0L;
        mChange = 0L;
    }

    public void setBuffer(long buffer) {
        mBuffer = Math.max(0L, buffer);
    }

    public void setChange(long change) {
        mChange = change;
    }
}