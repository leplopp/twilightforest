package twilightforest.world;

import java.util.Random;

import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import twilightforest.block.BlockTFLog;
import twilightforest.block.TFBlocks;

public class TFGenCanopyOak extends TFGenCanopyTree {
	
    public TFGenCanopyOak()
    {
        this(false);
    }    
    
    public TFGenCanopyOak(boolean par1)
    {
    	super(par1);
    	this.treeState = TFBlocks.log.getDefaultState();
    	this.branchState = treeState.withProperty(BlockTFLog.LOG_AXIS, BlockLog.EnumAxis.NONE);
		this.leafState = TFBlocks.leaves.getDefaultState().withProperty(BlockLeaves.CHECK_DECAY, false);
		this.rootState = TFBlocks.root.getDefaultState();

    }
    
	@Override
	public boolean generate(World world, Random random, BlockPos pos)
	{
		int treeHeight;
		
		// check if we're on dirt or grass
		Material materialUnder = world.getBlockState(pos.down()).getMaterial();
		if ((materialUnder != Material.GRASS && materialUnder != Material.GROUND) || pos.getY() >= TFWorld.MAXHEIGHT - 12)
		{
			return false;
		}


		// determine a height
		treeHeight = minHeight;
		if (random.nextInt(chanceAddFirstFive) == 0) {
			treeHeight += random.nextInt(5);
			
			if (random.nextInt(chanceAddSecondFive) == 0) {
				treeHeight += random.nextInt(5);
			}
		}
		
		//okay build a tree!  Go up to the height
		buildTrunk(world, pos, treeHeight);

		// make 12 - 20 branches
		int numBranches = 12 + random.nextInt(9);
		float bangle = random.nextFloat();
		for (int b = 0; b < numBranches; b++) {
			float btilt = 0.15F + (random.nextFloat() * 0.35F);
			buildBranch(world, pos, treeHeight - 10 + (b / 2), 5, bangle, btilt, false, random);
			
			bangle += (random.nextFloat() * 0.4F);
			if (bangle > 1.0F) {
				bangle -= 1.0F;
			}
		}
		
		makeRoots(world, random, pos);
		makeRoots(world, random, pos.east());
		makeRoots(world, random, pos.south());
		makeRoots(world, random, pos.east().south());
		
		return true;
	}

	private void makeRoots(World world, Random random, BlockPos pos) {
		// root bulb
		if (TFGenerator.hasAirAround(world, pos.down())) {
			this.setBlockAndNotifyAdequately(world, pos.down(), treeState);
		}
		else {
			this.setBlockAndNotifyAdequately(world, pos.down(), rootState);
		}

		// roots!
		int numRoots = 1 + random.nextInt(2);
		float offset = random.nextFloat();
		for (int b = 0; b < numRoots; b++)
		{
			buildRoot(world, pos, offset, b);
		}
	}
    
	private void buildTrunk(World world, BlockPos pos, int treeHeight) {
		for (int dy = 0; dy < treeHeight; dy++) {
			this.setBlockAndNotifyAdequately(world, pos.add(0, dy, 0), treeState);
			this.setBlockAndNotifyAdequately(world, pos.add(1, dy, 0), treeState);
			this.setBlockAndNotifyAdequately(world, pos.add(0, dy, 1), treeState);
			this.setBlockAndNotifyAdequately(world, pos.add(1, dy, 1), treeState);
		}
		
		TFGenerator.drawLeafBlob(this, world, pos.up(treeHeight), 3, leafState);

	}

	/**
	 * Build a branch with a flat blob of leaves at the end.
	 */
	@Override
	void buildBranch(World world, BlockPos pos, int height, double length, double angle, double tilt, boolean trunk, Random treeRNG)
	{
		BlockPos src = pos.up(height);
		BlockPos dest = TFGenerator.translate(src, length, angle, tilt);
		
		// constrain branch spread
		int limit = 5;
		if ((dest.getX() - pos.getX()) < -limit)
		{
			dest = new BlockPos(pos.getX() - limit, dest.getY(), dest.getZ());
		}
		if ((dest.getX() - pos.getX()) > limit)
		{
			dest = new BlockPos(pos.getX() + limit, dest.getY(), dest.getZ());
		}
		if ((dest.getZ() - pos.getZ()) < -limit)
		{
			dest = new BlockPos(dest.getX(), dest.getY(), pos.getZ() - limit);
		}
		if ((dest.getZ() - pos.getZ()) > limit)
		{
			dest = new BlockPos(dest.getX(), dest.getY(), pos.getZ() + limit);
		}
		
		TFGenerator.drawBresehnam(this, world, src, dest, trunk ? treeState : branchState);
	
		// do this here until that bug with the lighting is fixed
		if (trunk) 
		{
			// add a firefly (torch) to the trunk
			addFirefly(world, pos, 3 + treeRNG.nextInt(7), treeRNG.nextDouble());
		}
		
		int blobSize = 2;// + treeRNG.nextInt(2);

		TFGenerator.drawLeafBlob(this, world, dest, blobSize, leafState);

//		makeLeafCircle(world, dest.posX, dest.posY - 1, dest.posZ, 3, leafBlock, leafMeta, true);	
//		makeLeafCircle(world, dest.posX, dest.posY, dest.posZ, 4, leafBlock, leafMeta, true);	
//		makeLeafCircle(world, dest.posX, dest.posY + 1, dest.posZ, 2, leafBlock, leafMeta, true);	
		
		setBlockAndNotifyAdequately(world, dest.east(), branchState);
		setBlockAndNotifyAdequately(world, dest.west(), branchState);
		setBlockAndNotifyAdequately(world, dest.north(), branchState);
		setBlockAndNotifyAdequately(world, dest.south(), branchState);
		
	}
}