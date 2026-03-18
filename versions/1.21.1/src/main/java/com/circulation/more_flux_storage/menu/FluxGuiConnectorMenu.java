package com.circulation.more_flux_storage.menu;

import com.circulation.more_flux_storage.api.IFluxGuiConnector;
import com.circulation.more_flux_storage.registry.MoreFluxStorageContent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FluxGuiConnectorMenu extends AbstractContainerMenu {

    @Nullable
    private final IFluxGuiConnector provider;

    public FluxGuiConnectorMenu(int containerId, Inventory inventory, @Nullable IFluxGuiConnector provider) {
        super(MoreFluxStorageContent.FLUX_GUI_CONNECTOR_MENU.get(), containerId);
        this.provider = provider;
        if (provider != null) {
            provider.onPlayerOpened(inventory.player);
        }
    }

    @Nullable
    public IFluxGuiConnector getProvider() {
        return provider;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (provider == null) return false;
        var pos = provider.getFluxGuiPos();
        var level = provider.getFluxGuiLevel();
        if (level == null || pos == null) return false;
        return player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        if (provider != null) {
            provider.onPlayerClosed(player);
        }
    }
}
