package com.circulation.more_flux_storage.util;

import com.circulation.more_flux_storage.api.IFluxGuiConnector;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import sonar.fluxnetworks.FluxConfig;
import sonar.fluxnetworks.api.gui.EnumFeedbackInfo;
import sonar.fluxnetworks.api.network.FluxLogicType;
import sonar.fluxnetworks.api.network.IFluxNetwork;
import sonar.fluxnetworks.api.network.NetworkSettings;
import sonar.fluxnetworks.common.connection.FluxNetworkCache;
import sonar.fluxnetworks.common.data.FluxChunkManager;
import sonar.fluxnetworks.common.data.FluxNetworkData;
import sonar.fluxnetworks.common.network.PacketFeedback;

public class PacketTileHandler {

    public static IMessage handleSetNetworkPacket(IFluxGuiConnector tile, EntityPlayer player, NBTTagCompound tag) {
        int id = tag.getInteger(FluxNetworkData.NETWORK_ID);
        String pass = tag.getString(FluxNetworkData.NETWORK_PASSWORD);
        if (tile.getNetworkID() == id) {
            return null;
        } else {
            IFluxNetwork network = FluxNetworkCache.instance.getNetwork(id);
            if (!network.isInvalid()) {
                if (tile.getConnectionType().isController() && !network.getConnections(FluxLogicType.CONTROLLER).isEmpty()) {
                    return new PacketFeedback.FeedbackMessage(EnumFeedbackInfo.HAS_CONTROLLER);
                } else {
                    if (!network.getMemberPermission(player).canAccess()) {
                        if (pass.isEmpty()) {
                            return new PacketFeedback.FeedbackMessage(EnumFeedbackInfo.PASSWORD_REQUIRE);
                        }

                        if (!pass.equals(network.getSetting(NetworkSettings.NETWORK_PASSWORD))) {
                            return new PacketFeedback.FeedbackMessage(EnumFeedbackInfo.REJECT);
                        }
                    }

                    if (tile.getNetwork() != null && !tile.getNetwork().isInvalid()) {
                        tile.getNetwork().queueConnectionRemoval(tile, false);
                    }

                    tile.setPlayerUUID(EntityPlayer.getUUID(player.getGameProfile()));
                    network.queueConnectionAddition(tile);
                    return new PacketFeedback.FeedbackMessage(EnumFeedbackInfo.SUCCESS);
                }
            } else {
                return null;
            }
        }
    }

    public static IMessage handleChunkLoadPacket(IFluxGuiConnector tile, EntityPlayer player, NBTTagCompound tag) {
        boolean load = tag.getBoolean("c");
        if (FluxConfig.enableChunkLoading) {
            if (load) {
                boolean p = FluxChunkManager.forceChunk(tile.getFluxGuiWorld(), new ChunkPos(tile.getFluxGuiPos()));
                tile.setChunkLoaded(p);
                return !p ? new PacketFeedback.FeedbackMessage(EnumFeedbackInfo.HAS_LOADER) : null;
            } else {
                FluxChunkManager.releaseChunk(tile.getFluxGuiWorld(), new ChunkPos(tile.getFluxGuiPos()));
                tile.setChunkLoaded(false);
                return null;
            }
        } else {
            tile.setChunkLoaded(false);
            return new PacketFeedback.FeedbackMessage(EnumFeedbackInfo.BANNED_LOADING);
        }
    }
}
