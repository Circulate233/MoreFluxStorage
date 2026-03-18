package com.circulation.more_flux_storage.blockentity;

import com.circulation.more_flux_storage.api.IFluxGuiConnector;
import com.circulation.more_flux_storage.block.BlockInductionPortFlux;
import com.circulation.more_flux_storage.registry.MoreFluxStorageContent;
import com.circulation.more_flux_storage.util.AbstractFluxTransferHandler;
import com.circulation.more_flux_storage.util.FluxGuiConnectorData;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.common.content.matrix.MatrixEnergyContainer;
import mekanism.common.content.matrix.MatrixMultiblockData;
import mekanism.common.tile.multiblock.TileEntityInductionPort;
import net.minecraft.core.BlockPos;
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

    private static final double MEKANISM_TO_FLUX = 0.4D;
    private static final double FLUX_TO_MEKANISM = 1D / MEKANISM_TO_FLUX;

    private final FluxGuiConnectorData data = new FluxGuiConnectorData(this);
    private final InductionPortTransferHandler transferHandler = new InductionPortTransferHandler();
    private final IFluxGuiConnector fluxConnector = new FluxConnector();

    public TileInductionPortFlux(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    private static long toFlux(FloatingLong amount) {
        if (amount.isZero()) {
            return 0L;
        }
        return Math.max(0L, amount.multiply(MEKANISM_TO_FLUX).longValue());
    }

    private static FloatingLong toMekanism(long amount) {
        if (amount <= 0L) {
            return FloatingLong.createConst(0L);
        }
        return FloatingLong.createConst(amount).multiply(FLUX_TO_MEKANISM);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        data.writeCustomTag(tag, FluxConstants.NBT_SAVE_ALL);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        data.readCustomTag(tag, FluxConstants.NBT_SAVE_ALL);
        transferHandler.syncBufferFromMatrix();
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
        transferHandler.syncBufferFromMatrix();
    }

    @NotNull
    @Override
    public Component getDisplayName() {
        return Component.translatable(MoreFluxStorageContent.INDUCTION_PORT_FLUX.get().getDescriptionId());
    }

    public IFluxGuiConnector getFluxConnector() {
        return fluxConnector;
    }

    public InteractionResult onSneakRightClick(Player player) {
        if (level == null) return InteractionResult.PASS;
        boolean newActive = !getActive();
        setActive(newActive);
        BlockState state = getBlockState();
        if (state.hasProperty(BlockInductionPortFlux.ACTIVE)) {
            level.setBlockAndUpdate(worldPosition, state.setValue(BlockInductionPortFlux.ACTIVE, newActive));
        }
        return InteractionResult.SUCCESS;
    }

    public long getFluxEnergyStored() {
        return toFlux(getMatrixEnergyContainer().getEnergy());
    }

    public long getFluxMaxEnergyStored() {
        return toFlux(getMatrixEnergyContainer().getMaxEnergy());
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
            long matrixLimit = toFlux(getMatrixEnergyContainer().getMaxTransfer());
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
            long freeSpace = toFlux(energyContainer.getMaxEnergy().subtract(energyContainer.getEnergy()));
            return Math.max(0L, Math.min(freeSpace, getEffectiveFluxTransferLimit()));
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
            FloatingLong requested = toMekanism(allowed);
            FloatingLong remainder = energyContainer.insert(requested, Action.EXECUTE, AutomationType.INTERNAL);
            long accepted = toFlux(requested.subtract(remainder));
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
            FloatingLong extracted = energyContainer.extract(toMekanism(allowed), Action.EXECUTE, AutomationType.INTERNAL);
            long removed = super.removeFromBuffer(toFlux(extracted));
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