package com.circulation.more_flux_storage.block;

import com.circulation.more_flux_storage.api.IFluxProxyHost;
import com.circulation.more_flux_storage.blockentity.TileFluxAccessorFlux;
import com.circulation.more_flux_storage.registry.MoreFluxStorageContent;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlockFluxAccessorFlux extends AbstractFluxGuiBlock {

    public static final MapCodec<BlockFluxAccessorFlux> CODEC = simpleCodec(BlockFluxAccessorFlux::new);

    public BlockFluxAccessorFlux() {
        this(BlockBehaviour.Properties.of().strength(5.0F, 12.0F));
    }

    private BlockFluxAccessorFlux(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull MapCodec<? extends AbstractFluxGuiBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new TileFluxAccessorFlux(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state,
                                                                            @NotNull BlockEntityType<T> blockEntityType) {
        BlockEntityType<TileFluxAccessorFlux> fluxAccessorType = MoreFluxStorageContent.getFluxAccessorFluxBlockEntityType();
        if (fluxAccessorType == null) {
            return null;
        }
        return createTickerHelper(blockEntityType, fluxAccessorType, TileFluxAccessorFlux::tick);
    }

    @Override
    protected @Nullable IFluxProxyHost getFluxHost(@Nullable BlockEntity blockEntity) {
        return blockEntity instanceof TileFluxAccessorFlux tile ? tile : null;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> components, @NotNull TooltipFlag flag) {
        components.add(Component.translatable("block.more_flux_storage.flux_accessor_flux.tooltip")
            .withStyle(style -> style.withColor(0x808080).withItalic(true)));
    }
}