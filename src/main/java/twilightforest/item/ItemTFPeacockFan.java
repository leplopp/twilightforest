package twilightforest.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ItemTFPeacockFan extends ItemTF
{
	protected ItemTFPeacockFan() {
		this.setCreativeTab(TFItems.creativeTab);
		this.maxStackSize = 1;
        this.setMaxDamage(1024);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

		if (!world.isRemote) 
		{
			if (!player.onGround)
			{
				player.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 45, 0));
			}
			else
			{
				int fanned = 0;

				fanned = doFan(world, player);

				if (fanned > 0)
				{
					player.getHeldItem(hand).damageItem(fanned, player);
				}
			}
			
		}
		else
		{

			
			// jump if the player is in the air
			//TODO: only one extra jump per jump
			if (!player.onGround && !player.isPotionActive(MobEffects.JUMP_BOOST))
			{
				player.motionX *= 3F;
				player.motionY = 1.5F;
				player.motionZ *= 3F;
				player.fallDistance = 0.0F;
			}
			else
			{
				AxisAlignedBB fanBox = getEffectAABB(world, player);
				Vec3d lookVec = player.getLookVec();

				// particle effect
				for (int i = 0; i < 30; i++)
				{
					world.spawnParticle(EnumParticleTypes.CLOUD, fanBox.minX + world.rand.nextFloat() * (fanBox.maxX - fanBox.minX),
							fanBox.minY + world.rand.nextFloat() * (fanBox.maxY - fanBox.minY), 
							fanBox.minZ + world.rand.nextFloat() * (fanBox.maxZ - fanBox.minZ), 
							lookVec.xCoord, lookVec.yCoord, lookVec.zCoord);
				}
				
			}

			player.playSound(SoundEvents.ENTITY_PLAYER_BREATH, 1.0F + itemRand.nextFloat(), itemRand.nextFloat() * 0.7F + 0.3F);
		}
		
		player.setActiveHand(hand);
		
		return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
	}
	
    @Override
	public EnumAction getItemUseAction(ItemStack par1ItemStack)
    {
        return EnumAction.BLOCK;
    }
    
    @Override
	public int getMaxItemUseDuration(ItemStack par1ItemStack)
    {
        return 20;
    }
    
    @Override
	public boolean isFull3D()
    {
        return true;
    }

	/**
	 * Fannnn
	 */
	private int doFan(World world, EntityPlayer player)
	{
		AxisAlignedBB fanBox = getEffectAABB(world, player);
		
		fanBlocksInAABB(world, player, fanBox);
		
		fanEntitiesInAABB(world, player, fanBox);
		
		return 1;
	}

	/**
	 * Move entities in the box
	 */
	private void fanEntitiesInAABB(World world, EntityPlayer player, AxisAlignedBB fanBox) 
	{
		Vec3d moveVec = player.getLookVec();
		
		List<Entity> inBox = world.getEntitiesWithinAABB(Entity.class, fanBox);
		
		float force = 2.0F;
		
		for (Entity entity : inBox)
		{
			if (entity.canBePushed() || entity instanceof EntityItem)
			{
				entity.motionX = moveVec.xCoord * force;
				entity.motionY = moveVec.yCoord * force;
				entity.motionZ = moveVec.zCoord * force;
			}
		}
		
	}

	private AxisAlignedBB getEffectAABB(World world, EntityPlayer player) {
		double range = 3.0D;
		double radius = 2.0D;
		Vec3d srcVec = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
		Vec3d lookVec = player.getLookVec();
		Vec3d destVec = srcVec.addVector(lookVec.xCoord * range, lookVec.yCoord * range, lookVec.zCoord * range);

		return new AxisAlignedBB(destVec.xCoord - radius, destVec.yCoord - radius, destVec.zCoord - radius, destVec.xCoord + radius, destVec.yCoord + radius, destVec.zCoord + radius);
	}


	/**
     * Do fan effects on blocks in the bounding box
	 * @param player 
     */
    private int fanBlocksInAABB(World world, EntityPlayer player, AxisAlignedBB par1AxisAlignedBB)
    {
    	
        int minX = MathHelper.floor(par1AxisAlignedBB.minX);
        int minY = MathHelper.floor(par1AxisAlignedBB.minY);
        int minZ = MathHelper.floor(par1AxisAlignedBB.minZ);
        int maxX = MathHelper.floor(par1AxisAlignedBB.maxX);
        int maxY = MathHelper.floor(par1AxisAlignedBB.maxY);
        int maxZ = MathHelper.floor(par1AxisAlignedBB.maxZ);

        int fan = 0;

        for (int dx = minX; dx <= maxX; ++dx)
        {
            for (int dy = minY; dy <= maxY; ++dy)
            {
                for (int dz = minZ; dz <= maxZ; ++dz)
                {
                    fan += fanBlock(world, player, new BlockPos(dx, dy, dz));
                }
            }
        }
        


        return fan;
    }

	private int fanBlock(World world, EntityPlayer player, BlockPos pos) {
		int cost = 0;
		
		IBlockState state = world.getBlockState(pos);
		
		if (state.getBlock() != Blocks.AIR)
		{
			if (state.getBlock() instanceof BlockFlower)
			{
				if(state.getBlock().canHarvestBlock(world, pos, player) && itemRand.nextInt(3) == 0)
				{
					state.getBlock().harvestBlock(world, player, pos, state, world.getTileEntity(pos), null);
					world.destroyBlock(pos, false);
				}
			}
		}
		
		return cost;
	}
}
