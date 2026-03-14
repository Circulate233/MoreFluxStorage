package com.circulation.more_flux_storage.util;

import com.circulation.more_flux_storage.api.IFluxGuiConnector;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
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
    public Set<EntityPlayer> playerUsing = new ObjectOpenHashSet<>();
    @Getter
    private IFluxNetwork network = FluxNetworkInvalid.instance;
    private int networkId = -1;
    private UUID owner = new UUID(0L, 0L);
    @Getter
    @Setter
    private String customName = "Flux Storage";
    private int priority;
    private long limit = 200000L;
    @Setter
    private boolean surgeMode;
    @Setter
    private boolean disableLimit;
    @Setter
    private boolean chunkLoading;
    @Getter
    @Setter
    private boolean chunkLoaded = true;
    private boolean load;

    public FluxGuiConnectorData(Host host) {
        this.host = host;
    }

    public void sync() {
        host.onFluxGuiDataChanged();
    }

    public void setPlayerUUID(UUID uuid) {
        this.owner = uuid;
    }

    public int getNetworkID() {
        return networkId;
    }

    public void writeCustomNBT(NBTTagCompound tag, NBTType type) {
        tag.setInteger(TAG_NETWORK_ID, getNetworkID());
        tag.setUniqueId(TAG_OWNER, owner);
        tag.setString(TAG_CUSTOM_NAME, customName);
        tag.setInteger(TAG_PRIORITY, priority);
        tag.setLong(TAG_LIMIT, limit);
        tag.setBoolean(TAG_SURGE, surgeMode);
        tag.setBoolean(TAG_DISABLE_LIMIT, disableLimit);
        tag.setBoolean(TAG_CHUNK_LOADING, chunkLoading);
        tag.setBoolean(TAG_CHUNK_LOADED, chunkLoaded);
        host.getFluxGuiTransferHandler().writeCustomNBT(tag, type);
    }

    public void readCustomNBT(NBTTagCompound tag, NBTType type) {
        networkId = tag.getInteger(TAG_NETWORK_ID);
        customName = tag.getString(TAG_CUSTOM_NAME);
        priority = tag.getInteger(TAG_PRIORITY);
        limit = tag.getLong(TAG_LIMIT);
        surgeMode = tag.getBoolean(TAG_SURGE);
        disableLimit = tag.getBoolean(TAG_DISABLE_LIMIT);
        chunkLoading = tag.getBoolean(TAG_CHUNK_LOADING);
        chunkLoaded = !tag.hasKey(TAG_CHUNK_LOADED) || tag.getBoolean(TAG_CHUNK_LOADED);
        if (tag.hasUniqueId(TAG_OWNER)) {
            owner = tag.getUniqueId(TAG_OWNER);
        }
        host.getFluxGuiTransferHandler().readCustomNBT(tag, type);
    }

    public void update() {
        if (!this.getFluxWorld().isRemote) {
            if (!this.playerUsing.isEmpty()) {
                this.sendPackets();
            }

            if (!this.load) {
                if (!FluxUtils.addConnection((IFluxGuiConnector) this.host)) {
                    this.networkId = -1;
                }

                this.sendPackets();
                this.load = true;
            }
        }

    }

    public void onChunkUnload() {
        if (!this.getFluxWorld().isRemote && this.load) {
            FluxUtils.removeConnection((IFluxGuiConnector) this.host, true);
            host.getFluxGuiTransferHandler().reset();
            this.load = false;
        }
    }

    public void invalidate() {
        if (!this.getFluxWorld().isRemote && this.load) {
            FluxUtils.removeConnection((IFluxGuiConnector) this.host, false);
            if (this.chunkLoading) {
                FluxChunkManager.releaseChunk(this.getFluxWorld(), new ChunkPos(this.getCoords().getPos()));
            }
            host.getFluxGuiTransferHandler().reset();
            this.load = false;
        }
    }

    public int getLogicPriority() {
        return priority;
    }

    public int getRawPriority() {
        return priority;
    }

    public void setRawPriority(int priority) {
        this.priority = priority;
    }

    public UUID getConnectionOwner() {
        return owner;
    }

    public ConnectionType getConnectionType() {
        return host.getFluxGuiConnectionType();
    }

    public boolean canAccess(EntityPlayer player) {
        if (!this.network.isInvalid()) {
            return EntityPlayer.getUUID(player.getGameProfile()).equals(this.owner) || this.network.getMemberPermission(player).canAccess();
        } else {
            return true;
        }
    }

    public long getLogicLimit() {
        return disableLimit ? Long.MAX_VALUE : limit;
    }

    public long getRawLimit() {
        return limit;
    }

    public void setRawLimit(long limit) {
        this.limit = limit;
    }

    public long getMaxTransferLimit() {
        return Long.MAX_VALUE;
    }

    public boolean isActive() {
        return true;
    }

    public boolean isForcedLoading() {
        return chunkLoading;
    }

    public void connect(@Nonnull IFluxNetwork network) {
        this.network = network;
        this.networkId = network.getNetworkID();
    }

    public void open(EntityPlayer player) {
        if (!this.getFluxWorld().isRemote) {
            this.playerUsing.add(player);
            this.sendPackets();
        }
    }

    public void close(EntityPlayer player) {
        if (!this.getFluxWorld().isRemote) {
            this.playerUsing.remove(player);
        }
    }

    public void sendPackets() {
        var pos = this.getCoords().getPos();
        IBlockState state = this.getFluxWorld().getBlockState(pos);
        this.getFluxWorld().notifyBlockUpdate(pos, state, state, 3);
    }

    public void disconnect(IFluxNetwork network) {
        if (network != null && network.getNetworkID() == getNetworkID()) {
            this.network = FluxNetworkInvalid.instance;
            this.networkId = -1;
        }
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

    public boolean getDisableLimit() {
        return disableLimit;
    }

    public boolean getSurgeMode() {
        return surgeMode;
    }

    public long getTransferBuffer() {
        return host.getFluxGuiTransferHandler().getBuffer();
    }

    public long getTransferChange() {
        return host.getFluxGuiTransferHandler().getChange();
    }

    public ItemStack getDisplayStack() {
        return host.getFluxGuiDisplayStack();
    }

    public interface Host {

        TileEntity getFluxGuiTileEntity();

        ConnectionType getFluxGuiConnectionType();

        ItemStack getFluxGuiDisplayStack();

        ITransferHandler getFluxGuiTransferHandler();

        default int getFluxGuiFolderId() {
            return -1;
        }

        default void onFluxGuiDataChanged() {
            TileEntity tileEntity = getFluxGuiTileEntity();
            tileEntity.markDirty();
            if (tileEntity.getWorld() != null && !tileEntity.getWorld().isRemote) {
                tileEntity.getWorld().notifyBlockUpdate(tileEntity.getPos(), tileEntity.getWorld().getBlockState(tileEntity.getPos()),
                    tileEntity.getWorld().getBlockState(tileEntity.getPos()), 3);
            }
        }
    }
}