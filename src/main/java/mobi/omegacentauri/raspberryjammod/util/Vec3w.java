package mobi.omegacentauri.raspberryjammod.util;

import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class Vec3w extends Vec3 {
	private World world;

	public Vec3w(World w, double x, double y, double z) {
		super(x, y, z);
		setWorld(w);
	}

	public World getWorld() {
		return world;
	}

	public void setWorld(World world) {
		this.world = world;
	}
}
