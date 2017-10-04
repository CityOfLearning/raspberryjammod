package mobi.omegacentauri.raspberryjammod.events;

import mobi.omegacentauri.raspberryjammod.api.APIHandler;
import mobi.omegacentauri.raspberryjammod.api.APIRegistry;
import mobi.omegacentauri.raspberryjammod.api.Python2MinecraftApi;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class MCEventHandlerServer extends MCEventHandler {
	public MCEventHandlerServer() {
		super();
		doRemote = false;
	}

	@Override
	public World[] getWorlds() {
		return MinecraftServer.getServer().worldServers;
	}

	@SubscribeEvent
	public void onChatEvent(ServerChatEvent event) {
		Python2MinecraftApi.ChatDescription cd = new Python2MinecraftApi.ChatDescription(
				event.player.getEntityId(), event.message);

		for (APIHandler apiHandler : apiHandlers) {
			apiHandler.addChatDescription(cd);
		}
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		runQueue();
	}
}
