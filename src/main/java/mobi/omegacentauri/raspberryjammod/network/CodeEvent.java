package mobi.omegacentauri.raspberryjammod.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Event;

public class CodeEvent extends Event {
	
	public static class RobotErrorEvent extends ErrorEvent {

		private final int entityId;
		
		public RobotErrorEvent(String code, String error, int line, EntityPlayer player, int entityId) {
			super(code, error, line, player);
			this.entityId = entityId;
		}

		public int getEntityId() {
			return entityId;
		}
		
	}
	
	public static class ErrorEvent extends CodeEvent {
		private final EntityPlayer player;
		private final String code;
		private final String error;
		private final int line;

		public ErrorEvent(String code, String error, int line, EntityPlayer player) {
			this.code = code;
			this.error = error;
			this.line = line;
			this.player = player;
		}

		public EntityPlayer getPlayer() {
			return player;
		}

		public String getCode() {
			return code;
		}

		public String getError() {
			return error;
		}

		public int getLine() {
			return line;
		}
	}

	public static class FailEvent extends CodeEvent {

		private final EntityPlayer player;
		private final String code;
		private final int id;

		public FailEvent(String code, int id, EntityPlayer player) {
			this.code = code;
			this.id = id;
			this.player = player;
		}

		public EntityPlayer getPlayer() {
			return player;
		}

		public String getCode() {
			return code;
		}

		public int getId() {
			return id;
		}
	}

	public static class SocketCloseEvent extends CodeEvent {

		private final EntityPlayer player;
		
		public SocketCloseEvent(EntityPlayer player) {
			this.player = player;
		}

		public EntityPlayer getPlayer() {
			return player;
		}
	}

	public static class SuccessEvent extends CodeEvent {

		private final EntityPlayer player;
		
		private final String code;
		private final int id;

		public SuccessEvent(String code, int id, EntityPlayer player) {
			this.code = code;
			this.id = id;
			this.player = player;
		}

		public EntityPlayer getPlayer() {
			return player;
		}

		public String getCode() {
			return code;
		}

		public int getId() {
			return id;
		}
	}

}
