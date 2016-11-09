package mobi.omegacentauri.raspberryjammod.events;

import java.io.IOException;
import java.net.InetSocketAddress;

import mobi.omegacentauri.raspberryjammod.RaspberryJamMod;
import mobi.omegacentauri.raspberryjammod.api.APIServer;
import mobi.omegacentauri.raspberryjammod.command.AddPythonExternalCommand;
import mobi.omegacentauri.raspberryjammod.command.PythonExternalCommand;
import mobi.omegacentauri.raspberryjammod.command.ScriptExternalCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.ICommand;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClientEventHandler {
	private volatile boolean nightVision = false;
	private int clientTickCount = 0;
	private MCEventHandlerClientOnly apiEventHandler = null;
	private APIServer apiServer = null;
	// private boolean registeredCommands = false;

	public void closeAPI() {
		RaspberryJamMod.closeAllScripts();
		if (RaspberryJamMod.scriptExternalCommands != null) {
			for (ICommand c : RaspberryJamMod.scriptExternalCommands) {
				RaspberryJamMod.logger.info("Unregistering " + c.getClass());
				RaspberryJamMod.unregisterCommand(net.minecraftforge.client.ClientCommandHandler.instance, c);
			}
			RaspberryJamMod.scriptExternalCommands = null;
		}
		RaspberryJamMod.apiActive = false;
		if (apiEventHandler != null) {
			MinecraftForge.EVENT_BUS.unregister(apiEventHandler);
			RaspberryJamMod.EVENT_BUS.unregister(apiEventHandler);
			apiEventHandler = null;
		}
		if (apiServer != null) {
			apiServer.close();
			apiServer = null;
		}
	}

	public boolean getNightVision() {
		return nightVision;
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onClientConnectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
		try {
			Object address = event.manager.getRemoteAddress();
			if (address instanceof InetSocketAddress) {
				RaspberryJamMod.serverAddress = ((InetSocketAddress) address).getAddress().getHostAddress();
				RaspberryJamMod.logger.info("Server address " + RaspberryJamMod.serverAddress);
			} else {
				RaspberryJamMod.logger.info("No IP address");
			}
		} catch (Exception e) {
			RaspberryJamMod.serverAddress = null;
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (nightVision && ((clientTickCount % 1024) == 0)) {
			Minecraft mc = Minecraft.getMinecraft();
			if (mc != null) {
				EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;

				if (player != null) {
					player.addPotionEffect(new PotionEffect(Potion.nightVision.id, 4096));
				}
			}
		}
		clientTickCount++;
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onWorldLoaded(WorldEvent.Load event) {
		RaspberryJamMod.synchronizeConfig();

		if (!RaspberryJamMod.clientOnlyAPI) {
			return;
		}

		// if (! registeredCommands) {
		RaspberryJamMod.logger.info("Registering commands");
		RaspberryJamMod.scriptExternalCommands = new ScriptExternalCommand[] { new PythonExternalCommand(true),
				new AddPythonExternalCommand(true) };
		for (ScriptExternalCommand c : RaspberryJamMod.scriptExternalCommands) {
			net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(c);
		}
		// }

		if (apiEventHandler == null) {
			apiEventHandler = new MCEventHandlerClientOnly();
			MinecraftForge.EVENT_BUS.register(apiEventHandler);
			RaspberryJamMod.EVENT_BUS.register(apiEventHandler);
		}

		if (apiServer == null) {
			try {
				RaspberryJamMod.logger.info("RaspberryJamMod client only API");
				RaspberryJamMod.apiActive = true;
				if (apiServer == null) {
					RaspberryJamMod.currentPortNumber = -1;
					apiServer = new APIServer(apiEventHandler, RaspberryJamMod.portNumber,
							RaspberryJamMod.searchForPort ? 65535 : RaspberryJamMod.portNumber, RaspberryJamMod.wsPort,
							true);
					RaspberryJamMod.currentPortNumber = apiServer.getPortNumber();

					new Thread(() -> {
						try {
							apiServer.communicate();
						} catch (IOException e) {
							RaspberryJamMod.logger.error("RaspberryJamMod error " + e);
						} finally {
							closeAPI();
						}
					}).start();
				}
			} catch (Exception e) {
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onWorldUnloaded(WorldEvent.Unload event) {
		if (!RaspberryJamMod.clientOnlyAPI) {
			return;
		}

		closeAPI();
	}

	public void setNightVision(boolean b) {
		nightVision = b;
	}
}
