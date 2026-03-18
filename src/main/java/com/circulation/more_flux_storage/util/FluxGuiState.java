package com.circulation.more_flux_storage.util;

import java.util.UUID;

public final class FluxGuiState {

    private int networkId = -1;
    private UUID owner = new UUID(0L, 0L);
    private String customName = "Flux Storage";
    private boolean chunkLoading;
    private boolean chunkLoaded = true;

    public int getNetworkId() { return networkId; }
    public void setNetworkId(int networkId) { this.networkId = networkId; }

    public UUID getOwner() { return owner; }
    public void setOwner(UUID owner) { this.owner = owner; }

    public String getCustomName() { return customName; }
    public void setCustomName(String customName) { this.customName = customName; }

    public boolean isChunkLoading() { return chunkLoading; }
    public void setChunkLoading(boolean chunkLoading) { this.chunkLoading = chunkLoading; }

    public boolean isChunkLoaded() { return chunkLoaded; }
    public void setChunkLoaded(boolean chunkLoaded) { this.chunkLoaded = chunkLoaded; }

}