package com.circulation.more_flux_storage.block;

import com.circulation.more_flux_storage.api.IFluxGuiConnector;
import com.circulation.more_flux_storage.blockentity.TileInductionPortFlux;
import com.circulation.more_flux_storage.registry.MoreFluxStorageContent;
import com.mojang.serialization.MapCodec;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockInductionPortFlux extends AbstractFluxGuiBlock {

    public static final MapCodec<BlockInductionPortFlux> CODEC = simpleCodec(BlockInductionPortFlux::new);

    public BlockInductionPortFlux() {
        this(BlockBehaviour.Properties.of().strength(5.0F, 12.0F));
    }

    private BlockInductionPortFlux(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull MapCodec<? extends AbstractFluxGuiBlock> codec() {
        return CODEC;
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new TileInductionPortFlux(pos, state);
    }

    @Override
    protected @Nullable IFluxGuiConnector getFluxConnector(@Nullable BlockEntity blockEntity) {
        return blockEntity instanceof TileInductionPortFlux tile ? tile.getFluxConnector() : null;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state,
                                                                             @NotNull BlockEntityType<T> blockEntityType) {
        return createTickerHelper(
            blockEntityType,
            MoreFluxStorageContent.INDUCTION_PORT_FLUX_BLOCK_ENTITY.get(),
            level.isClientSide ? TileEntityMekanism::tickClient : TileEntityMekanism::tickServer
        );
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                                        @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof TileInductionPortFlux tile) {
                return tile.onSneakRightClick(player);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof TileInductionPortFlux tile)) {
            return 0;
        }

        long capacity = tile.getFluxMaxEnergyStored();
        if (capacity <= 0L) {
            return 0;
        }

        double fillRatio = (double) tile.getFluxEnergyStored() / (double) capacity;
        return Mth.clamp((int) (fillRatio * 15.0D), 0, 15);
    }
}