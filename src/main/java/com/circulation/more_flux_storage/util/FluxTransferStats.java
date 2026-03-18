package com.circulation.more_flux_storage.util;

public class FluxTransferStats {

    public static final int PRI_USER_MIN = -9999;
    public static final int PRI_USER_MAX = 9999;
    public static final int PRI_GAIN_MIN = 10000;
    public static final int PRI_GAIN_MAX = 100000;
    public static final int STORAGE_PRI_DIFF = 1000000;

    private long added;
    private long removed;
    private long buffer;
    private long change;
    private int priority;
    private boolean surgeMode;
    private long limit = 200000L;
    private boolean disableLimit;

    private static int clampPriority(int value) {
        if (value < PRI_USER_MIN) {
            return PRI_USER_MIN;
        }
        return Math.min(value, PRI_USER_MAX);
    }

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

    protected final void finishTransferCycle() {
        change = added - removed;
        added = 0L;
        removed = 0L;
    }

    protected final void resetTransferStats() {
        added = 0L;
        removed = 0L;
        buffer = 0L;
        change = 0L;
    }

    public final int getRawPriority() {
        return priority;
    }

    public final void setRawPriority(int priority) {
        int clamped = clampPriority(priority);
        if (this.priority != clamped) {
            this.priority = clamped;
        }
    }

    public final int getPriority() {
        return surgeMode ? PRI_GAIN_MAX : priority;
    }

    public final int getStoragePriority() {
        return surgeMode ? -PRI_GAIN_MIN : Math.min(priority - STORAGE_PRI_DIFF, -PRI_GAIN_MIN);
    }

    public final boolean getSurgeMode() {
        return surgeMode;
    }

    public final void setSurgeMode(boolean surgeMode) {
        if (this.surgeMode != surgeMode) {
            this.surgeMode = surgeMode;
        }
    }

    public final long getRawLimit() {
        return limit;
    }

    public final void setRawLimit(long limit) {
        this.limit = Math.max(0L, limit);
    }

    public final long getLimit() {
        return disableLimit ? Long.MAX_VALUE : limit;
    }

    public final boolean getDisableLimit() {
        return disableLimit;
    }

    public final void setDisableLimit(boolean disableLimit) {
        this.disableLimit = disableLimit;
    }

    public final long getStoredBuffer() {
        return buffer;
    }

    public final void setStoredBuffer(long buffer) {
        this.buffer = buffer;
    }

    public final long getStoredChange() {
        return change;
    }

    public final void setStoredChange(long change) {
        this.change = change;
    }
}