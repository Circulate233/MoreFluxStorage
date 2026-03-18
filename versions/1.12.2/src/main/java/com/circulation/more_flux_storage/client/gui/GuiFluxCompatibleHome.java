package com.circulation.more_flux_storage.client.gui;

import com.circulation.more_flux_storage.MoreFluxStorage;
import com.circulation.more_flux_storage.api.IFluxGuiConnector;
import com.circulation.more_flux_storage.common.network.PacketFluxGuiAction;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;
import sonar.fluxnetworks.FluxNetworks;
import sonar.fluxnetworks.api.gui.EnumNavigationTabs;
import sonar.fluxnetworks.api.network.NetworkSettings;
import sonar.fluxnetworks.api.translate.FluxTranslate;
import sonar.fluxnetworks.api.utils.NBTType;
import sonar.fluxnetworks.client.gui.basic.GuiButtonCore;
import sonar.fluxnetworks.client.gui.basic.GuiTabCore;
import sonar.fluxnetworks.client.gui.basic.ITextBoxButton;
import sonar.fluxnetworks.client.gui.button.SlidedSwitchButton;
import sonar.fluxnetworks.client.gui.button.TextboxButton;
import sonar.fluxnetworks.common.handler.PacketHandler;
import sonar.fluxnetworks.common.network.PacketNetworkUpdateRequest;

public class GuiFluxCompatibleHome extends GuiTabCore implements ITextBoxButton {

    private final IFluxGuiConnector connectorTile;
    public TextboxButton fluxName;
    public TextboxButton priority;
    public TextboxButton limit;
    public SlidedSwitchButton surge;
    public SlidedSwitchButton disableLimit;
    public SlidedSwitchButton chunkLoad;
    private int timer;

    public GuiFluxCompatibleHome(EntityPlayer player, IFluxGuiConnector connectorTile) {
        super(player, connectorTile);
        this.connectorTile = connectorTile;
        this.setDefaultTabs();
    }

    @Override
    public EnumNavigationTabs getNavigationTab() {
        return EnumNavigationTabs.TAB_HOME;
    }

    @Override
    protected void drawForegroundLayer(int mouseX, int mouseY) {
        super.drawForegroundLayer(mouseX, mouseY);
        this.renderNetwork(this.network.getSetting(NetworkSettings.NETWORK_NAME),
            this.network.getSetting(NetworkSettings.NETWORK_COLOR), 20, 8);
        this.renderTransfer(this.connectorTile, 16777215, 30, 90);
        this.drawCenteredString(this.fontRenderer,
            TextFormatting.RED + FluxNetworks.proxy.getFeedback(false).getInfo(), 89, 150, 16777215);
        this.fontRenderer.drawString(FluxTranslate.SURGE_MODE.t(), 20, 120,
            this.network.getSetting(NetworkSettings.NETWORK_COLOR));
        this.fontRenderer.drawString(FluxTranslate.DISABLE_LIMIT.t(), 20, 132,
            this.network.getSetting(NetworkSettings.NETWORK_COLOR));
        if (this.connectorTile.shouldShowFluxGuiChunkLoading()) {
            this.fontRenderer.drawString(FluxTranslate.CHUNK_LOADING.t(), 20, 144,
                this.network.getSetting(NetworkSettings.NETWORK_COLOR));
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        this.configureNavigationButtons(EnumNavigationTabs.TAB_HOME, this.navigationTabs);
        int color = this.network.getSetting(NetworkSettings.NETWORK_COLOR) | -16777216;
        this.fluxName = TextboxButton.create(this, FluxTranslate.NAME.t() + ": ", 0,
            this.fontRenderer, 16, 28, 144, 12).setOutlineColor(color);
        this.fluxName.setMaxStringLength(24);
        this.fluxName.setText(this.connectorTile.getCustomName());

        this.priority = TextboxButton.create(this, FluxTranslate.PRIORITY.t() + ": ", 1,
            this.fontRenderer, 16, 45, 144, 12).setOutlineColor(color).setDigitsOnly();
        this.priority.setMaxStringLength(5);
        this.priority.setText(String.valueOf(this.connectorTile.getRawPriority()));

        this.limit = TextboxButton.create(this, FluxTranslate.TRANSFER_LIMIT.t() + ": ", 2,
            this.fontRenderer, 16, 62, 144, 12).setOutlineColor(color).setDigitsOnly();
        this.limit.setMaxStringLength(19);
        this.limit.setText(String.valueOf(this.connectorTile.getRawLimit()));

        this.surge = new SlidedSwitchButton(140, 120, 1, this.guiLeft, this.guiTop,
            this.connectorTile.getSurgeMode());
        this.disableLimit = new SlidedSwitchButton(140, 132, 2, this.guiLeft, this.guiTop,
            this.connectorTile.getDisableLimit());
        this.switches.add(this.surge);
        this.switches.add(this.disableLimit);

        if (this.connectorTile.shouldShowFluxGuiChunkLoading()) {
            this.chunkLoad = new SlidedSwitchButton(140, 144, 3, this.guiLeft, this.guiTop,
                this.connectorTile.isForcedLoading());
            this.switches.add(this.chunkLoad);
        }

        this.textBoxes.add(this.fluxName);
        this.textBoxes.add(this.priority);
        this.textBoxes.add(this.limit);
    }

    @Override
    public void onTextBoxChanged(TextboxButton text) {
        if (text == this.fluxName) {
            this.connectorTile.setCustomName(this.fluxName.getText());
            MoreFluxStorage.NET_CHANNEL.sendToServer(
                PacketFluxGuiAction.setName(this.connectorTile.getFluxGuiPos(), this.fluxName.getText()));
        } else if (text == this.priority) {
            int value = this.priority.getIntegerFromText(false);
            this.connectorTile.setRawPriority(value);
            MoreFluxStorage.NET_CHANNEL.sendToServer(
                PacketFluxGuiAction.setPriority(this.connectorTile.getFluxGuiPos(), value));
        } else if (text == this.limit) {
            long value = this.connectorTile.sanitizeFluxGuiLimit(this.limit.getLongFromText(true));
            this.connectorTile.setRawLimit(value);
            this.limit.setText(String.valueOf(value));
            MoreFluxStorage.NET_CHANNEL.sendToServer(
                PacketFluxGuiAction.setLimit(this.connectorTile.getFluxGuiPos(), value));
        }
    }

    @Override
    public void onButtonClicked(GuiButtonCore button, int mouseX, int mouseY, int mouseButton) {
        super.onButtonClicked(button, mouseX, mouseY, mouseButton);
        if (mouseButton == 0 && button instanceof SlidedSwitchButton switchButton) {
            switchButton.switchButton();
            switch (switchButton.id) {
                case 1:
                    this.connectorTile.setSurgeMode(switchButton.slideControl);
                    MoreFluxStorage.NET_CHANNEL.sendToServer(
                        PacketFluxGuiAction.setSurge(this.connectorTile.getFluxGuiPos(), switchButton.slideControl));
                    break;
                case 2:
                    this.connectorTile.setDisableLimit(switchButton.slideControl);
                    MoreFluxStorage.NET_CHANNEL.sendToServer(PacketFluxGuiAction.setDisableLimit(
                        this.connectorTile.getFluxGuiPos(), switchButton.slideControl));
                    break;
                case 3:
                    this.connectorTile.setChunkLoading(switchButton.slideControl);
                    MoreFluxStorage.NET_CHANNEL.sendToServer(PacketFluxGuiAction.setChunkLoading(
                        this.connectorTile.getFluxGuiPos(), switchButton.slideControl));
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (this.timer == 0) {
            PacketHandler.network.sendToServer(
                new PacketNetworkUpdateRequest.UpdateRequestMessage(this.network.getNetworkID(), NBTType.NETWORK_GENERAL));
        }

        if (this.timer % 4 == 0 && this.chunkLoad != null) {
            this.chunkLoad.slideControl = this.connectorTile.isForcedLoading();
        }

        ++this.timer;
        this.timer %= 100;
    }
}
