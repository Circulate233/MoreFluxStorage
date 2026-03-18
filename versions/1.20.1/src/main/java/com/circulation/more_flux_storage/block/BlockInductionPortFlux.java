package com.circulation.more_flux_storage.block;

import com.circulation.more_flux_storage.api.IFluxGuiConnector;
import com.circulation.more_flux_storage.blockentity.TileInductionPortFlux;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockInductionPortFlux extends AbstractFluxGuiBlock {

    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public BlockInductionPortFlux() {
        super(BlockBehaviour.Properties.of().strength(5.0F, 12.0F));
        registerDefaultState(defaultBlockState().setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
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


}