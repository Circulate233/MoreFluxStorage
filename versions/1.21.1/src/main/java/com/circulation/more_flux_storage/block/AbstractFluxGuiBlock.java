package com.circulation.more_flux_storage.block;

import com.circulation.more_flux_storage.api.IFluxProxyHost;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sonar.fluxnetworks.api.FluxConstants;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractFluxGuiBlock extends BaseEntityBlock {

    private static final String FLUX_DATA_TAG = "FluxData";

    protected AbstractFluxGuiBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    public static @Nullable IFluxProxyHost resolveFluxHost(Level level, BlockPos pos) {
        if (level.getBlockState(pos).getBlock() instanceof AbstractFluxGuiBlock block) {
            return block.getFluxHost(level.getBlockEntity(pos));
        }
        return null;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                                        @NotNull Player player, @NotNull BlockHitResult hitResult) {
        IFluxProxyHost host = getFluxHost(level.getBlockEntity(pos));
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
            serverPlayer.openMenu(host.getFluxProxyDevice(), buf -> {
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
            CompoundTag tag = readFluxData(stack);
            if (tag != null) {
                host.readFluxTag(tag.copy(), FluxConstants.NBT_TILE_DROP);
            }
            if (placer instanceof Player player) {
                host.setFluxOwner(player.getUUID());
            }
        }
    }

    @Override
    public @NotNull List<ItemStack> getDrops(@NotNull BlockState state, @NotNull LootParams.Builder builder) {
        List<ItemStack> drops = new ArrayList<>(super.getDrops(state, builder));
        if (drops.isEmpty()) {
            return drops;
        }

        IFluxProxyHost host = getFluxHost(builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY));
        if (host != null) {
            for (ItemStack stack : drops) {
                writeFluxData(stack, host);
            }
        }
        return drops;
    }

    protected void writeFluxData(ItemStack stack, IFluxProxyHost host) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            CompoundTag fluxTag = new CompoundTag();
            host.writeFluxTag(fluxTag, FluxConstants.NBT_TILE_DROP);
            if (fluxTag.isEmpty()) {
                tag.remove(FLUX_DATA_TAG);
            } else {
                tag.put(FLUX_DATA_TAG, fluxTag);
            }
        });
    }

    protected @Nullable IFluxProxyHost getFluxHost(@Nullable BlockEntity blockEntity) {
        return blockEntity instanceof IFluxProxyHost host ? host : null;
    }

    @Nullable
    private static CompoundTag readFluxData(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.contains(FLUX_DATA_TAG, Tag.TAG_COMPOUND) ? tag.getCompound(FLUX_DATA_TAG) : null;
    }
}
