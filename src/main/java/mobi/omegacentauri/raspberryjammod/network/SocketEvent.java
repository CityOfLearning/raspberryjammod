package mobi.omegacentauri.raspberryjammod.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Event;

public class SocketEvent extends Event{
	
	private final EntityPlayer player;
	
	public SocketEvent(EntityPlayer player) {
		this.player = player;
	}

	public EntityPlayer getPlayer() {
		return player;
	}
	
	public static class Close extends SocketEvent {
		public Close(EntityPlayer player) {
			super(player);
		}
	}
}
