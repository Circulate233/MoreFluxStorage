package com.circulation.more_flux_storage.mixins;

import com.circulation.more_flux_storage.api.IFluxGuiConnector;
import com.circulation.more_flux_storage.util.FluxGuiConnectorData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sonar.fluxnetworks.common.connection.FluxMenu;
import sonar.fluxnetworks.register.RegistryMenuTypes;

@Mixin(value = RegistryMenuTypes.class, remap = false)
public class MixinRegistryMenuTypes {

    @Unique
    private static BlockEntity more$blockEntity;

    @Redirect(method = "lambda$register$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;", remap = true))
    private static BlockEntity register(Level instance, BlockPos pos) {
        return more$blockEntity = instance.getBlockEntity(pos);
    }

    @Inject(method = "lambda$register$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;", shift = At.Shift.AFTER, remap = true), cancellable = true)
    private static void blockEntity(int containerId, Inventory inventory, FriendlyByteBuf buffer, CallbackInfoReturnable<FluxMenu> cir) {
        if (more$blockEntity instanceof IFluxGuiConnector device) {
            CompoundTag tag = buffer.readNbt();
            if (tag != null) {
                device.readCustomTag(tag, (byte) 11);
            }

            cir.setReturnValue(new FluxMenu(containerId, inventory, device));
        } else if (more$blockEntity instanceof FluxGuiConnectorData.Host h) {
            var device = h.getFluxConnector();
            CompoundTag tag = buffer.readNbt();
            if (tag != null) {
                device.readCustomTag(tag, (byte) 11);
            }

            cir.setReturnValue(new FluxMenu(containerId, inventory, device));
        }
        more$blockEntity = null;
    }
}
