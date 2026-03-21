package com.circulation.more_flux_storage.block;

import com.brandon3055.draconicevolution.blocks.StructureBlock;
import com.circulation.more_flux_storage.api.IFluxGuiConnector;
import com.circulation.more_flux_storage.blockentity.TileEnergyPylonFlux;
import com.circulation.more_flux_storage.registry.MoreFluxStorageContent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class BlockEnergyPylonFlux extends AbstractFluxGuiBlock {

    public BlockEnergyPylonFlux() {
        super(BlockBehaviour.Properties.of().strength(5.0F, 12.0F).noOcclusion());
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
        return BaseEntityBlock.createTickerHelper(blockEntityType, MoreFluxStorageContent.ENERGY_PYLON_FLUX_BLOCK_ENTITY.get(), TileEnergyPylonFlux::tick);
    }

    @Override
    public void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                @NotNull Block block, @NotNull BlockPos fromPos, boolean isMoving) {
        if (!StructureBlock.buildingLock && level.getBlockEntity(pos) instanceof TileEnergyPylonFlux tile) {
            tile.validateStructure();
        }
    }

}