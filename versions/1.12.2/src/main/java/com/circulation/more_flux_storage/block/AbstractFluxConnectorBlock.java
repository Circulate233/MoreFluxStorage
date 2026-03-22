package com.circulation.more_flux_storage.block;

import com.circulation.more_flux_storage.MoreFluxStorage;
import com.circulation.more_flux_storage.api.IFluxGuiConnector;
import com.circulation.more_flux_storage.proxy.CommonProxy;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"deprecation", "unused"})
public abstract class AbstractFluxConnectorBlock extends Block implements ITileEntityProvider {

    private static final String BLOCK_ENTITY_TAG = "BlockEntityTag";

    protected AbstractFluxConnectorBlock(Class<? extends TileEntity> tileEntityClass, String name) {
        super(Material.IRON);
        this.setRegistryName(new ResourceLocation(MoreFluxStorage.MOD_ID, name))
            .setTranslationKey(MoreFluxStorage.MOD_ID + "." + name)
            .setCreativeTab(MoreFluxStorage.CREATIVE_TAB);

        TileEntity.register(MoreFluxStorage.MOD_ID + ":" + name, tileEntityClass);
    }

    protected abstract TileEntity createFluxTileEntity(World world, IBlockState state);

    protected void onTilePlaced(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack,
                                TileEntity tileEntity) {
    }

    protected void onTileBroken(World world, BlockPos pos, IBlockState state, TileEntity tileEntity) {
    }

    @Override
    public boolean hasTileEntity(@NotNull IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(@NotNull World world, int meta) {
        return createFluxTileEntity(world, getStateFromMeta(meta));
    }

    @Override
    public TileEntity createTileEntity(@NotNull World world, @NotNull IBlockState state) {
        return createFluxTileEntity(world, state);
    }

    @Override
    public boolean onBlockActivated(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player,
                                    @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof IFluxGuiConnector connector && connector.canOpenFluxGui(player)) {
                CommonProxy.openGui(player, pos);
            }
        }
        return true;
    }

    @Override
    public void getDrops(@NotNull NonNullList<ItemStack> drops, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                         @NotNull IBlockState state, int fortune) {
        Item item = Item.getItemFromBlock(this);
        if (item == null) {
            return;
        }
        drops.clear();
        ItemStack stack = new ItemStack(item, 1, damageDropped(state));
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity != null) {
            writeTileEntityToStack(stack, tileEntity);
        }
        drops.add(stack);
    }

    @Override
    public @NotNull ItemStack getPickBlock(@NotNull IBlockState state, @NotNull RayTraceResult target, @NotNull World world, @NotNull BlockPos pos, @NotNull EntityPlayer player) {
        ItemStack stack = super.getPickBlock(state, target, world, pos, player);
        TileEntity tileEntity = world.getTileEntity(pos);
        if (!stack.isEmpty() && tileEntity != null) {
            writeTileEntityToStack(stack, tileEntity);
        }
        return stack;
    }

    @Override
    public void breakBlock(World world, @NotNull BlockPos pos, @NotNull IBlockState state) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity != null) {
            onTileBroken(world, pos, state, tileEntity);
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public void onBlockPlacedBy(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state,
                                @NotNull EntityLivingBase placer, @NotNull ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity != null) {
            readTileEntityFromStack(stack, tileEntity);
            onTilePlaced(world, pos, state, placer, stack, tileEntity);
        }
    }

    protected void writeTileEntityToStack(ItemStack stack, TileEntity tileEntity) {
        NBTTagCompound tileTag = tileEntity.writeToNBT(new NBTTagCompound());
        stack.setTagInfo(BLOCK_ENTITY_TAG, tileTag);
    }

    protected void readTileEntityFromStack(ItemStack stack, TileEntity tileEntity) {
        if (!stack.hasTagCompound()) {
            return;
        }
        NBTTagCompound stackTag = stack.getTagCompound();
        if (stackTag == null || !stackTag.hasKey(BLOCK_ENTITY_TAG, 10)) {
            return;
        }
        NBTTagCompound tileTag = stackTag.getCompoundTag(BLOCK_ENTITY_TAG).copy();
        BlockPos pos = tileEntity.getPos();
        tileTag.setInteger("x", pos.getX());
        tileTag.setInteger("y", pos.getY());
        tileTag.setInteger("z", pos.getZ());
        tileEntity.readFromNBT(tileTag);
        tileEntity.markDirty();
        if (tileEntity.getWorld() != null && !tileEntity.getWorld().isRemote) {
            IBlockState state = tileEntity.getWorld().getBlockState(pos);
            tileEntity.getWorld().notifyBlockUpdate(pos, state, state, 3);
        }
    }

    @SideOnly(Side.CLIENT)
    public @NotNull BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean isOpaqueCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(@NotNull IBlockState state) {
        return false;
    }
}
