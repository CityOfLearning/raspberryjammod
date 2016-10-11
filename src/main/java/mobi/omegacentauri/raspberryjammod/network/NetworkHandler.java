package mobi.omegacentauri.raspberryjammod.network;

import mobi.omegacentauri.raspberryjammod.RaspberryJamMod;
import mobi.omegacentauri.raspberryjammod.network.handler.HandlerRawError;
import mobi.omegacentauri.raspberryjammod.network.message.RawErrorMessage;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NetworkHandler {

	public static final SimpleNetworkWrapper channel = NetworkRegistry.INSTANCE.newSimpleChannel(RaspberryJamMod.MODID);

	public static void init() {
		channel.registerMessage(HandlerRawError.class, RawErrorMessage.class, 0, Side.CLIENT);
	}

}
