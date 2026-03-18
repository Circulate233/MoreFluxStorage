package com.circulation.more_flux_storage.util;

import com.circulation.more_flux_storage.api.IFluxGuiConnector;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import sonar.fluxnetworks.api.network.ConnectionType;
import sonar.fluxnetworks.api.network.IFluxNetwork;
import sonar.fluxnetworks.api.network.ITransferHandler;
import sonar.fluxnetworks.api.utils.Coord4D;
import sonar.fluxnetworks.api.utils.NBTType;
import sonar.fluxnetworks.common.connection.FluxNetworkInvalid;
import sonar.fluxnetworks.common.core.FluxUtils;
import sonar.fluxnetworks.common.data.FluxChunkManager;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unused")
public class FluxGuiConnectorData {

    public static final String TAG_NETWORK_ID = "0";
    public static final String TAG_OWNER = "3";
    public static final String TAG_CUSTOM_NAME = "4";
    public static final String TAG_PRIORITY = "5";
    public static final String TAG_LIMIT = "6";
    public static final String TAG_SURGE = "7";
    public static final String TAG_DISABLE_LIMIT = "8";
    public static final String TAG_CHUNK_LOADING = "9";
    public static final String TAG_CHUNK_LOADED = "10";

    private final Host host;
    private final FluxGuiState state = new FluxGuiState();
    private final Set<EntityPlayer> playerUsing = new ObjectOpenHashSet<>();
    private IFluxNetwork network = FluxNetworkInvalid.instance;

    public IFluxNetwork getNetwork() {
        return network;
    }
    private boolean load;

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

    public String getCustomName() {
        return state.getCustomName();
    }

    public void setCustomName(String customName) {
        state.setCustomName(customName);
    }

    public int getLogicPriority() {
        return getFluxTransferHandler().getLogicPriority();
    }

    public int getRawPriority() {
        return getFluxTransferHandler().getRawPriority();
    }

    public void setRawPriority(int priority) {
        getFluxTransferHandler().setRawPriority(priority);
    }

    public UUID getConnectionOwner() {
        return state.getOwner();
    }

    public long getLogicLimit() {
        return getFluxTransferHandler().getLogicLimit();
    }

    public long getRawLimit() {
        return getFluxTransferHandler().getRawLimit();
    }

    public void setRawLimit(long limit) {
        getFluxTransferHandler().setRawLimit(limit);
    }

    public boolean getSurgeMode() {
        return getFluxTransferHandler().getSurgeMode();
    }

    public void setSurgeMode(boolean surgeMode) {
        getFluxTransferHandler().setSurgeMode(surgeMode);
    }

    public boolean getDisableLimit() {
        return getFluxTransferHandler().getDisableLimit();
    }

    public void setDisableLimit(boolean disableLimit) {
        getFluxTransferHandler().setDisableLimit(disableLimit);
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

    public void writeCustomNBT(NBTTagCompound tag, NBTType type) {
        tag.setInteger(TAG_NETWORK_ID, getNetworkID());
        tag.setUniqueId(TAG_OWNER, state.getOwner());
        tag.setString(TAG_CUSTOM_NAME, state.getCustomName());
        tag.setBoolean(TAG_CHUNK_LOADING, state.isChunkLoading());
        tag.setBoolean(TAG_CHUNK_LOADED, state.isChunkLoaded());
        getFluxTransferHandler().writeCustomNBT(tag, type);
    }

    public void readCustomNBT(NBTTagCompound tag, NBTType type) {
        state.setNetworkId(tag.getInteger(TAG_NETWORK_ID));
        state.setCustomName(tag.getString(TAG_CUSTOM_NAME));
        state.setChunkLoading(tag.getBoolean(TAG_CHUNK_LOADING));
        state.setChunkLoaded(!tag.hasKey(TAG_CHUNK_LOADED) || tag.getBoolean(TAG_CHUNK_LOADED));
        if (tag.hasUniqueId(TAG_OWNER)) {
            state.setOwner(tag.getUniqueId(TAG_OWNER));
        }
        getFluxTransferHandler().readCustomNBT(tag, type);
    }

    public void update() {
        if (!getFluxWorld().isRemote) {
            if (!playerUsing.isEmpty()) {
                sendPackets();
            }

            if (!load) {
                if (!FluxUtils.addConnection((IFluxGuiConnector) host)) {
                    state.setNetworkId(-1);
                }

                sendPackets();
                load = true;
            }
        }
    }

    public void onChunkUnload() {
        if (!getFluxWorld().isRemote && load) {
            FluxUtils.removeConnection((IFluxGuiConnector) host, true);
            host.getFluxGuiTransferHandler().reset();
            load = false;
        }
    }

    public void invalidate() {
        if (!getFluxWorld().isRemote && load) {
            FluxUtils.removeConnection((IFluxGuiConnector) host, false);
            if (state.isChunkLoading()) {
                FluxChunkManager.releaseChunk(getFluxWorld(), new ChunkPos(getCoords().getPos()));
            }
            host.getFluxGuiTransferHandler().reset();
            load = false;
        }
    }

    public long getMaxTransferLimit() {
        return Long.MAX_VALUE;
    }

    public boolean isActive() {
        return true;
    }

    public ConnectionType getConnectionType() {
        return host.getFluxGuiConnectionType();
    }

    public boolean canAccess(EntityPlayer player) {
        if (!network.isInvalid()) {
            return EntityPlayer.getUUID(player.getGameProfile()).equals(state.getOwner()) || network.getMemberPermission(player).canAccess();
        }
        return true;
    }

    public void connect(@Nonnull IFluxNetwork network) {
        this.network = network;
        state.setNetworkId(network.getNetworkID());
    }

    public void disconnect(IFluxNetwork network) {
        if (network != null && network.getNetworkID() == getNetworkID()) {
            this.network = FluxNetworkInvalid.instance;
            state.setNetworkId(-1);
        }
    }

    public void open(EntityPlayer player) {
        if (!getFluxWorld().isRemote) {
            playerUsing.add(player);
            sendPackets();
        }
    }

    public void close(EntityPlayer player) {
        if (!getFluxWorld().isRemote) {
            playerUsing.remove(player);
        }
    }

    public void sendPackets() {
        BlockPos pos = getCoords().getPos();
        IBlockState state = getFluxWorld().getBlockState(pos);
        getFluxWorld().notifyBlockUpdate(pos, state, state, 3);
    }

    public net.minecraft.world.World getFluxWorld() {
        return host.getFluxGuiTileEntity().getWorld();
    }

    public Coord4D getCoords() {
        return new Coord4D(host.getFluxGuiTileEntity());
    }

    public int getFolderID() {
        return host.getFluxGuiFolderId();
    }

    public long getTransferBuffer() {
        return getFluxTransferHandler().getBuffer();
    }

    public long getTransferChange() {
        return getFluxTransferHandler().getChange();
    }

    private AbstractFluxTransferHandler getFluxTransferHandler() {
        return (AbstractFluxTransferHandler) host.getFluxGuiTransferHandler();
    }

    public ItemStack getDisplayStack() {
        return host.getFluxGuiDisplayStack();
    }

    public interface Host {

        TileEntity getFluxGuiTileEntity();

        ConnectionType getFluxGuiConnectionType();

        ItemStack getFluxGuiDisplayStack();

        ITransferHandler getFluxGuiTransferHandler();

        int getFluxGuiFolderId();

        void onFluxGuiDataChanged();
    }
}