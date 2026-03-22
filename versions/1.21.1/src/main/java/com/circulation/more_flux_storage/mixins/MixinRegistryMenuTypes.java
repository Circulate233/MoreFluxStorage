package com.circulation.more_flux_storage.mixins;

import com.circulation.more_flux_storage.api.IFluxProxyHost;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import sonar.fluxnetworks.register.RegistryMenuTypes;

@Mixin(value = RegistryMenuTypes.class, remap = false)
public class MixinRegistryMenuTypes {

    @Redirect(method = "lambda$register$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"))
    private static BlockEntity register(Level instance, BlockPos pos) {
        BlockEntity blockEntity = instance.getBlockEntity(pos);
        if (blockEntity instanceof IFluxProxyHost host) {
            return host.getFluxProxyDevice();
        }
        return blockEntity;
    }
}