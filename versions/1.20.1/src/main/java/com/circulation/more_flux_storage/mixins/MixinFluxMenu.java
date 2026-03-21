package com.circulation.more_flux_storage.mixins;

import com.circulation.more_flux_storage.api.IFluxGuiConnector;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sonar.fluxnetworks.api.device.IFluxProvider;
import sonar.fluxnetworks.common.connection.FluxMenu;

@Mixin(value = FluxMenu.class, remap = false)
public class MixinFluxMenu {

    @Shadow
    @Final
    public IFluxProvider mProvider;

    @Inject(method = "stillValid", at = @At("HEAD"), cancellable = true)
    private void add(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (this.mProvider instanceof IFluxGuiConnector device) {
            cir.setReturnValue(device.isChunkLoaded() && device.getLevel() == player.level());
        }
    }
}
