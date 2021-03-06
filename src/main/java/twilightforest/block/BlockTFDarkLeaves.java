package twilightforest.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import twilightforest.client.ModelRegisterCallback;
import twilightforest.item.TFItems;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockTFDarkLeaves extends Block implements ModelRegisterCallback {

	protected BlockTFDarkLeaves() {
		super(Material.LEAVES);
		this.setHardness(2F);
		this.setResistance(10F);
		this.setSoundType(SoundType.PLANT);
		this.setCreativeTab(TFItems.creativeTab);
	}
	
    @Override
	public int damageDropped(IBlockState state) {
    	return 3;
	}

    @Override
	public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing side) {
		return 1;
	}

	@Override
	public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing side) {
		return 0;
	}

    @Override
	public int quantityDropped(Random par1Random)
    {
    	return par1Random.nextInt(40) == 0 ? 1 : 0;
    }

    @Override
	public Item getItemDropped(IBlockState state, Random par2Random, int par3)
    {
    	return Item.getItemFromBlock(TFBlocks.sapling);
    }

    @Override
	public void dropBlockAsItemWithChance(World par1World, BlockPos pos, IBlockState state, float par6, int fortune)
    {
    	if (!par1World.isRemote)
    	{
    		if (par1World.rand.nextInt(40) == 0)
    		{
    			this.dropBlockAsItem(par1World, pos, state, fortune);
    		}
    	}
    }

}

