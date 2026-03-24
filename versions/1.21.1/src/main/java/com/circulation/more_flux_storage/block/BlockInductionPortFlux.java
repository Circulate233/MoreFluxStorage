package com.circulation.more_flux_storage.block;

import com.circulation.more_flux_storage.api.IFluxProxyHost;
import com.circulation.more_flux_storage.blockentity.TileInductionPortFlux;
import com.circulation.more_flux_storage.registry.MoreFluxStorageContent;
import com.mojang.serialization.MapCodec;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlockInductionPortFlux extends AbstractFluxGuiBlock {

    public static final MapCodec<BlockInductionPortFlux> CODEC = simpleCodec(BlockInductionPortFlux::new);

    public BlockInductionPortFlux() {
        this(BlockBehaviour.Properties.of().strength(5.0F, 12.0F).noOcclusion().sound(net.minecraft.world.level.block.SoundType.METAL));
    }

    private BlockInductionPortFlux(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull MapCodec<? extends AbstractFluxGuiBlock> codec() {
        return CODEC;
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
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> components, @NotNull TooltipFlag flag) {
        components.add(Component.translatable("block.more_flux_storage.induction_port_flux.tooltip")
                                .withStyle(style -> style.withColor(0x808080).withItalic(true)));
    }
}
