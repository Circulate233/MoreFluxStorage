package com.circulation.more_flux_storage.mixins;

import com.circulation.more_flux_storage.registry.MoreFluxStorageContent;
import com.circulation.more_flux_storage.util.Utils;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityUpdateable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = TileEntityUpdateable.class, remap = false)
public abstract class TileEntityUpdateableMixin {

    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lmekanism/common/registration/impl/TileEntityTypeRegistryObject;get()Ljava/lang/Object;"
        )
    )
    private static Object redirectBlockEntityType(TileEntityTypeRegistryObject<?> instance) {
        if (Utils.trigger() && MoreFluxStorageContent.INDUCTION_PORT_FLUX_BLOCK_ENTITY != null) {
            return MoreFluxStorageContent.INDUCTION_PORT_FLUX_BLOCK_ENTITY.get();
        }
        return instance.get();
    }
}
