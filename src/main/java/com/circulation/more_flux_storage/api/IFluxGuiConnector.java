package com.circulation.more_flux_storage.api;

import com.circulation.more_flux_storage.util.FluxGuiConnectorData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.fluxnetworks.api.network.ConnectionType;
import sonar.fluxnetworks.api.network.IFluxNetwork;
import sonar.fluxnetworks.api.tiles.IFluxStorage;
import sonar.fluxnetworks.api.utils.Coord4D;
import sonar.fluxnetworks.api.utils.NBTType;

import java.util.UUID;

public interface IFluxGuiConnector extends IFluxStorage {

    FluxGuiConnectorData getData();

    default long getMaxTransferLimit() {
        return getData().getMaxTransferLimit();
    }

    @Override
    default int getNetworkID() {
        return getData().getNetworkID();
    }

    @Override
    default IFluxNetwork getNetwork() {
        return getData().getNetwork();
    }

    @Override
    default void open(EntityPlayer player) {
        getData().open(player);
    }

    @Override
    default void close(EntityPlayer player) {
        getData().close(player);
    }

    @Override
    default NBTTagCompound writeCustomNBT(NBTTagCompound tag, NBTType type) {
        getData().writeCustomNBT(tag, type);
        return tag;
    }

    @Override
    default void readCustomNBT(NBTTagCompound tag, NBTType type) {
        getData().readCustomNBT(tag, type);
    }

    @Override
    default int getLogicPriority() {
        return getData().getLogicPriority();
    }

    @Override
    default int getRawPriority() {
        return getData().getRawPriority();
    }

    default void setRawPriority(int priority) {
        getData().setRawPriority(priority);
        getData().sync();
    }

    @Override
    default UUID getConnectionOwner() {
        return getData().getConnectionOwner();
    }

    @Override
    default ConnectionType getConnectionType() {
        return getData().getConnectionType();
    }

    @Override
    default boolean canAccess(EntityPlayer player) {
        return getData().canAccess(player);
    }

    @Override
    default long getLogicLimit() {
        return getData().getLogicLimit();
    }

    @Override
    default long getRawLimit() {
        return getData().getRawLimit();
    }

    default void setRawLimit(long limit) {
        getData().setRawLimit(sanitizeFluxGuiLimit(limit));
        getData().sync();
    }

    @Override
    default boolean isActive() {
        return getData().isActive();
    }

    @Override
    default boolean isChunkLoaded() {
        return getData().isChunkLoaded();
    }

    @Override
    default boolean isForcedLoading() {
        return getData().isForcedLoading();
    }

    default void connect(IFluxNetwork network) {
        getData().connect(network);
    }

    default void disconnect(IFluxNetwork network) {
        getData().disconnect(network);
    }

    @Override
    default World getFluxWorld() {
        return getData().getFluxWorld();
    }

    @Override
    default Coord4D getCoords() {
        return getData().getCoords();
    }

    @Override
    default int getFolderID() {
        return getData().getFolderID();
    }

    @Override
    default String getCustomName() {
        return getData().getCustomName();
    }

    default void setCustomName(String customName) {
        getData().setCustomName(customName);
        getData().sync();
    }

    @Override
    default boolean getDisableLimit() {
        return getData().getDisableLimit();
    }

    default void setDisableLimit(boolean disabled) {
        getData().setDisableLimit(disabled);
        getData().sync();
    }

    @Override
    default boolean getSurgeMode() {
        return getData().getSurgeMode();
    }

    default void setSurgeMode(boolean surgeMode) {
        getData().setSurgeMode(surgeMode);
        getData().sync();
    }

    default void setChunkLoading(boolean chunkLoading) {
        getData().setChunkLoading(chunkLoading);
        getData().sync();
    }

    default void setPlayerUUID(UUID uuid) {
        getData().setPlayerUUID(uuid);
        getData().sync();
    }

    @Override
    default long getTransferBuffer() {
        return getData().getTransferBuffer();
    }

    @Override
    default long getTransferChange() {
        return getData().getTransferChange();
    }

    @Override
    default ItemStack getDisplayStack() {
        return getData().getDisplayStack();
    }

    default BlockPos getFluxGuiPos() {
        return getCoords().getPos();
    }

    default World getFluxGuiWorld() {
        return getFluxWorld();
    }

    default boolean shouldShowFluxGuiChunkLoading() {
        return !getConnectionType().isStorage();
    }

    default boolean canOpenFluxGui(EntityPlayer player) {
        return canAccess(player);
    }

    default long sanitizeFluxGuiLimit(long requestedLimit) {
        return Math.min(Math.max(0L, requestedLimit), getMaxTransferLimit());
    }

    default int getFluxGuiDimension() {
        World world = getFluxWorld();
        return world == null ? 0 : world.provider.getDimension();
    }

    default void onFluxGuiAction(EntityPlayer player, FluxGuiActionType action) {
    }
}