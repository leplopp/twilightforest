package twilightforest.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import twilightforest.TwilightForestMod;
import twilightforest.block.BlockTFMazestone;
import twilightforest.block.TFBlocks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twilightforest.block.enums.MazestoneVariant;

public class ItemTFCrumbleHorn extends ItemTF
{
	private static final int CHANCE_HARVEST = 20;
	private static final int CHANCE_CRUMBLE = 5;

	protected ItemTFCrumbleHorn() {
		this.setCreativeTab(TFItems.creativeTab);
		this.maxStackSize = 1;
        this.setMaxDamage(1024);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		player.setActiveHand(hand);
		player.playSound(SoundEvents.ENTITY_SHEEP_AMBIENT, 1.0F, 0.8F);
		return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
	}

    @Override
	public void onUsingTick(ItemStack stack, EntityLivingBase living, int count)
    {
		if (count > 10 && count % 5 == 0 && !living.world.isRemote)
		{
			int crumbled = doCrumble(living.world, living);

			if (crumbled > 0)
			{
				stack.damageItem(crumbled, living);

			}
			
			living.world.playSound(null, living.posX, living.posY, living.posZ, SoundEvents.ENTITY_SHEEP_AMBIENT, living.getSoundCategory(), 1.0F, 0.8F);
		}
    }
	
    @Override
	public EnumAction getItemUseAction(ItemStack par1ItemStack)
    {
        return EnumAction.BOW;
    }
    
    @Override
	public int getMaxItemUseDuration(ItemStack par1ItemStack)
    {
        return 72000;
    }

	/**
	 * Bleeeat!
	 */
	private int doCrumble(World world, EntityLivingBase living)
	{
		double range = 3.0D;
		double radius = 2.0D;
		Vec3d srcVec = new Vec3d(living.posX, living.posY + living.getEyeHeight(), living.posZ);
		Vec3d lookVec = living.getLookVec();
		Vec3d destVec = srcVec.addVector(lookVec.xCoord * range, lookVec.yCoord * range, lookVec.zCoord * range);
		
		AxisAlignedBB crumbleBox =  new AxisAlignedBB(destVec.xCoord - radius, destVec.yCoord - radius, destVec.zCoord - radius, destVec.xCoord + radius, destVec.yCoord + radius, destVec.zCoord + radius);
		
		return crumbleBlocksInAABB(world, living, crumbleBox);
	}

    private int crumbleBlocksInAABB(World world, EntityLivingBase living, AxisAlignedBB par1AxisAlignedBB)
    {
    	//System.out.println("Destroying blocks in " + par1AxisAlignedBB);
    	
        int minX = MathHelper.floor(par1AxisAlignedBB.minX);
        int minY = MathHelper.floor(par1AxisAlignedBB.minY);
        int minZ = MathHelper.floor(par1AxisAlignedBB.minZ);
        int maxX = MathHelper.floor(par1AxisAlignedBB.maxX);
        int maxY = MathHelper.floor(par1AxisAlignedBB.maxY);
        int maxZ = MathHelper.floor(par1AxisAlignedBB.maxZ);

        int crumbled = 0;

        for (int dx = minX; dx <= maxX; ++dx)
        {
            for (int dy = minY; dy <= maxY; ++dy)
            {
                for (int dz = minZ; dz <= maxZ; ++dz)
                {
                    crumbled += crumbleBlock(world, living, new BlockPos(dx, dy, dz));
                }
            }
        }
        
        return crumbled;
    }

    /**
     * Crumble a specific block.
     */
	private int crumbleBlock(World world, EntityLivingBase living, BlockPos pos) {
		int cost = 0;

		IBlockState state = world.getBlockState(pos);
		Block currentID = state.getBlock();
		
		if (currentID != Blocks.AIR)
		{
			if (currentID == Blocks.STONE && world.rand.nextInt(CHANCE_CRUMBLE) == 0)
		    {
		        world.setBlockState(pos, Blocks.COBBLESTONE.getDefaultState(), 3);
				world.playEvent(2001, pos, Block.getStateId(state));
		        cost++;
		    }
			
			if (currentID == Blocks.STONEBRICK && state.getValue(BlockStoneBrick.VARIANT) == BlockStoneBrick.EnumType.DEFAULT && world.rand.nextInt(CHANCE_CRUMBLE) == 0)
		    {
		        world.setBlockState(pos, state.withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.CRACKED), 3);
				world.playEvent(2001, pos, Block.getStateId(state));
		        cost++;
		    }
			
			if (currentID == TFBlocks.mazestone && state.getValue(BlockTFMazestone.VARIANT) == MazestoneVariant.BRICK && world.rand.nextInt(CHANCE_CRUMBLE) == 0)
		    {
		        world.setBlockState(pos, TFBlocks.mazestone.getDefaultState().withProperty(BlockTFMazestone.VARIANT, MazestoneVariant.CRACKED), 3);
				world.playEvent(2001, pos, Block.getStateId(state));
		        cost++;
		    }
			
			if (currentID == Blocks.COBBLESTONE && world.rand.nextInt(CHANCE_CRUMBLE) == 0)
		    {
		        world.setBlockState(pos, Blocks.GRAVEL.getDefaultState(), 3);
				world.playEvent(2001, pos, Block.getStateId(state));
		        cost++;
		    }
			
			if (currentID == Blocks.GRAVEL || currentID == Blocks.DIRT)
			{
				if (living instanceof EntityPlayer)
				{
					if (currentID.canHarvestBlock(world, pos, (EntityPlayer) living) && world.rand.nextInt(CHANCE_HARVEST) == 0)
					{
						world.setBlockToAir(pos);
						currentID.harvestBlock(world, (EntityPlayer) living, pos, state, world.getTileEntity(pos), null);
						world.playEvent(2001, pos, Block.getStateId(state));
						cost++;
					}
				} else if (world.getGameRules().getBoolean("mobGriefing") && world.rand.nextInt(CHANCE_HARVEST) == 0)
				{
					world.destroyBlock(pos, true);
					cost++;
				}
			}

		}
		
		return cost;
	}
}
