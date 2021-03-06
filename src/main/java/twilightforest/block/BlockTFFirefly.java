package twilightforest.block;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twilightforest.client.ModelRegisterCallback;
import twilightforest.tileentity.TileEntityTFFirefly;

public class BlockTFFirefly extends BlockTFCritter implements ModelRegisterCallback {

	public static final Random rand = new Random();
	
	protected BlockTFFirefly() {
		this.setLightLevel(0.9375F);
	}

	@Override
    public int tickRate(World world)
    {
        return 50 + rand.nextInt(50);
    }
    
	@Override
	@Deprecated
	public int getLightValue(IBlockState state) {
    	return 15; 
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityTFFirefly();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModel() {
		ModelLoader.setCustomStateMapper(this, new StateMap.Builder().ignore(TFBlockProperties.FACING).build());
		// todo fix up display transforms
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
		ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(this), 0, TileEntityTFFirefly.class);
	}
}
