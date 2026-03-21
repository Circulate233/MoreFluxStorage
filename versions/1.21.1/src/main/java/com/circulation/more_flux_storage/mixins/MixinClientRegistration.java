package com.circulation.more_flux_storage.mixins;

import com.circulation.more_flux_storage.api.IFluxGuiConnector;
import com.circulation.more_flux_storage.client.GuiFluxDeviceHomeM;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sonar.fluxnetworks.common.connection.FluxMenu;
import sonar.fluxnetworks.register.ClientRegistration;

@Mixin(value = ClientRegistration.class, remap = false)
public class MixinClientRegistration {

    @Inject(method = "lambda$getScreenFactory$2", at = @At("HEAD"), cancellable = true)
    private static void add(FluxMenu menu, Inventory inventory, Component title, CallbackInfoReturnable<AbstractContainerScreen<?>> cir) {
        if (menu.mProvider instanceof IFluxGuiConnector) {
            cir.setReturnValue(new GuiFluxDeviceHomeM(menu, inventory.player));
        }
    }
}
