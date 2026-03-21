package com.circulation.more_flux_storage.blockentity;

import com.circulation.more_flux_storage.api.IFluxGuiConnector;
import com.circulation.more_flux_storage.registry.MoreFluxStorageContent;
import com.circulation.more_flux_storage.util.AbstractFluxTransferHandler;
import com.circulation.more_flux_storage.util.FluxGuiConnectorData;
import com.circulation.more_flux_storage.util.Utils;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.common.content.matrix.MatrixEnergyContainer;
import mekanism.common.content.matrix.MatrixMultiblockData;
import mekanism.common.tile.multiblock.TileEntityInductionPort;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import sonar.fluxnetworks.api.FluxConstants;

public class TileInductionPortFlux extends TileEntityInductionPort implements FluxGuiConnectorData.Host {

    private final FluxGuiConnectorData data = new FluxGuiConnectorData(this);
    private final InductionPortTransferHandler transferHandler = new InductionPortTransferHandler();
    private final IFluxGuiConnector fluxConnector = new FluxConnector();

    public TileInductionPortFlux(BlockPos pos, BlockState state) {
        super(Utils.trigger(pos), state);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        data.writeCustomTag(tag, FluxConstants.NBT_SAVE_ALL);
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        data.readCustomTag(tag, FluxConstants.NBT_SAVE_ALL);
        transferHandler.syncBufferFromMatrix();
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        data.writeCustomTag(tag, FluxConstants.NBT_TILE_UPDATE);
        return tag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        data.readCustomTag(tag, FluxConstants.NBT_TILE_UPDATE);
        transferHandler.syncBufferFromMatrix();
    }

    public IFluxGuiConnector getFluxConnector() {
        return fluxConnector;
    }

    public InteractionResult onSneakRightClick(Player player) {
        if (level == null) return InteractionResult.PASS;
        setActive(!getActive());
        return InteractionResult.SUCCESS;
    }

    public long getFluxEnergyStored() {
        return getMatrixEnergyContainer().getEnergy();
    }

    public long getFluxMaxEnergyStored() {
        return getMatrixEnergyContainer().getMaxEnergy();
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
        return new ItemStack(MoreFluxStorageContent.INDUCTION_PORT_FLUX_ITEM.get());
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
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private MatrixEnergyContainer getMatrixEnergyContainer() {
        MatrixMultiblockData multiblock = getMultiblock();
        return multiblock.getEnergyContainer();
    }

    private final class InductionPortTransferHandler extends AbstractFluxTransferHandler {

        private void syncBufferFromMatrix() {
            setBuffer(getFluxEnergyStored());
        }

        private long getEffectiveFluxTransferLimit() {
            long matrixLimit = getMatrixEnergyContainer().getMaxTransfer();
            if (getDisableLimit()) {
                return matrixLimit;
            }
            return Math.min(matrixLimit, getLogicLimit());
        }

        @Override
        public void onCycleStart() {
            syncBufferFromMatrix();
        }

        @Override
        public long getRequest() {
            if (getActive()) {
                return 0L;
            }

            MatrixEnergyContainer energyContainer = getMatrixEnergyContainer();
            long freeSpace = Math.max(0L, energyContainer.getMaxEnergy() - energyContainer.getEnergy());
            return Math.min(freeSpace, getEffectiveFluxTransferLimit());
        }

        @Override
        public void addToBuffer(long amount) {
            if (getActive()) {
                return;
            }

            long allowed = Math.min(Math.max(0L, amount), getEffectiveFluxTransferLimit());
            if (allowed <= 0L) {
                return;
            }

            MatrixEnergyContainer energyContainer = getMatrixEnergyContainer();
            long remainder = energyContainer.insert(allowed, Action.EXECUTE, AutomationType.INTERNAL);
            long accepted = allowed - remainder;
            super.addToBuffer(accepted);
            syncBufferFromMatrix();
            if (accepted > 0L) {
                onFluxGuiDataChanged();
            }
        }

        @Override
        public long removeFromBuffer(long amount) {
            if (!getActive()) {
                return 0L;
            }

            long allowed = Math.min(Math.max(0L, amount), getEffectiveFluxTransferLimit());
            if (allowed <= 0L) {
                return 0L;
            }

            MatrixEnergyContainer energyContainer = getMatrixEnergyContainer();
            long extracted = energyContainer.extract(allowed, Action.EXECUTE, AutomationType.INTERNAL);
            long removed = super.removeFromBuffer(extracted);
            syncBufferFromMatrix();
            if (removed > 0L) {
                onFluxGuiDataChanged();
            }
            return removed;
        }
    }

    private final class FluxConnector implements IFluxGuiConnector {

        @Override
        public FluxGuiConnectorData getFluxData() {
            return data;
        }

        @Override
        public @NotNull Component getDisplayName() {
            return Component.translatable(MoreFluxStorageContent.INDUCTION_PORT_FLUX.get().getDescriptionId());
        }

        @NotNull
        @Override
        public ItemStack getDisplayStack() {
            return getFluxGuiDisplayStack();
        }
    }
}