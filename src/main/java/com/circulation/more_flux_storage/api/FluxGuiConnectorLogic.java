package com.circulation.more_flux_storage.api;

public interface FluxGuiConnectorLogic {

    long getMaxTransferLimit();

    int getNetworkID();

    int getLogicPriority();

    int getRawPriority();

    void setRawPriority(int priority);

    long getLogicLimit();

    long getRawLimit();

    void setRawLimit(long limit);

    boolean isActive();

    boolean isChunkLoaded();

    boolean isForcedLoading();

    int getFolderID();

    String getCustomName();

    void setCustomName(String customName);

    boolean getDisableLimit();

    void setDisableLimit(boolean disabled);

    boolean getSurgeMode();

    void setSurgeMode(boolean surgeMode);

    void setChunkLoading(boolean chunkLoading);

    long getTransferBuffer();

    long getTransferChange();

    default boolean shouldShowFluxGuiChunkLoading() {
        return true;
    }

    default long sanitizeFluxGuiLimit(long requestedLimit) {
        return Math.min(Math.max(0L, requestedLimit), getMaxTransferLimit());
    }

    default void onFluxGuiAction(FluxGuiActionType action) {
    }
}