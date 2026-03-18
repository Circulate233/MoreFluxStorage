package com.circulation.more_flux_storage.proxy;

import com.circulation.more_flux_storage.MoreFluxStorage;
import com.circulation.more_flux_storage.api.IFluxGuiConnector;
import com.circulation.more_flux_storage.common.network.PacketFluxGuiAction;
import com.circulation.more_flux_storage.registry.RegistryBlocks;
import com.circulation.more_flux_storage.registry.RegistryItems;
import com.circulation.more_flux_storage.util.Packet;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import sonar.fluxnetworks.common.core.ContainerCore;

import static com.circulation.more_flux_storage.MoreFluxStorage.NET_CHANNEL;

@SuppressWarnings("unused")
public class CommonProxy implements IGuiHandler {

    private int id = 0;

    public static void openGui(EntityPlayer player, BlockPos pos) {
        player.openGui(MoreFluxStorage.instance, 0,
            player.world, pos.getX(), pos.getY(), pos.getZ());
    }

    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        NetworkRegistry.INSTANCE.registerGuiHandler(MoreFluxStorage.instance, this);
        registerMessage(PacketFluxGuiAction.class, Side.SERVER);
    }

    public void init() {
    }

    public void postInit() {
    }

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
        if (tileEntity instanceof IFluxGuiConnector connector) {
            if (connector.canOpenFluxGui(player)) {
                return new ContainerCore(player, connector);
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    public <T extends Packet<T>> void registerMessage(T aClass, Side side) {
        //noinspection unchecked
        NET_CHANNEL.registerMessage(aClass, (Class<T>) aClass.getClass(), id++, side);
    }

    public <T extends Packet<T>> void registerMessage(Class<T> aClass, Side side) {
        NET_CHANNEL.registerMessage(aClass, aClass, id++, side);
    }

    @SubscribeEvent
    public void registryItem(RegistryEvent.Register<Item> event) {
        RegistryItems.registerItems(event);
    }

    @SubscribeEvent
    public void registryBlock(RegistryEvent.Register<Block> event) {
        RegistryBlocks.registerBlocks(event);
    }


}
