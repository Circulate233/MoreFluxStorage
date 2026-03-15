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

    FluxGuiConnectorData getFluxData();

    default long getMaxTransferLimit() {
        return getFluxData().getMaxTransferLimit();
    }

    @Override
    default int getNetworkID() {
        return getFluxData().getNetworkID();
    }

    @Override
    default IFluxNetwork getNetwork() {
        return getFluxData().getNetwork();
    }

    @Override
    default void open(EntityPlayer player) {
        getFluxData().open(player);
    }

    @Override
    default void close(EntityPlayer player) {
        getFluxData().close(player);
    }

    @Override
    default NBTTagCompound writeCustomNBT(NBTTagCompound tag, NBTType type) {
        getFluxData().writeCustomNBT(tag, type);
        return tag;
    }

    @Override
    default void readCustomNBT(NBTTagCompound tag, NBTType type) {
        getFluxData().readCustomNBT(tag, type);
    }

    @Override
    default int getLogicPriority() {
        return getFluxData().getLogicPriority();
    }

    @Override
    default int getRawPriority() {
        return getFluxData().getRawPriority();
    }

    default void setRawPriority(int priority) {
        getFluxData().setRawPriority(priority);
        getFluxData().sync();
    }

    @Override
    default UUID getConnectionOwner() {
        return getFluxData().getConnectionOwner();
    }

    @Override
    default ConnectionType getConnectionType() {
        return getFluxData().getConnectionType();
    }

    @Override
    default boolean canAccess(EntityPlayer player) {
        return getFluxData().canAccess(player);
    }

    @Override
    default long getLogicLimit() {
        return getFluxData().getLogicLimit();
    }

    @Override
    default long getRawLimit() {
        return getFluxData().getRawLimit();
    }

    default void setRawLimit(long limit) {
        getFluxData().setRawLimit(sanitizeFluxGuiLimit(limit));
        getFluxData().sync();
    }

    @Override
    default boolean isActive() {
        return getFluxData().isActive();
    }

    @Override
    default boolean isChunkLoaded() {
        return getFluxData().isChunkLoaded();
    }

    @Override
    default boolean isForcedLoading() {
        return getFluxData().isForcedLoading();
    }

    default void connect(IFluxNetwork network) {
        getFluxData().connect(network);
    }

    default void disconnect(IFluxNetwork network) {
        getFluxData().disconnect(network);
    }

    @Override
    default World getFluxWorld() {
        return getFluxData().getFluxWorld();
    }

    @Override
    default Coord4D getCoords() {
        return getFluxData().getCoords();
    }

    @Override
    default int getFolderID() {
        return getFluxData().getFolderID();
    }

    @Override
    default String getCustomName() {
        return getFluxData().getCustomName();
    }

    default void setCustomName(String customName) {
        getFluxData().setCustomName(customName);
        getFluxData().sync();
    }

    @Override
    default boolean getDisableLimit() {
        return getFluxData().getDisableLimit();
    }

    default void setDisableLimit(boolean disabled) {
        getFluxData().setDisableLimit(disabled);
        getFluxData().sync();
    }

    @Override
    default boolean getSurgeMode() {
        return getFluxData().getSurgeMode();
    }

    default void setSurgeMode(boolean surgeMode) {
        getFluxData().setSurgeMode(surgeMode);
        getFluxData().sync();
    }

    default void setChunkLoading(boolean chunkLoading) {
        getFluxData().setChunkLoading(chunkLoading);
        getFluxData().sync();
    }

    default void setPlayerUUID(UUID uuid) {
        getFluxData().setPlayerUUID(uuid);
        getFluxData().sync();
    }

    @Override
    default long getTransferBuffer() {
        return getFluxData().getTransferBuffer();
    }

    @Override
    default long getTransferChange() {
        return getFluxData().getTransferChange();
    }

    @Override
    default ItemStack getDisplayStack() {
        return getFluxData().getDisplayStack();
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