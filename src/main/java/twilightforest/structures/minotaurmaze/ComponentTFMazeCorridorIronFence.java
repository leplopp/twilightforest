package twilightforest.structures.minotaurmaze;

import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import twilightforest.block.BlockTFMazestone;
import twilightforest.block.TFBlocks;
import twilightforest.block.enums.MazestoneVariant;

public class ComponentTFMazeCorridorIronFence extends ComponentTFMazeCorridor {

	public ComponentTFMazeCorridorIronFence() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ComponentTFMazeCorridorIronFence(int i, int x, int y, int z, EnumFacing rotation) {
		super(i, x, y, z, rotation);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, StructureBoundingBox sbb) {		
		this.fillWithBlocks(world, sbb, 1, 4, 2, 4, 4, 3, TFBlocks.mazestone.getDefaultState().withProperty(BlockTFMazestone.VARIANT, MazestoneVariant.DECORATIVE), AIR, false);
		this.fillWithBlocks(world, sbb, 1, 1, 2, 4, 3, 3, TFBlocks.mazestone.getDefaultState().withProperty(BlockTFMazestone.VARIANT, MazestoneVariant.CHISELED), AIR, false);
		this.fillWithBlocks(world, sbb, 2, 1, 2, 3, 3, 3, Blocks.IRON_BARS.getDefaultState(), Blocks.AIR.getDefaultState(), false);
		return true;
	}
}
