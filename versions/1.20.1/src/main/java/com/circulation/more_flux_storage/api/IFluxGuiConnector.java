package com.circulation.more_flux_storage.api;

import com.circulation.more_flux_storage.menu.FluxGuiConnectorMenu;
import com.circulation.more_flux_storage.util.AbstractFluxTransferHandler;
import com.circulation.more_flux_storage.util.FluxGuiConnectorData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import sonar.fluxnetworks.api.device.FluxDeviceType;
import sonar.fluxnetworks.api.device.IFluxStorage;

import java.util.UUID;

public interface IFluxGuiConnector extends IFluxStorage, FluxGuiConnectorLogic {

    FluxGuiConnectorData getFluxData();

    default AbstractFluxTransferHandler getFluxCompatTransferHandler() {
        return getFluxData().getFluxTransferHandler();
    }

    @NotNull
    @Override
    default FluxDeviceType getDeviceType() {
        return FluxDeviceType.STORAGE;
    }

    @Override
    default AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
        return new FluxGuiConnectorMenu(containerId, inventory, this);
    }

    @Override
    default long getMaxTransferLimit() {
        return getFluxData().getMaxTransferLimit();
    }

    @Override
    default int getNetworkID() {
        return getFluxData().getNetworkID();
    }

    @NotNull
    @Override
    default UUID getOwnerUUID() {
        return getFluxData().getConnectionOwner();
    }

    default void onPlayerOpened(@NotNull Player player) {
        getFluxData().open(player);
    }

    default void onPlayerClosed(@NotNull Player player) {
        getFluxData().close(player);
    }

    @Override
    default void writeCustomTag(@NotNull CompoundTag tag, byte type) {
        getFluxData().writeCustomTag(tag, type);
    }

    @Override
    default void readCustomTag(@NotNull CompoundTag tag, byte type) {
        getFluxData().readCustomTag(tag, type);
    }

    @Override
    default int getLogicPriority() {
        return getFluxCompatTransferHandler().getLogicPriority();
    }

    @Override
    default int getRawPriority() {
        return getFluxCompatTransferHandler().getRawPriority();
    }

    @Override
    default void setRawPriority(int priority) {
        getFluxCompatTransferHandler().setRawPriority(priority);
        getFluxData().sync();
    }

    @Override
    default long getLogicLimit() {
        return getFluxCompatTransferHandler().getLogicLimit();
    }

    @Override
    default long getRawLimit() {
        return getFluxCompatTransferHandler().getRawLimit();
    }

    @Override
    default void setRawLimit(long limit) {
        getFluxCompatTransferHandler().setRawLimit(sanitizeFluxGuiLimit(limit));
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

    @NotNull
    @Override
    default GlobalPos getGlobalPos() {
        return getFluxData().getGlobalPos();
    }

    @Override
    default boolean isForcedLoading() {
        return getFluxData().isForcedLoading();
    }

    @Override
    default int getFolderID() {
        return getFluxData().getFolderID();
    }

    @NotNull
    @Override
    default String getCustomName() {
        return getFluxData().getCustomName();
    }

    @Override
    default void setCustomName(String customName) {
        getFluxData().setCustomName(customName);
        getFluxData().sync();
    }

    @Override
    default boolean getDisableLimit() {
        return getFluxCompatTransferHandler().getDisableLimit();
    }

    @Override
    default void setDisableLimit(boolean disabled) {
        getFluxCompatTransferHandler().setDisableLimit(disabled);
        getFluxData().sync();
    }

    @Override
    default boolean getSurgeMode() {
        return getFluxCompatTransferHandler().getSurgeMode();
    }

    @Override
    default void setSurgeMode(boolean surgeMode) {
        getFluxCompatTransferHandler().setSurgeMode(surgeMode);
        getFluxData().sync();
    }

    @Override
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
        return getFluxCompatTransferHandler().getBuffer();
    }

    @Override
    default long getTransferChange() {
        return getFluxCompatTransferHandler().getChange();
    }

    @NotNull
    default ItemStack getDisplayStack() {
        return getFluxData().getDisplayStack();
    }

    default BlockPos getFluxGuiPos() {
        return getFluxData().getFluxGuiPos();
    }

    default Level getFluxGuiLevel() {
        return getFluxData().getFluxGuiLevel();
    }

    default boolean canOpenFluxGui(Player player) {
        return getFluxData().canAccess(player);
    }
}