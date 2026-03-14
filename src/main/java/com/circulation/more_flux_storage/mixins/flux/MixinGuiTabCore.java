package com.circulation.more_flux_storage.mixins.flux;

import com.circulation.more_flux_storage.api.IFluxGuiConnector;
import com.circulation.more_flux_storage.client.gui.GuiFluxCompatibleHome;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sonar.fluxnetworks.api.gui.EnumNavigationTabs;
import sonar.fluxnetworks.api.network.INetworkConnector;
import sonar.fluxnetworks.client.gui.basic.GuiTabCore;

@Mixin(value = GuiTabCore.class, remap = false)
public class MixinGuiTabCore {

    @Inject(method = "switchTab", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;closeScreen()V", shift = At.Shift.BEFORE), cancellable = true)
    private static void openGui(EnumNavigationTabs tab, EntityPlayer player, INetworkConnector connector, CallbackInfo ci) {
        if (connector instanceof IFluxGuiConnector s) {
            FMLCommonHandler.instance().showGuiScreen(new GuiFluxCompatibleHome(player, s));
            ci.cancel();
        }
    }

}
