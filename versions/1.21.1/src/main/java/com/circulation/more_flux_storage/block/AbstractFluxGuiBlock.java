package com.circulation.more_flux_storage.block;

import com.circulation.more_flux_storage.api.IFluxGuiConnector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
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
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                                        @NotNull Player player, @NotNull BlockHitResult hitResult) {
        IFluxGuiConnector connector = getFluxConnector(level.getBlockEntity(pos));
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
            serverPlayer.openMenu(connector, buf -> {
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

        if (level.getBlockEntity(pos) instanceof IFluxGuiConnector connector) {
            CompoundTag tag = readFluxData(stack);
            if (tag != null) {
                connector.readCustomTag(tag.copy(), FluxConstants.NBT_TILE_DROP);
            }
            if (placer instanceof Player player) {
                connector.setPlayerUUID(player.getUUID());
            }
        }
    }

    @Override
    public @NotNull List<ItemStack> getDrops(@NotNull BlockState state, @NotNull LootParams.Builder builder) {
        List<ItemStack> drops = new ArrayList<>(super.getDrops(state, builder));
        if (drops.isEmpty()) {
            return drops;
        }

        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof IFluxGuiConnector connector) {
            for (ItemStack stack : drops) {
                writeFluxData(stack, connector);
            }
        }
        return drops;
    }

    protected void writeFluxData(ItemStack stack, IFluxGuiConnector connector) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            CompoundTag fluxTag = new CompoundTag();
            connector.writeCustomTag(fluxTag, FluxConstants.NBT_TILE_DROP);

            if (fluxTag.isEmpty()) {
                tag.remove(FLUX_DATA_TAG);
            } else {
                tag.put(FLUX_DATA_TAG, fluxTag);
            }
        });
    }

    @Nullable
    private static CompoundTag readFluxData(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.contains(FLUX_DATA_TAG, net.minecraft.nbt.Tag.TAG_COMPOUND) ? tag.getCompound(FLUX_DATA_TAG) : null;
    }

    protected @Nullable IFluxGuiConnector getFluxConnector(@Nullable BlockEntity blockEntity) {
        return blockEntity instanceof IFluxGuiConnector connector ? connector : null;
    }

    @Nullable
    public static IFluxGuiConnector resolveFluxConnector(Level level, BlockPos pos) {
        if (level.getBlockState(pos).getBlock() instanceof AbstractFluxGuiBlock block) {
            return block.getFluxConnector(level.getBlockEntity(pos));
        }
        return null;
    }
}