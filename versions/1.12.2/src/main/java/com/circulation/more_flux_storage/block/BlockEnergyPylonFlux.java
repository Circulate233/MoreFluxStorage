package com.circulation.more_flux_storage.block;

import com.circulation.more_flux_storage.tile.TileEnergyPylonFlux;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlockEnergyPylonFlux extends AbstractFluxConnectorBlock {

    public BlockEnergyPylonFlux() {
        super(TileEnergyPylonFlux.class, "energy_pylon_flux");
    }

    @Override
    protected TileEntity createFluxTileEntity(World world, IBlockState state) {
        return new TileEnergyPylonFlux();
    }

    @Override
    protected void onTilePlaced(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack,
                                TileEntity tileEntity) {
        if (tileEntity instanceof TileEnergyPylonFlux fluxTile && placer instanceof EntityPlayer player) {
            fluxTile.setPlayerUUID(player.getUniqueID());
        }
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World worldIn, @NotNull List<String> tooltip, @NotNull ITooltipFlag flagIn) {
        tooltip.add("" + TextFormatting.GRAY + TextFormatting.ITALIC + I18n.format("block.more_flux_storage.energy_pylon_flux.tooltip"));
    }
}
