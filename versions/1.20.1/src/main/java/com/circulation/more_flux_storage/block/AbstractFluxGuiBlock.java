package com.circulation.more_flux_storage.block;

import com.circulation.more_flux_storage.api.IFluxProxyHost;
import com.circulation.more_flux_storage.blockentity.TileInductionPortFlux;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sonar.fluxnetworks.api.FluxConstants;

import java.util.List;

public abstract class AbstractFluxGuiBlock extends BaseEntityBlock {

    protected AbstractFluxGuiBlock(Properties properties) {
        super(properties);
    }

    public static @Nullable IFluxProxyHost resolveFluxHost(Level level, BlockPos pos) {
        if (level.getBlockState(pos).getBlock() instanceof AbstractFluxGuiBlock block) {
            return block.getFluxHost(level.getBlockEntity(pos));
        }
        return null;
    }

    @NotNull
    @Override
    public RenderShape getRenderShape(@NotNull BlockState p_49232_) {
        return RenderShape.MODEL;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack p_49816_, @Nullable BlockGetter p_49817_, @NotNull List<Component> p_49818_, @NotNull TooltipFlag p_49819_) {

    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                          @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof TileInductionPortFlux tile) {
                return tile.onSneakRightClick(player);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        var e = level.getBlockEntity(pos);
        IFluxProxyHost host = getFluxHost(e);
        if (host == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (host.getFluxNetworkId() < 0) {
            host.setFluxOwner(player.getUUID());
        }
        if (!host.canOpenFluxGui(player)) {
            return InteractionResult.FAIL;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            MenuProvider provider = host.getFluxProxyDevice();
            NetworkHooks.openScreen(serverPlayer, provider, buf -> {
                buf.writeBoolean(true);
                buf.writeBlockPos(pos);
                CompoundTag tag = new CompoundTag();
                host.writeFluxTag(tag, FluxConstants.NBT_TILE_UPDATE);
                buf.writeNbt(tag);
            });
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state,
                            @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        IFluxProxyHost host = getFluxHost(level.getBlockEntity(pos));
        if (host != null) {
            CompoundTag tag = stack.getTagElement(FluxConstants.TAG_FLUX_DATA);
            if (tag != null) {
                host.readFluxTag(tag.copy(), FluxConstants.NBT_TILE_DROP);
            }
            if (placer instanceof Player player) {
                host.setFluxOwner(player.getUUID());
            }
        }
    }

    protected @Nullable IFluxProxyHost getFluxHost(@Nullable BlockEntity blockEntity) {
        return blockEntity instanceof IFluxProxyHost host ? host : null;
    }
}