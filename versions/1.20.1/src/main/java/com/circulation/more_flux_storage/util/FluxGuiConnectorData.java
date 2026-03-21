package com.circulation.more_flux_storage.util;

import com.circulation.more_flux_storage.api.IFluxGuiConnector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import sonar.fluxnetworks.api.FluxConstants;
import sonar.fluxnetworks.common.connection.FluxNetwork;

import java.util.UUID;

public class FluxGuiConnectorData {

    private final Host host;
    private final FluxGuiState state = new FluxGuiState();
    private Player playerUsing;

    public FluxGuiConnectorData(Host host) {
        this.host = host;
    }

    public void sync() {
        host.onFluxGuiDataChanged();
    }

    public int getNetworkID() {
        return state.getNetworkId();
    }

    public void setNetworkID(int networkID) {
        state.setNetworkId(networkID);
    }

    public void setPlayerUUID(UUID uuid) {
        state.setOwner(uuid);
    }

    public UUID getConnectionOwner() {
        return state.getOwner();
    }

    public void writeCustomTag(CompoundTag tag, byte type) {
        tag.putInt(FluxConstants.NETWORK_ID, state.getNetworkId());
        tag.putUUID(FluxConstants.PLAYER_UUID, state.getOwner());
        tag.putString(FluxConstants.CUSTOM_NAME, state.getCustomName());
        tag.putBoolean(FluxConstants.FORCED_LOADING, state.isChunkLoading());
        tag.putBoolean(FluxConstants.CHUNK_LOADED, state.isChunkLoaded());
        getFluxTransferHandler().writeCustomTag(tag, type);
    }

    public void readCustomTag(CompoundTag tag, byte type) {
        if (type == FluxConstants.NBT_TILE_SETTINGS) {
            if (tag.contains(FluxConstants.NETWORK_ID)) {
                state.setNetworkId(tag.getInt(FluxConstants.NETWORK_ID));
            }
            if (tag.contains(FluxConstants.CUSTOM_NAME)) {
                state.setCustomName(tag.getString(FluxConstants.CUSTOM_NAME));
            }
            if (tag.hasUUID(FluxConstants.PLAYER_UUID)) {
                state.setOwner(tag.getUUID(FluxConstants.PLAYER_UUID));
            }
            if (tag.contains(FluxConstants.FORCED_LOADING)) {
                state.setChunkLoading(tag.getBoolean(FluxConstants.FORCED_LOADING));
            }
            if (tag.contains(FluxConstants.CHUNK_LOADED)) {
                state.setChunkLoaded(tag.getBoolean(FluxConstants.CHUNK_LOADED));
            }
            getFluxTransferHandler().changeSettings(tag);
            return;
        }

        state.setNetworkId(tag.getInt(FluxConstants.NETWORK_ID));
        state.setCustomName(tag.getString(FluxConstants.CUSTOM_NAME));
        if (tag.hasUUID(FluxConstants.PLAYER_UUID)) {
            state.setOwner(tag.getUUID(FluxConstants.PLAYER_UUID));
        }
        state.setChunkLoading(tag.getBoolean(FluxConstants.FORCED_LOADING));
        state.setChunkLoaded(!tag.contains(FluxConstants.CHUNK_LOADED) || tag.getBoolean(FluxConstants.CHUNK_LOADED));
        getFluxTransferHandler().readCustomTag(tag, type);
    }

    public String getCustomName() {
        return state.getCustomName();
    }

    public void setCustomName(String customName) {
        state.setCustomName(customName);
    }

    public boolean isForcedLoading() {
        return state.isChunkLoading();
    }

    public void setChunkLoading(boolean chunkLoading) {
        state.setChunkLoading(chunkLoading);
    }

    public boolean isChunkLoaded() {
        return state.isChunkLoaded();
    }

    public void setChunkLoaded(boolean chunkLoaded) {
        state.setChunkLoaded(chunkLoaded);
    }

    public long getMaxTransferLimit() {
        return Long.MAX_VALUE;
    }

    public boolean isActive() {
        return true;
    }

    public boolean canAccess(Player player) {
        UUID owner = state.getOwner();
        if (state.getNetworkId() < 0) {
            return true;
        }
        return owner.getMostSignificantBits() == 0L && owner.getLeastSignificantBits() == 0L
            || owner.equals(player.getUUID());
    }

    public void open(Player player) {
        playerUsing = player;
    }

    public void close(Player player) {
        if (playerUsing == player) {
            playerUsing = null;
        }
    }

    public Player getPlayerUsing() {
        return playerUsing;
    }

    public BlockPos getFluxGuiPos() {
        return host.getFluxGuiPos();
    }

    public Level getFluxGuiLevel() {
        return host.getFluxGuiLevel();
    }

    public GlobalPos getGlobalPos() {
        return GlobalPos.of(getFluxGuiLevel().dimension(), getFluxGuiPos());
    }

    public int getFolderID() {
        return host.getFluxGuiFolderId();
    }

    public ItemStack getDisplayStack() {
        return host.getFluxGuiDisplayStack();
    }

    public AbstractFluxTransferHandler getFluxTransferHandler() {
        return host.getFluxGuiTransferHandler();
    }

    public interface Host {

        BlockPos getFluxGuiPos();

        Level getFluxGuiLevel();

        ItemStack getFluxGuiDisplayStack();

        AbstractFluxTransferHandler getFluxGuiTransferHandler();

        int getFluxGuiFolderId();

        void onFluxGuiDataChanged();

        IFluxGuiConnector getFluxConnector();
    }
}