package com.circulation.more_flux_storage.menu;

import com.circulation.more_flux_storage.api.IFluxGuiConnector;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import sonar.fluxnetworks.common.connection.FluxMenu;
import org.jetbrains.annotations.NotNull;

public class FluxGuiConnectorMenu extends FluxMenu {

    public FluxGuiConnectorMenu(int containerId, Inventory inventory, IFluxGuiConnector provider) {
        super(containerId, inventory, provider);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (mProvider instanceof IFluxGuiConnector connector) {
            var level = connector.getFluxGuiLevel();
            return connector.isChunkLoaded() && level == player.level();
        }
        return super.stillValid(player);
    }
}
