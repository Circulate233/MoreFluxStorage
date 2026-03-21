package com.circulation.more_flux_storage.mixins;

import com.brandon3055.draconicevolution.blocks.tileentity.TileEnergyPylon;
import com.circulation.more_flux_storage.registry.MoreFluxStorageContent;
import com.circulation.more_flux_storage.util.Utils;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = TileEnergyPylon.class, remap = false)
public abstract class TileEnergyPylonMixin {

    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/neoforged/neoforge/registries/DeferredHolder;get()Ljava/lang/Object;"
        )
    )
    private static Object redirectBlockEntityType(DeferredHolder<?,?> instance) {
        if (Utils.trigger()) {
            return MoreFluxStorageContent.ENERGY_PYLON_FLUX_BLOCK_ENTITY.get();
        }
        return instance.get();
    }
}
