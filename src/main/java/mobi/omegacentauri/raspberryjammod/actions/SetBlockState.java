package mobi.omegacentauri.raspberryjammod.actions;

import mobi.omegacentauri.raspberryjammod.util.BlockState;
import mobi.omegacentauri.raspberryjammod.util.Location;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;

public class SetBlockState extends ServerAction {
	Location pos;
	short id;
	short meta;

	public SetBlockState(Location pos, short id, short meta) {
		this.pos = pos;
		this.id = id;
		// this.meta = meta;
	}

	public SetBlockState(short id, short meta) {
		this.id = id;
		this.meta = meta;
	}

	@Override
	public boolean contains(World w, int x, int y, int z) {
		return (pos.getWorld() == w) && (x == pos.getX()) && (y == pos.getY()) && (z == pos.getZ());
	}

	@Override
	public String describe() {
		return "" + id + "," + meta + ",";
	}

	@Override
	public void execute() {
		IBlockState oldState = pos.getWorld().getBlockState(pos);
		Block oldBlock = oldState.getBlock();

		if (null != pos.getWorld().getTileEntity(pos)) {
			pos.getWorld().removeTileEntity(pos);
		}

		if ((Block.getIdFromBlock(oldBlock) != id) || (oldBlock.getMetaFromState(oldState) != meta)) {
			pos.getWorld().setBlockState(pos, safeGetStateFromMeta(Block.getBlockById(id), meta), 3);
			// Maybe the update code should be 2? I don't really know.
		}
	}

	@Override
	public int getBlockId() {
		return id;
	}

	@Override
	public int getBlockMeta() {
		return meta;
	}

	@Override
	public BlockState getBlockState() {
		return new BlockState(id, meta);
	}

	public IBlockState safeGetStateFromMeta(Block b, int meta) {
		try {
			return b.getStateFromMeta(meta);
		} catch (Exception e) {
			return b.getStateFromMeta(0);
		}
	}
}
