package com.circulation.more_flux_storage.block;

import com.circulation.more_flux_storage.api.IFluxGuiConnector;
import com.circulation.more_flux_storage.blockentity.TileInductionPortFlux;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sonar.fluxnetworks.api.FluxConstants;

public abstract class AbstractFluxGuiBlock extends BaseEntityBlock {

    protected AbstractFluxGuiBlock(Properties properties) {
        super(properties);
    }

    public static @Nullable IFluxGuiConnector resolveFluxConnector(Level level, BlockPos pos) {
        if (level.getBlockState(pos).getBlock() instanceof AbstractFluxGuiBlock block) {
            return block.getFluxConnector(level.getBlockEntity(pos));
        }
        return null;
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
        IFluxGuiConnector connector = getFluxConnector(e);
        if (connector == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (connector.getNetworkID() < 0) {
            connector.setPlayerUUID(player.getUUID());
        }
        if (!connector.canOpenFluxGui(player)) {
            return InteractionResult.FAIL;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, connector, buf -> {
                buf.writeBoolean(true);
                buf.writeBlockPos(pos);
                CompoundTag tag = new CompoundTag();
                connector.writeCustomTag(tag, FluxConstants.NBT_TILE_UPDATE);
                buf.writeNbt(tag);
            });
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state,
                            @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        IFluxGuiConnector connector = getFluxConnector(level.getBlockEntity(pos));
        if (connector != null) {
            CompoundTag tag = stack.getTagElement(FluxConstants.TAG_FLUX_DATA);
            if (tag != null) {
                connector.readCustomTag(tag.copy(), FluxConstants.NBT_TILE_DROP);
            }
            if (placer instanceof Player player) {
                connector.setPlayerUUID(player.getUUID());
            }
        }
    }

    protected @Nullable IFluxGuiConnector getFluxConnector(@Nullable BlockEntity blockEntity) {
        return blockEntity instanceof IFluxGuiConnector connector ? connector : null;
    }
}