package mobi.omegacentauri.raspberryjammod.actions;

import mobi.omegacentauri.raspberryjammod.RaspberryJamMod;
import mobi.omegacentauri.raspberryjammod.util.Location;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class SetBlocksNBT extends SetBlocksState {
	NBTTagCompound nbt;

	public SetBlocksNBT(Location corner1, Location corner2, short id, short meta, NBTTagCompound nbt) {
		super(corner1, corner2, id, meta);
		this.nbt = nbt;
	}

	@Override
	public boolean contains(World w, int x, int y, int z) {
		return (x <= x2) && (y <= y2) && (z <= z2) && (pos.getX() <= x) && (pos.getY() <= y) && (pos.getZ() <= z)
				&& (w == pos.getWorld());
	}

	@Override
	public String describe() {
		SetBlockNBT.scrubNBT(nbt);
		return super.describe() + nbt.toString();
	}

	@Override
	public void execute() {
		int y1 = pos.getY();
		int z1 = pos.getZ();
		IBlockState state = safeGetStateFromMeta(Block.getBlockById(id), meta);

		for (int x = pos.getX(); x <= x2; x++) {
			for (int y = y1; y <= y2; y++) {
				for (int z = z1; z <= z2; z++) {

					// TODO: fix in client-only mode
					if (!RaspberryJamMod.apiActive) {
						break;
					}

					BlockPos here = new BlockPos(x, y, z);

					pos.getWorld().setBlockState(here, state, 2);

					TileEntity tileEntity = pos.getWorld().getTileEntity(here);
					if (tileEntity != null) {
						nbt.setInteger("x", here.getX());
						nbt.setInteger("y", here.getY());
						nbt.setInteger("z", here.getZ());
						try {
							tileEntity.readFromNBT(nbt);
						} catch (Exception e) {
						}
						tileEntity.markDirty();
					}
				}
			}
		}

	}
}
