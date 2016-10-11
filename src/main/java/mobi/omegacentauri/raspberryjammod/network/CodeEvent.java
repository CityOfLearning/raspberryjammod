package mobi.omegacentauri.raspberryjammod.network;

import net.minecraftforge.fml.common.eventhandler.Event;

public class CodeEvent extends Event {

	public static class ErrorEvent extends CodeEvent {
		private final String code;
		private final String error;
		private final int line;

		public ErrorEvent(String code, String error, int line) {
			this.code = code;
			this.error = error;
			this.line = line;
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

}
