package com.circulation.more_flux_storage.client;

import com.circulation.more_flux_storage.api.IFluxGuiConnector;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import sonar.fluxnetworks.FluxConfig;
import sonar.fluxnetworks.api.FluxTranslate;
import sonar.fluxnetworks.client.gui.EnumNavigationTab;
import sonar.fluxnetworks.client.gui.basic.GuiButtonCore;
import sonar.fluxnetworks.client.gui.basic.GuiTabCore;
import sonar.fluxnetworks.client.gui.button.FluxEditBox;
import sonar.fluxnetworks.client.gui.button.SwitchButton;
import sonar.fluxnetworks.common.connection.FluxMenu;

import javax.annotation.Nonnull;

public class GuiFluxDeviceHomeM extends GuiTabCore {
    public FluxEditBox mCustomName;
    public FluxEditBox mPriority;
    public FluxEditBox mLimit;
    public SwitchButton mSurgeMode;
    public SwitchButton mDisableLimit;
    public SwitchButton mChunkLoading;

    public GuiFluxDeviceHomeM(@Nonnull FluxMenu menu, @Nonnull Player player) {
        super(menu, player);
    }

    public EnumNavigationTab getNavigationTab() {
        return EnumNavigationTab.TAB_HOME;
    }

    public IFluxGuiConnector getDevice() {
        return (IFluxGuiConnector) ((FluxMenu) this.menu).mProvider;
    }

    public void init() {
        super.init();
        int color = this.getNetwork().getNetworkColor() | -16777216;
        this.mCustomName = FluxEditBox.create(FluxTranslate.NAME.get() + ": ", this.font, this.leftPos + 16, this.topPos + 28, 144, 12).setOutlineColor(color);
        this.mCustomName.setMaxLength(24);
        this.mCustomName.setValue(this.getDevice().getCustomName());
        this.mCustomName.setResponder((string) -> {
            CompoundTag tag = new CompoundTag();
            tag.putString("custom_name", this.mCustomName.getValue());
            ClientMessagesM.editTile(this.getToken(), this.getDevice(), tag);
        });
        this.addRenderableWidget(this.mCustomName);
        this.mPriority = FluxEditBox.create(FluxTranslate.PRIORITY.get() + ": ", this.font, this.leftPos + 16, this.topPos + 45, 144, 12).setOutlineColor(color).setDigitsOnly().setAllowNegatives(true);
        this.mPriority.setMaxLength(5);
        this.mPriority.setValue(String.valueOf(this.getDevice().getRawPriority()));
        this.mPriority.setResponder((string) -> {
            int priority = Mth.clamp(this.mPriority.getValidInt(), -9999, 9999);
            CompoundTag tag = new CompoundTag();
            tag.putInt("priority", priority);
            ClientMessagesM.editTile(this.getToken(), this.getDevice(), tag);
        });
        this.addRenderableWidget(this.mPriority);
        this.mLimit = FluxEditBox.create(FluxTranslate.TRANSFER_LIMIT.get() + ": ", this.font, this.leftPos + 16, this.topPos + 62, 144, 12).setOutlineColor(color).setDigitsOnly().setMaxValue(Long.MAX_VALUE);
        this.mLimit.setMaxLength(15);
        this.mLimit.setValue(String.valueOf(this.getDevice().getRawLimit()));
        this.mLimit.setResponder((string) -> {
            long limit = this.mLimit.getValidLong();
            CompoundTag tag = new CompoundTag();
            tag.putLong("limit", limit);
            ClientMessagesM.editTile(this.getToken(), this.getDevice(), tag);
        });
        this.addRenderableWidget(this.mLimit);
        this.mSurgeMode = new SwitchButton(this, this.leftPos + 140, this.topPos + 120, this.getDevice().getSurgeMode(), color);
        this.mDisableLimit = new SwitchButton(this, this.leftPos + 140, this.topPos + 132, this.getDevice().getDisableLimit(), color);
        this.mButtons.add(this.mSurgeMode);
        this.mButtons.add(this.mDisableLimit);
        if (!this.getDevice().getDeviceType().isStorage()) {
            this.mChunkLoading = new SwitchButton(this, this.leftPos + 140, this.topPos + 144, this.getDevice().isForcedLoading(), color);
            this.mChunkLoading.setClickable(FluxConfig.enableChunkLoading);
            this.mButtons.add(this.mChunkLoading);
        }

    }

    protected void drawForegroundLayer(GuiGraphics gr, int mouseX, int mouseY, float deltaTicks) {
        super.drawForegroundLayer(gr, mouseX, mouseY, deltaTicks);
        int color = this.getNetwork().getNetworkColor();
        this.renderNetwork(gr, this.getNetwork().getNetworkName(), color, this.topPos + 8);
        this.renderTransfer(gr, this.getDevice(), this.leftPos + 30, this.topPos + 90);
        if (this.mCustomName.getValue().isEmpty()) {
            int y = this.mCustomName.getY() + (this.mCustomName.getHeight() - 8) / 2;
            gr.drawString(this.font, Language.getInstance().getOrDefault(this.getDevice().getBlockState().getBlock().getDescriptionId()), this.mCustomName.getX() + 4, y, 11711154);
        }

        gr.drawString(this.font, FluxTranslate.SURGE_MODE.get(), 20 + this.leftPos, 120 + this.topPos, color);
        gr.drawString(this.font, FluxTranslate.DISABLE_LIMIT.get(), 20 + this.leftPos, 132 + this.topPos, color);
        if (this.mChunkLoading != null) {
            gr.drawString(this.font, FluxTranslate.CHUNK_LOADING.get(), 20 + this.leftPos, 144 + this.topPos, color);
        }

    }

    public void onButtonClicked(GuiButtonCore button, float mouseX, float mouseY, int mouseButton) {
        super.onButtonClicked(button, mouseX, mouseY, mouseButton);
        if (mouseButton == 0 && button instanceof SwitchButton switchButton) {
            if (switchButton == this.mSurgeMode) {
                switchButton.toggle();
                CompoundTag tag = new CompoundTag();
                tag.putBoolean("surge_mode", this.mSurgeMode.isChecked());
                ClientMessagesM.editTile(this.getToken(), this.getDevice(), tag);
            } else if (switchButton == this.mDisableLimit) {
                switchButton.toggle();
                CompoundTag tag = new CompoundTag();
                tag.putBoolean("disable_limit", this.mDisableLimit.isChecked());
                ClientMessagesM.editTile(this.getToken(), this.getDevice(), tag);
            } else if (switchButton == this.mChunkLoading) {
                CompoundTag tag = new CompoundTag();
                tag.putBoolean("forced_loading", !this.mChunkLoading.isChecked());
                ClientMessagesM.editTile(this.getToken(), this.getDevice(), tag);
            }
        }

    }

    protected void containerTick() {
        super.containerTick();
        if (this.mCustomName != null) {
            int color = this.getNetwork().getNetworkColor() | -16777216;
            this.mCustomName.setOutlineColor(color);
            this.mPriority.setOutlineColor(color);
            this.mLimit.setOutlineColor(color);
            this.mSurgeMode.setColor(color);
            this.mDisableLimit.setColor(color);
            if (this.mChunkLoading != null) {
                this.mChunkLoading.setColor(color);
                this.mChunkLoading.setChecked(this.getDevice().isForcedLoading());
            }
        }

    }

    public boolean onMouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (super.onMouseClicked(mouseX, mouseY, mouseButton)) {
            return true;
        } else if (mouseButton == 0 && mouseX >= (double) (this.leftPos + 20) && mouseX < (double) (this.leftPos + 155) && mouseY >= (double) (this.topPos + 8) && mouseY < (double) (this.topPos + 20)) {
            this.switchTab(EnumNavigationTab.TAB_SELECTION, false);
            return true;
        } else {
            return false;
        }
    }
}

