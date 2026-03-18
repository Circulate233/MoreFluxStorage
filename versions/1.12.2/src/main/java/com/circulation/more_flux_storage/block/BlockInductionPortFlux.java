package com.circulation.more_flux_storage.block;

import com.circulation.more_flux_storage.tile.TileInductionPortFlux;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockInductionPortFlux extends AbstractFluxConnectorBlock {

    public BlockInductionPortFlux() {
        super(TileInductionPortFlux.class, "induction_port_flux");
    }

    @Override
    protected TileEntity createFluxTileEntity(World world, IBlockState state) {
        return new TileInductionPortFlux();
    }

    @Override
    protected void onTilePlaced(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack,
                                TileEntity tileEntity) {
        if (tileEntity instanceof TileInductionPortFlux fluxTile && placer instanceof EntityPlayer player) {
            fluxTile.setPlayerUUID(player.getUniqueID());
        }
    }
}
