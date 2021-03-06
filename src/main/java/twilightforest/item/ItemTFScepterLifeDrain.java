package twilightforest.item;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemTFScepterLifeDrain extends ItemTF {

	protected ItemTFScepterLifeDrain() {
        this.maxStackSize = 1;
        this.setMaxDamage(99);
		this.setCreativeTab(TFItems.creativeTab);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		if (player.getHeldItem(hand).getItemDamage() < this.getMaxDamage(player.getHeldItem(hand))) {
			player.setActiveHand(hand);
		}
		else {
			player.resetActiveHand();
		}
		
		return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
	}

	/**
	 * Animates the target falling apart into a rain of shatter particles
	 */
	private static void animateTargetShatter(World world, EntityLivingBase target) {
		for (int var1 = 0; var1 < 50; ++var1)
		{
		    double gaussX = itemRand.nextGaussian() * 0.02D;
		    double gaussY = itemRand.nextGaussian() * 0.02D;
		    double gaussZ = itemRand.nextGaussian() * 0.02D;
		    double gaussFactor = 10.0D;
		    
		    Item popItem = getTargetDropItemId(target) != null ? getTargetDropItemId(target) : Items.ROTTEN_FLESH;
		    
		    world.spawnParticle(EnumParticleTypes.ITEM_CRACK, target.posX + itemRand.nextFloat() * target.width * 2.0F - target.width - gaussX * gaussFactor, target.posY + itemRand.nextFloat() * target.height - gaussY * gaussFactor, target.posZ + itemRand.nextFloat() * target.width * 2.0F - target.width - gaussZ * gaussFactor, gaussX, gaussY, gaussZ, Item.getIdFromItem(popItem));
		}
	}
	
	private static Item getTargetDropItemId(EntityLivingBase target) {
		//TODO: make this actually work
		return Items.ROTTEN_FLESH;
	}

	/**
	 * What, if anything, is the player currently looking at?
	 */
	private Entity getPlayerLookTarget(World world, EntityLivingBase living) {
		Entity pointedEntity = null;
		double range = 20.0D;
        Vec3d srcVec = new Vec3d(living.posX, living.posY + living.getEyeHeight(), living.posZ);
        Vec3d lookVec = living.getLook(1.0F);
        Vec3d destVec = srcVec.addVector(lookVec.xCoord * range, lookVec.yCoord * range, lookVec.zCoord * range);
        float var9 = 1.0F;
        List<Entity> possibleList = world.getEntitiesWithinAABBExcludingEntity(living, living.getEntityBoundingBox().addCoord(lookVec.xCoord * range, lookVec.yCoord * range, lookVec.zCoord * range).expand(var9, var9, var9));
        double hitDist = 0;

        for (Entity possibleEntity : possibleList)
        {
            

            if (possibleEntity.canBeCollidedWith())
            {
                float borderSize = possibleEntity.getCollisionBorderSize();
                AxisAlignedBB collisionBB = possibleEntity.getEntityBoundingBox().expand((double)borderSize, (double)borderSize, (double)borderSize);
                RayTraceResult interceptPos = collisionBB.calculateIntercept(srcVec, destVec);

                if (collisionBB.isVecInside(srcVec))
                {
                    if (0.0D < hitDist || hitDist == 0.0D)
                    {
                        pointedEntity = possibleEntity;
                        hitDist = 0.0D;
                    }
                }
                else if (interceptPos != null)
                {
                    double possibleDist = srcVec.distanceTo(interceptPos.hitVec);

                    if (possibleDist < hitDist || hitDist == 0.0D)
                    {
                        pointedEntity = possibleEntity;
                        hitDist = possibleDist;
                    }
                }
            }
        }
		return pointedEntity;
	}
	
	
	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase living, int count) {
		World world = living.world;
		
		if (stack.getItemDamage() >= this.getMaxDamage(stack)) {
			// do not use
			living.resetActiveHand();
			return;
		}

		if (count % 5 == 0) {

			// is the player looking at an entity
			Entity pointedEntity = getPlayerLookTarget(world, living);

			if (pointedEntity != null && pointedEntity instanceof EntityLivingBase) {
				EntityLivingBase target =  (EntityLivingBase)pointedEntity;

				if (target.getActivePotionEffect(MobEffects.SLOWNESS) != null || target.getHealth() < 1) {

					if (target.getHealth() <= 3) {
						// make it explode

						makeRedMagicTrail(world,  living.posX, living.posY + living.getEyeHeight(), living.posZ, target.posX, target.posY + target.getEyeHeight(), target.posZ);
						if (target instanceof EntityLiving)
						{
							((EntityLiving) target).spawnExplosionParticle();
						}
						target.playSound(SoundEvents.ENTITY_GENERIC_BIG_FALL, 1.0F, ((itemRand.nextFloat() - itemRand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
						animateTargetShatter(world, (EntityLivingBase) target);
						if (!world.isRemote) {
							target.setDead();
							target.onDeath(DamageSource.causeIndirectMagicDamage(living, living));
						}
						living.resetActiveHand();
					}
					else {
						// we have hit this creature recently
						if (!world.isRemote) {
							target.attackEntityFrom(DamageSource.causeIndirectMagicDamage(living, living), 3);

							// only do lifting effect on creatures weaker than the player
							if (getMaxHealth(target) <= getMaxHealth(living)) {
								target.motionX = 0;
								target.motionY = 0.2;
								target.motionZ = 0;
							}

							target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 20, 2));

							if (count % 10 == 0) {
								// heal the player
								living.heal(1);
								// and give foods
								if (living instanceof EntityPlayer)
									((EntityPlayer) living).getFoodStats().addStats(1, 0.1F);
							}
						}
					}
				}
				else {
					// this is a new creature to start draining
					makeRedMagicTrail(world,  living.posX, living.posY + living.getEyeHeight(), living.posZ, target.posX, target.posY + target.getEyeHeight(), target.posZ);

					living.playSound(SoundEvents.ITEM_FLINTANDSTEEL_USE, 1.0F, (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F + 1.0F);

					if (!world.isRemote) {
						target.attackEntityFrom(DamageSource.causeIndirectMagicDamage(living, living), 1);
						
						// only do lifting effect on creatures weaker than the player
						if (getMaxHealth(target) <= getMaxHealth(living)) {
							target.motionX = 0;
							target.motionY = 0.2;
							target.motionZ = 0;
						}

						target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 20, 2));
					}
				}

				if (!world.isRemote) {
					stack.damageItem(1, living);
				}

			}
		}

	}
	
	private float getMaxHealth(EntityLivingBase target)
	{
		return (float) target.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue();
	}

	private void makeRedMagicTrail(World world, double srcX, double srcY, double srcZ, double destX, double destY, double destZ) {
		// make particle trail
    	int particles = 32;
    	for (int i = 0; i < particles; i++)
    	{
    		double trailFactor = i / (particles - 1.0D);
    		float f = 1.0F;
    		float f1 = 0.5F;
    		float f2 = 0.5F;
    		double tx = srcX + (destX - srcX) * trailFactor + world.rand.nextGaussian() * 0.005;
    		double ty = srcY + (destY - srcY) * trailFactor + world.rand.nextGaussian() * 0.005;
    		double tz = srcZ + (destZ - srcZ) * trailFactor + world.rand.nextGaussian() * 0.005;
    		world.spawnParticle(EnumParticleTypes.SPELL_MOB, tx, ty, tz, f, f1, f2);
    	}
	}
	
    @Override
	public int getMaxItemUseDuration(ItemStack par1ItemStack)
    {
        return 72000;
    }
    
    @Override
	public EnumAction getItemUseAction(ItemStack par1ItemStack)
    {
        return EnumAction.BOW;
    }

    @Override
	public EnumRarity getRarity(ItemStack par1ItemStack) {
    	return EnumRarity.RARE;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List<String> par3List, boolean par4) {
		super.addInformation(par1ItemStack, par2EntityPlayer, par3List, par4);
		par3List.add((par1ItemStack.getMaxDamage() -  par1ItemStack.getItemDamage()) + " charges left");
	}
}
