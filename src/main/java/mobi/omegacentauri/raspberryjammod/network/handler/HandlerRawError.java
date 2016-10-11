package mobi.omegacentauri.raspberryjammod.network.handler;

import mobi.omegacentauri.raspberryjammod.RaspberryJamMod;
import mobi.omegacentauri.raspberryjammod.network.CodeEvent;
import mobi.omegacentauri.raspberryjammod.network.message.RawErrorMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class HandlerRawError implements IMessageHandler<RawErrorMessage, IMessage> {
	@Override
	public IMessage onMessage(final RawErrorMessage message, MessageContext ctx) {
		RaspberryJamMod.EVENT_BUS
				.post(new CodeEvent.ErrorEvent(message.getCode(), message.getError(), message.getLine()));
		return null;
	}
}
