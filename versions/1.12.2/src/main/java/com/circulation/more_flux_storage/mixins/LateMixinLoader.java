package com.circulation.more_flux_storage.mixins;

import it.unimi.dsi.fastutil.objects.ObjectLists;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.List;

@SuppressWarnings("unused")
public class LateMixinLoader implements ILateMixinLoader {

    @Override
    public List<String> getMixinConfigs() {
        return ObjectLists.singleton("mixins.more_flux_storage.json");
    }
}
