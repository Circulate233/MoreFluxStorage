package com.circulation.more_flux_storage.block;

import com.circulation.more_flux_storage.api.IFluxProxyHost;
import com.circulation.more_flux_storage.blockentity.TileEnergyPylonFlux;
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

public class BlockEnergyPylonFlux extends AbstractFluxGuiBlock {

    public static final MapCodec<BlockEnergyPylonFlux> CODEC = simpleCodec(BlockEnergyPylonFlux::new);

    public BlockEnergyPylonFlux() {
        this(BlockBehaviour.Properties.of().strength(5.0F, 12.0F).noOcclusion());
    }

    private BlockEnergyPylonFlux(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull MapCodec<? extends AbstractFluxGuiBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new TileEnergyPylonFlux(pos, state);
    }

    @Override
    protected @Nullable IFluxProxyHost getFluxHost(@Nullable BlockEntity blockEntity) {
        return blockEntity instanceof TileEnergyPylonFlux tile ? tile : null;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state,
                                                                            @NotNull BlockEntityType<T> blockEntityType) {
        BlockEntityType<TileEnergyPylonFlux> energyPylonType = MoreFluxStorageContent.getEnergyPylonFluxBlockEntityType();
        if (energyPylonType == null) {
            return null;
        }
        return createTickerHelper(blockEntityType, energyPylonType, TileEnergyPylonFlux::tick);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> components, @NotNull TooltipFlag flag) {
        components.add(Component.translatable("block.more_flux_storage.energy_pylon_flux.tooltip")
            .withStyle(style -> style.withColor(0x808080).withItalic(true)));
    }
}
