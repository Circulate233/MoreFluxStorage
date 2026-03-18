package com.circulation.more_flux_storage.block;

import com.brandon3055.draconicevolution.blocks.StructureBlock;
import com.brandon3055.draconicevolution.blocks.machines.EnergyPylon;
import com.brandon3055.draconicevolution.blocks.machines.EnergyPylon.Mode;
import com.circulation.more_flux_storage.api.IFluxGuiConnector;
import com.circulation.more_flux_storage.blockentity.TileEnergyPylonFlux;
import com.circulation.more_flux_storage.registry.MoreFluxStorageContent;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockEnergyPylonFlux extends AbstractFluxGuiBlock {

    public static final MapCodec<BlockEnergyPylonFlux> CODEC = simpleCodec(BlockEnergyPylonFlux::new);

    public BlockEnergyPylonFlux() {
        this(BlockBehaviour.Properties.of().strength(5.0F, 12.0F).noOcclusion());
    }

    private BlockEnergyPylonFlux(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(EnergyPylon.MODE, Mode.OUTPUT).setValue(EnergyPylon.FACING, Direction.UP));
    }

    @Override
    protected @NotNull MapCodec<? extends AbstractFluxGuiBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(EnergyPylon.MODE, EnergyPylon.FACING);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new TileEnergyPylonFlux(pos, state);
    }

    @Override
    protected @Nullable IFluxGuiConnector getFluxConnector(@Nullable BlockEntity blockEntity) {
        return blockEntity instanceof TileEnergyPylonFlux tile ? tile.getFluxConnector() : null;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state,
                                                                             @NotNull BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, MoreFluxStorageContent.ENERGY_PYLON_FLUX_BLOCK_ENTITY.get(), TileEnergyPylonFlux::tick);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                                        @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof TileEnergyPylonFlux tile)) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                tile.selectNextCore();
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!level.isClientSide) {
            tile.validateStructure();
            if (tile.hasCoreBinding()) {
                tile.drawParticleBeam();
            }
            if (!tile.isStructureValidForFlux()) {
                return InteractionResult.FAIL;
            }
        }

        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                @NotNull Block block, @NotNull BlockPos fromPos, boolean isMoving) {
        if (!StructureBlock.buildingLock && level.getBlockEntity(pos) instanceof TileEnergyPylonFlux tile) {
            tile.validateStructure();
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof TileEnergyPylonFlux tile)) {
            return 0;
        }

        long capacity = tile.getStoredOpCapacity();
        if (capacity <= 0L) {
            return 0;
        }

        double fillRatio = (double) tile.getStoredOpAmount() / (double) capacity;
        return Mth.clamp((int) (fillRatio * 15.0D), 0, 15);
    }
}