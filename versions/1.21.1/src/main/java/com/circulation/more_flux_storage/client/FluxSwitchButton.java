package com.circulation.more_flux_storage.client;

import net.minecraft.client.gui.GuiGraphics;
import sonar.fluxnetworks.client.gui.button.SwitchButton;

public class FluxSwitchButton extends SwitchButton {

    public FluxSwitchButton(int x, int y, boolean checked, int color) {
        super(null, x, y, checked, color);
    }

    public void render(GuiGraphics gr, int mouseX, int mouseY, float deltaTicks) {
        drawButton(gr, mouseX, mouseY, deltaTicks);
    }
}
