package com.circulation.more_flux_storage.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import sonar.fluxnetworks.common.device.TileFluxDevice;

import java.util.UUID;

public interface IFluxProxyHost {

    TileFluxDevice getFluxProxyDevice();

    int getFluxNetworkId();

    void setFluxOwner(UUID uuid);

    boolean canOpenFluxGui(Player player);

    void writeFluxTag(CompoundTag tag, byte type);

    void readFluxTag(CompoundTag tag, byte type);
}