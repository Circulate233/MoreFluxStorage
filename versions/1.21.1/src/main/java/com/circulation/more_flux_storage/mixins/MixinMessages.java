package com.circulation.more_flux_storage.mixins;

import com.circulation.more_flux_storage.api.IFluxProxyHost;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import sonar.fluxnetworks.register.Messages;

@Mixin(value = Messages.class, remap = false)
public class MixinMessages {

    @Unique
    private static BlockEntity more$getFluxDevice(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof IFluxProxyHost host) {
            return host.getFluxProxyDevice();
        }
        return blockEntity;
    }

    @Redirect(method = "lambda$onEditTile$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"))
    private static BlockEntity onEditTile(Level instance, BlockPos pos) {
        return more$getFluxDevice(instance, pos);
    }

    @Redirect(method = "lambda$onTileNetwork$5", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"))
    private static BlockEntity onTileNetwork(Level instance, BlockPos pos) {
        return more$getFluxDevice(instance, pos);
    }
}