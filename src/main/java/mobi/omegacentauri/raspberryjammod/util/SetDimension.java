package mobi.omegacentauri.raspberryjammod.util;

import mobi.omegacentauri.raspberryjammod.actions.ServerAction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;

public class SetDimension extends ServerAction {
	Entity entity;
	int dimension;

	public SetDimension(Entity e, int dim) {
		entity = e;
		dimension = dim;
	}

	@Override
	public void execute() {
		if (null == MinecraftServer.getServer().worldServerForDimension(dimension)) {
			return;
		}
		entity.travelToDimension(dimension);
	}

	@Override
	public IBlockState getBlockState() {
		// TODO Auto-generated method stub
		return null;
	}
}
