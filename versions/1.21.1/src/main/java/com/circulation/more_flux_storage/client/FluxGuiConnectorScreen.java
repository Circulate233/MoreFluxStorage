package com.circulation.more_flux_storage.client;

import com.circulation.more_flux_storage.api.IFluxGuiConnector;
import com.circulation.more_flux_storage.menu.FluxGuiConnectorMenu;
import com.circulation.more_flux_storage.network.FluxGuiActionPayload;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import sonar.fluxnetworks.api.FluxTranslate;
import sonar.fluxnetworks.api.energy.EnergyType;
import sonar.fluxnetworks.client.ClientCache;
import sonar.fluxnetworks.client.gui.basic.GuiFocusable;
import sonar.fluxnetworks.client.gui.button.FluxEditBox;
import sonar.fluxnetworks.common.connection.FluxNetwork;
import sonar.fluxnetworks.common.util.FluxUtils;

import java.util.ArrayList;
import java.util.List;

public class FluxGuiConnectorScreen extends AbstractContainerScreen<FluxGuiConnectorMenu> {

    @Nullable
    private final IFluxGuiConnector connector;
    private final List<FluxSwitchButton> switchButtons = new ArrayList<>();

    private FluxEditBox customName;
    private FluxEditBox priority;
    private FluxEditBox limit;
    private FluxSwitchButton surgeMode;
    private FluxSwitchButton disableLimit;
    @Nullable
    private FluxSwitchButton chunkLoading;

    public FluxGuiConnectorScreen(FluxGuiConnectorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, CommonComponents.EMPTY);
        this.connector = menu.getProvider();
        this.imageWidth = 176;
        this.imageHeight = 172;
    }

    @NotNull
    private FluxNetwork getNetwork() {
        int networkId = connector != null ? connector.getNetworkID() : -1;
        return ClientCache.getNetwork(networkId);
    }

    @Override
    protected void init() {
        super.init();
        switchButtons.clear();
        if (connector == null) return;

        int color = getNetwork().getNetworkColor() | 0xFF000000;

        customName = FluxEditBox.create(FluxTranslate.NAME.get() + ": ", font,
                leftPos + 16, topPos + 28, 144, 12);
        customName.setOutlineColor(color);
        customName.setMaxLength(24);
        customName.setValue(connector.getCustomName());
        customName.setResponder(string ->
            PacketDistributor.sendToServer(
                FluxGuiActionPayload.setName(connector.getFluxGuiPos(), customName.getValue()))
        );
        addRenderableWidget(customName);

        priority = FluxEditBox.create(FluxTranslate.PRIORITY.get() + ": ", font,
                leftPos + 16, topPos + 45, 144, 12);
        priority.setOutlineColor(color);
        priority.setDigitsOnly();
        priority.setAllowNegatives(true);
        priority.setMaxLength(5);
        priority.setValue(String.valueOf(connector.getRawPriority()));
        priority.setResponder(string ->
            PacketDistributor.sendToServer(
                FluxGuiActionPayload.setPriority(connector.getFluxGuiPos(), priority.getValidInt()))
        );
        addRenderableWidget(priority);

        limit = FluxEditBox.create(FluxTranslate.TRANSFER_LIMIT.get() + ": ", font,
                leftPos + 16, topPos + 62, 144, 12);
        limit.setOutlineColor(color);
        limit.setDigitsOnly();
        limit.setMaxValue(Long.MAX_VALUE);
        limit.setMaxLength(15);
        limit.setValue(String.valueOf(connector.getRawLimit()));
        limit.setResponder(string ->
            PacketDistributor.sendToServer(
                FluxGuiActionPayload.setLimit(connector.getFluxGuiPos(), limit.getValidLong()))
        );
        addRenderableWidget(limit);

        surgeMode = new FluxSwitchButton(leftPos + 140, topPos + 120,
                connector.getSurgeMode(), color);
        switchButtons.add(surgeMode);

        disableLimit = new FluxSwitchButton(leftPos + 140, topPos + 132,
                connector.getDisableLimit(), color);
        switchButtons.add(disableLimit);

        if (connector.shouldShowFluxGuiChunkLoading()) {
            chunkLoading = new FluxSwitchButton(leftPos + 140, topPos + 144,
                    connector.isForcedLoading(), color);
            switchButtons.add(chunkLoading);
        }
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics gr, float partialTick, int mouseX, int mouseY) {
        int color = getNetwork().getNetworkColor();
        float cx = width / 2f;
        float cy = height / 2f + 5;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, GuiFocusable.BACKGROUND);
        GuiFocusable.blitF(gr.pose().last().pose(), cx - 86, cy - 86, 0, 172, 172, 0, 0, 1, 1);

        RenderSystem.setShaderColor(FluxUtils.getRed(color), FluxUtils.getGreen(color), FluxUtils.getBlue(color), 1.0f);
        RenderSystem.setShaderTexture(0, GuiFocusable.FRAME);
        GuiFocusable.blitF(gr.pose().last().pose(), cx - 86, cy - 86, 0, 172, 172, 0, 0, 1, 1);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics gr, int mouseX, int mouseY) {
    }

    @Override
    public void render(@NotNull GuiGraphics gr, int mouseX, int mouseY, float partialTick) {
        super.render(gr, mouseX, mouseY, partialTick);

        if (connector == null) return;

        float deltaTicks = getMinecraft().getTimer().getRealtimeDeltaTicks();
        int color = getNetwork().getNetworkColor();

        renderNetworkBar(gr, color, topPos + 8);
        renderTransferInfo(gr, leftPos + 30, topPos + 90);

        gr.drawString(font, FluxTranslate.SURGE_MODE.get(), leftPos + 20, topPos + 120, color);
        gr.drawString(font, FluxTranslate.DISABLE_LIMIT.get(), leftPos + 20, topPos + 132, color);
        if (chunkLoading != null) {
            gr.drawString(font, FluxTranslate.CHUNK_LOADING.get(), leftPos + 20, topPos + 144, color);
        }

        for (FluxSwitchButton button : switchButtons) {
            button.render(gr, mouseX, mouseY, deltaTicks);
        }

        if (customName != null && customName.getValue().isEmpty()) {
            int y = customName.getY() + (customName.getHeight() - 8) / 2;
            gr.drawString(font, connector.getDisplayName(), customName.getX() + 4, y, 0x404040);
        }
    }

    private void renderNetworkBar(GuiGraphics gr, int color, int y) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(FluxUtils.getRed(color), FluxUtils.getGreen(color), FluxUtils.getBlue(color), 1.0f);
        RenderSystem.setShaderTexture(0, GuiFocusable.ICON);
        int x = leftPos + 20;
        GuiFocusable.blitF(gr, x, y, 135, 12, 0, 320, 270, 24);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        String networkName = getNetwork().getNetworkName();
        gr.drawString(font, networkName, x + 4, y + 2, 0xFFFFFF);
    }

    private void renderTransferInfo(GuiGraphics gr, int x, int y) {
        long change = connector.getTransferChange();
        String transferInfo = (change > 0 ? "+" : "") + change + " FE/t";
        gr.drawString(font, transferInfo, x, y, 0xFFFFFF);

        String bufferLabel = connector.getDeviceType().isStorage()
                ? FluxTranslate.ENERGY.get() : FluxTranslate.BUFFER.get();
        String bufferText = bufferLabel + ": " + ChatFormatting.BLUE + connector.getTransferBuffer() + " FE";
        gr.drawString(font, bufferText, x, y + 10, 0xFFFFFF);

        gr.pose().pushPose();
        gr.pose().translate(0, 0, 50);
        gr.renderItem(connector.getDisplayStack(), x - 20, y + 1);
        gr.renderItemDecorations(font, connector.getDisplayStack(), x - 20, y + 1);
        gr.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT && connector != null) {
            for (FluxSwitchButton button : switchButtons) {
                if (button.isClickable() && button.isMouseHovered(mouseX, mouseY)) {
                    onSwitchClicked(button);
                    return true;
                }
            }
        }

        boolean result = super.mouseClicked(mouseX, mouseY, mouseButton);

        boolean anyFocused = false;
        for (GuiEventListener child : children()) {
            if (child instanceof FluxEditBox editBox && editBox.isFocused() && editBox.isMouseOver(mouseX, mouseY)) {
                anyFocused = true;
                break;
            }
        }
        if (!anyFocused) {
            setFocused(null);
        }

        return result;
    }

    private void onSwitchClicked(FluxSwitchButton button) {
        if (connector == null) return;
        if (button == surgeMode) {
            surgeMode.toggle();
            PacketDistributor.sendToServer(
                FluxGuiActionPayload.setSurge(connector.getFluxGuiPos(), surgeMode.isChecked()));
        } else if (button == disableLimit) {
            disableLimit.toggle();
            PacketDistributor.sendToServer(
                FluxGuiActionPayload.setDisableLimit(connector.getFluxGuiPos(), disableLimit.isChecked()));
        } else if (button == chunkLoading) {
            chunkLoading.toggle();
            PacketDistributor.sendToServer(
                FluxGuiActionPayload.setChunkLoading(connector.getFluxGuiPos(), chunkLoading.isChecked()));
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        InputConstants.Key key = InputConstants.getKey(keyCode, scanCode);

        if (getFocused() != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                setFocused(null);
                return true;
            }
            if (getMinecraft().options.keyInventory.isActiveAndMatches(key)) {
                return true;
            }
        } else if (keyCode == GLFW.GLFW_KEY_ESCAPE || getMinecraft().options.keyInventory.isActiveAndMatches(key)) {
            onClose();
            return true;
        }

        boolean result = super.keyPressed(keyCode, scanCode, modifiers);
        return result || getFocused() != null;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener listener) {
        super.setFocused(listener);
        for (GuiEventListener child : children()) {
            if (child != listener && child instanceof FluxEditBox editBox && editBox.isFocused()) {
                editBox.setFocused(false);
            }
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (connector != null && customName != null) {
            int color = getNetwork().getNetworkColor() | 0xFF000000;
            customName.setOutlineColor(color);
            priority.setOutlineColor(color);
            limit.setOutlineColor(color);
            surgeMode.setColor(color);
            disableLimit.setColor(color);
            if (chunkLoading != null) {
                chunkLoading.setChecked(connector.isForcedLoading());
                chunkLoading.setColor(color);
            }
        }
    }
}
