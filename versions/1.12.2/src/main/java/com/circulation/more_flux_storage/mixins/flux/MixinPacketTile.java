package com.circulation.more_flux_storage.mixins.flux;

import com.circulation.more_flux_storage.api.IFluxGuiConnector;
import com.circulation.more_flux_storage.util.PacketTileHandler;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sonar.fluxnetworks.common.handler.PacketHandler;
import sonar.fluxnetworks.common.network.PacketTile;

@Mixin(value = PacketTile.class, remap = false)
public class MixinPacketTile {

    @Inject(method = "onMessage(Lsonar/fluxnetworks/common/network/PacketTile$TileMessage;Lnet/minecraftforge/fml/common/network/simpleimpl/MessageContext;)Lnet/minecraftforge/fml/common/network/simpleimpl/IMessage;", at = @At(value = "CONSTANT", shift = At.Shift.AFTER, args = "classValue=sonar/fluxnetworks/common/tileentity/TileFluxCore"))
    public void onMessage(PacketTile.TileMessage message, MessageContext ctx, CallbackInfoReturnable<IMessage> cir, @Local(name = "tile") TileEntity tile, @Local(name = "player") EntityPlayer player) {
        if (tile instanceof IFluxGuiConnector g) {
            PacketHandler.handlePacket(() -> {
                IMessage returned = switch (message.handler) {
                    case CHUNK_LOADING -> PacketTileHandler.handleChunkLoadPacket(g, player, message.tag);
                    case SET_NETWORK -> PacketTileHandler.handleSetNetworkPacket(g, player, message.tag);
                };
                if (returned != null && player instanceof EntityPlayerMP) {
                    PacketHandler.network.sendTo(returned, (EntityPlayerMP) player);
                }

            }, ctx.netHandler);
        }
    }

}
