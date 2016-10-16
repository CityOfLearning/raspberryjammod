package mobi.omegacentauri.raspberryjammod.events;

import mobi.omegacentauri.raspberryjammod.api.APIHandler;
import mobi.omegacentauri.raspberryjammod.api.APIRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MCEventHandlerClientOnly extends MCEventHandler {
	protected World[] worlds = { null };

	public MCEventHandlerClientOnly() {
		super();
		doRemote = true;
	}

	@Override
	public World[] getWorlds() {
		worlds[0] = Minecraft.getMinecraft().theWorld;
		return worlds;
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onChatEvent(ClientChatReceivedEvent event) {
		APIRegistry.Python2MinecraftApi.ChatDescription cd = new APIRegistry.Python2MinecraftApi.ChatDescription(
				Minecraft.getMinecraft().thePlayer.getEntityId(), event.message.toString());
		for (APIHandler apiHandler : apiHandlers) {
			apiHandler.addChatDescription(cd);
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onClientTick(TickEvent.ClientTickEvent event) {
		runQueue();
	}
}
