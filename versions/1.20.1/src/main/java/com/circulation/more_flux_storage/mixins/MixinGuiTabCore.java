package com.circulation.more_flux_storage.mixins;

import com.circulation.more_flux_storage.api.IFluxGuiConnector;
import com.circulation.more_flux_storage.client.GuiFluxDeviceHomeM;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import sonar.fluxnetworks.client.gui.basic.GuiFluxCore;
import sonar.fluxnetworks.client.gui.basic.GuiTabCore;
import sonar.fluxnetworks.common.connection.FluxMenu;

@Mixin(value = GuiTabCore.class, remap = false)
public class MixinGuiTabCore extends GuiFluxCore {

    public MixinGuiTabCore(@NotNull FluxMenu menu, @NotNull Player player) {
        super(menu, player);
    }

    @Redirect(method = "switchTab", at = @At(value = "INVOKE", target = "Lsonar/fluxnetworks/client/gui/basic/GuiTabCore;onClose()V"))
    private void add(GuiTabCore instance) {
        if (this.menu.mProvider instanceof IFluxGuiConnector) {
            this.getMinecraft().setScreen(new GuiFluxDeviceHomeM(this.menu, this.mPlayer));
        } else {
            onClose();
        }
    }
}
