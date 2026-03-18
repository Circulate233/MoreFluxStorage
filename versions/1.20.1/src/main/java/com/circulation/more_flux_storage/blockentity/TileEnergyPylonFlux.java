package com.circulation.more_flux_storage.blockentity;

import com.circulation.more_flux_storage.api.IFluxGuiConnector;
import com.brandon3055.draconicevolution.blocks.tileentity.TileEnergyCore;
import com.brandon3055.draconicevolution.blocks.tileentity.TileEnergyPylon;
import com.circulation.more_flux_storage.registry.MoreFluxStorageContent;
import com.circulation.more_flux_storage.util.AbstractFluxTransferHandler;
import com.circulation.more_flux_storage.util.FluxGuiConnectorData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import sonar.fluxnetworks.api.FluxConstants;

public class TileEnergyPylonFlux extends TileEnergyPylon implements FluxGuiConnectorData.Host {

    private final FluxGuiConnectorData data = new FluxGuiConnectorData(this);
    private final EnergyPylonTransferHandler transferHandler = new EnergyPylonTransferHandler();
    private final IFluxGuiConnector fluxConnector = new FluxConnector();
    private int fluxTick;

    public TileEnergyPylonFlux(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TileEnergyPylonFlux tile) {
        tile.tick();
    }

    @Override
    public void tick() {
        if (level != null && !level.isClientSide && fluxTick++ % 20 == 0 && getCore() == null) {
            selectNextCore();
        }

        super.tick();
        transferHandler.syncBufferFromCore();
    }

    public boolean hasCoreBinding() {
        return coreOffset.notNull();
    }

    @Override
    public void writeExtraNBT(CompoundTag tag) {
        super.writeExtraNBT(tag);
        data.writeCustomTag(tag, FluxConstants.NBT_SAVE_ALL);
    }

    @Override
    public void readExtraNBT(CompoundTag tag) {
        super.readExtraNBT(tag);
        data.readCustomTag(tag, FluxConstants.NBT_SAVE_ALL);
        transferHandler.syncBufferFromCore();
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        data.writeCustomTag(tag, FluxConstants.NBT_TILE_UPDATE);
        return tag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag) {
        super.handleUpdateTag(tag);
        data.readCustomTag(tag, FluxConstants.NBT_TILE_UPDATE);
        transferHandler.syncBufferFromCore();
    }

    @NotNull
    @Override
    public Component getDisplayName() {
        return Component.translatable(MoreFluxStorageContent.ENERGY_PYLON_FLUX.get().getDescriptionId());
    }

    public FluxGuiConnectorData getFluxData() {
        return data;
    }

    public IFluxGuiConnector getFluxConnector() {
        return fluxConnector;
    }

    @Override
    public BlockPos getFluxGuiPos() {
        return worldPosition;
    }

    @Override
    public Level getFluxGuiLevel() {
        return level;
    }

    @Override
    public ItemStack getFluxGuiDisplayStack() {
        return new ItemStack(MoreFluxStorageContent.ENERGY_PYLON_FLUX_ITEM.get());
    }

    @Override
    public AbstractFluxTransferHandler getFluxGuiTransferHandler() {
        return transferHandler;
    }

    @Override
    public int getFluxGuiFolderId() {
        return 0;
    }

    @Override
    public void onFluxGuiDataChanged() {
        dirtyBlock();
        updateBlock();
    }

    private final class EnergyPylonTransferHandler extends AbstractFluxTransferHandler {

        private void syncBufferFromCore() {
            TileEnergyCore core = getCore();
            setBuffer(core == null ? 0L : core.energy.getOPStored());
        }

        @Override
        public long getRequest() {
            TileEnergyCore core = getCore();
            if (core == null || !ioMode.get().canReceive()) {
                return 0L;
            }

            long freeSpace = Math.max(0L, core.energy.getMaxOPStored() - core.energy.getUncappedStored());
            return getDisableLimit() ? freeSpace : Math.min(freeSpace, getLogicLimit());
        }

        @Override
        public void addToBuffer(long amount) {
            TileEnergyCore core = getCore();
            if (core == null || !ioMode.get().canReceive()) {
                return;
            }

            long received = core.energy.receiveOP(Math.max(0L, amount), false);
            super.addToBuffer(received);
            syncBufferFromCore();
            if (received > 0L) {
                onFluxGuiDataChanged();
            }
        }

        @Override
        public long removeFromBuffer(long amount) {
            TileEnergyCore core = getCore();
            if (core == null || !ioMode.get().canExtract()) {
                return 0L;
            }

            long extracted = core.energy.extractOP(Math.max(0L, amount), false);
            long removed = super.removeFromBuffer(extracted);
            syncBufferFromCore();
            if (removed > 0L) {
                onFluxGuiDataChanged();
            }
            return removed;
        }

        @Override
        public void onCycleStart() {
            syncBufferFromCore();
        }
    }

    private final class FluxConnector implements IFluxGuiConnector {

        @Override
        public FluxGuiConnectorData getFluxData() {
            return data;
        }

        @Override
        public @NotNull Component getDisplayName() {
            return Component.translatable(MoreFluxStorageContent.ENERGY_PYLON_FLUX.get().getDescriptionId());
        }

        @NotNull
        @Override
        public ItemStack getDisplayStack() {
            return getFluxGuiDisplayStack();
        }
    }
}