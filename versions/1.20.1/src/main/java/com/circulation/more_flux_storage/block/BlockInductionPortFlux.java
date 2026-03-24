package com.circulation.more_flux_storage.block;

import com.circulation.more_flux_storage.api.IFluxProxyHost;
import com.circulation.more_flux_storage.blockentity.TileInductionPortFlux;
import com.circulation.more_flux_storage.registry.MoreFluxStorageContent;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlockInductionPortFlux extends AbstractFluxGuiBlock {

    public BlockInductionPortFlux() {
        super(BlockBehaviour.Properties.of().strength(5.0F, 12.0F).noOcclusion().sound(net.minecraft.world.level.block.SoundType.METAL));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new TileInductionPortFlux(pos, state);
    }

    @Override
    protected @Nullable IFluxProxyHost getFluxHost(@Nullable BlockEntity blockEntity) {
        return blockEntity instanceof TileInductionPortFlux tile ? tile : null;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state,
                                                                            @NotNull BlockEntityType<T> blockEntityType) {
        BlockEntityType<TileInductionPortFlux> inductionPortType = MoreFluxStorageContent.getInductionPortFluxBlockEntityType();
        if (inductionPortType == null) {
            return null;
        }
        return createTickerHelper(
            blockEntityType,
            inductionPortType,
            level.isClientSide ? TileEntityMekanism::tickClient : TileEntityMekanism::tickServer
        );
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable BlockGetter getter, @NotNull List<Component> components, @NotNull TooltipFlag flag) {
        components.add(Component.translatable("block.more_flux_storage.induction_port_flux.tooltip")
                                .withStyle(style -> style.withColor(0x808080).withItalic(true)));
    }

}