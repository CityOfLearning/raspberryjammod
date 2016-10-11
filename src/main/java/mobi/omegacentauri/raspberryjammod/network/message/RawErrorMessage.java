package mobi.omegacentauri.raspberryjammod.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class RawErrorMessage implements IMessage {
	private String code;
	private String error;
	private int line;

	public RawErrorMessage() {
	}

	public RawErrorMessage(String code, String error, String line) {
		this.code = code;
		this.error = error;
		String tempLine = line;
		int index = line.indexOf(">>>");
		while (index != -1) {
			this.line++;
			tempLine = tempLine.substring(index + 1);
			index = tempLine.indexOf(">>>");
		}

		index = line.indexOf("...");
		tempLine = line;
		while (index != -1) {
			this.line++;
			tempLine = tempLine.substring(index + 1);
			index = tempLine.indexOf("...");
		}
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		code = ByteBufUtils.readUTF8String(buf);
		error = ByteBufUtils.readUTF8String(buf);
		line = buf.readInt();
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

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, code);
		ByteBufUtils.writeUTF8String(buf, error);
		buf.writeInt(line);
	}
}
