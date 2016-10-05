package mobi.omegacentauri.raspberryjammod.actions;

import mobi.omegacentauri.raspberryjammod.util.BlockState;
import net.minecraft.world.World;

public abstract class ServerAction {
	public boolean contains(World w, int x, int y, int z) {
		return false;
	}

	public String describe() {
		return "";
	}

	abstract public void execute();

	public int getBlockId() {
		return 0;
	}

	public int getBlockMeta() {
		return 0;
	}

	public BlockState getBlockState() {
		return new BlockState((short) 0, (short) 0);
	}
}
