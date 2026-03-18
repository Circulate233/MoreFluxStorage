package com.circulation.more_flux_storage.util;

//? if =1.12.2 {
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

public interface Packet<T extends Packet<T>> extends IMessageHandler<T, IMessage>, IMessage {
}
//?} else {
/*public interface Packet<T> {
}
*/
//?}
