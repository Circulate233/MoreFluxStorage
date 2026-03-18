package com.circulation.more_flux_storage.mixins.flux;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import sonar.fluxnetworks.api.network.ITransferHandler;
import sonar.fluxnetworks.common.connection.FluxNetworkBase;
import sonar.fluxnetworks.common.connection.FluxNetworkServer;

@Mixin(value = FluxNetworkServer.class, remap = false)
public abstract class MixinFluxNetworkServer extends FluxNetworkBase {

    @Shadow
    public long bufferLimiter;

    @WrapOperation(method = "onEndServerTick", at = @At(value = "INVOKE", target = "Lsonar/fluxnetworks/api/network/ITransferHandler;getRequest()J", ordinal = 1))
    public long getRequest(ITransferHandler instance, Operation<Long> original) {
        long i = original.call(instance);
        if (this.bufferLimiter == Long.MAX_VALUE) {
            return 0;
        } else return Math.min(Long.MAX_VALUE - this.bufferLimiter, i);
    }
}
