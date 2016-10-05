package mobi.omegacentauri.raspberryjammod;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = RaspberryJamMod.MODID, version = RaspberryJamMod.VERSION, name = RaspberryJamMod.NAME, guiFactory = "mobi.omegacentauri.raspberryjammod.GuiFactory", acceptableRemoteVersions = "*", acceptedMinecraftVersions = "[1.8,1.9)")
public class RaspberryJamMod {
	public static final String MODID = "raspberryjammod";
	public static final String VERSION = "0.82";
	public static final String NAME = "Raspberry Jam Mod";
	public static ScriptExternalCommand[] scriptExternalCommands = null;
	public static Configuration configFile;
	static int portNumber = 4711;
	static int wsPort = 14711;
	public static boolean concurrent = true;
	public static boolean leftClickToo = true;
	public static boolean allowRemote = true;
	public static boolean globalChatMessages = true;
	public static String pythonInterpreter = "python";
	public static boolean integrated = true;
	public static volatile boolean apiActive = false;
	static boolean clientOnlyAPI = false;
	static boolean searchForPort = false;
	public static int currentPortNumber;
	public static String serverAddress = null;

	public static int closeAllScripts() {
		if (scriptExternalCommands == null) {
			return 0;
		}
		int count = 0;
		for (ScriptExternalCommand c : scriptExternalCommands) {
			count += c.close();
		}
		return count;
	}

	static public Field findField(Class c, String name) throws NoSuchFieldException {
		do {
			try {
				// for (Field f : c.getDeclaredFields()) {
				// System.out.println(f.getName()+" "+f.getType());
				// }
				return c.getDeclaredField(name);
			} catch (Exception e) {
				// System.out.println(""+e);
			}
		} while (null != (c = c.getSuperclass()));
		throw new NoSuchFieldException(name);
	}

	public static void synchronizeConfig() {
		portNumber = configFile.getInt("Port Number", Configuration.CATEGORY_GENERAL, 4711, 0, 65535, "Port number");
		wsPort = configFile.getInt("Websocket Port", Configuration.CATEGORY_GENERAL, 14711, 0, 65535, "Websocket port");
		searchForPort = configFile.getBoolean("Port Search if Needed", Configuration.CATEGORY_GENERAL, false,
				"Port search if needed");
		concurrent = configFile.getBoolean("Multiple Connections", Configuration.CATEGORY_GENERAL, true,
				"Multiple connections");
		allowRemote = configFile.getBoolean("Remote Connections", Configuration.CATEGORY_GENERAL, true,
				"Remote connections");
		leftClickToo = configFile.getBoolean("Detect Sword Left-Click", Configuration.CATEGORY_GENERAL, false,
				"Detect sword left-click");
		pythonInterpreter = configFile.getString("Python Interpreter", Configuration.CATEGORY_GENERAL, "python",
				"Python interpreter");
		globalChatMessages = configFile.getBoolean("Messages Go To All", Configuration.CATEGORY_GENERAL, true,
				"Messages go to all");
		clientOnlyAPI = configFile.getBoolean("Read-Only Client-Based API", Configuration.CATEGORY_GENERAL, false,
				"Read-only API");
		// clientOnlyPortNumber = configFile.getInt("Port Number for Client-Only
		// API", Configuration.CATEGORY_GENERAL, 0, 0, 65535, "Client-only API
		// port number (normally 0)");

		if (configFile.hasChanged()) {
			configFile.save();
		}
	}

	static public void unregisterCommand(CommandHandler ch, ICommand c) {
		try {
			Map commandMap = ch.getCommands();
			for (String alias : c.getCommandAliases()) {
				try {
					commandMap.remove(alias);
				} catch (Exception e) {
				}
			}

			try {
				commandMap.remove(c.getCommandName());
			} catch (Exception e) {
			}

			Field commandSetField;
			try {
				commandSetField = findField(ch.getClass(), "commandSet");
			} catch (NoSuchFieldException e) {
				commandSetField = findField(ch.getClass(), "field_71561_b");
			}
			commandSetField.setAccessible(true);
			Set commandSet = (Set) commandSetField.get(ch);
			commandSet.remove(c);
		} catch (Exception e) {
			System.err.println("Oops " + e);
		}
	}

	private APIServer fullAPIServer = null;
	private NightVisionExternalCommand nightVisionExternalCommand = null;

	private CameraCommand cameraCommand = null;

	private ClientEventHandler clientEventHandler = null;

	private MCEventHandler serverEventHandler = null;

	private MinecraftServer s;

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void Init(FMLInitializationEvent event) {
		System.out.println("FMLInitializationEvent");
		clientEventHandler = new ClientEventHandler();
		MinecraftForge.EVENT_BUS.register(clientEventHandler);
		nightVisionExternalCommand = new NightVisionExternalCommand(clientEventHandler);
		net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(nightVisionExternalCommand);
		cameraCommand = new CameraCommand();
		net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(cameraCommand);
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void onConfigChanged(net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent event) {
		System.out.println("config changed");
	}

	@EventHandler
	public void onServerStarting(FMLServerStartingEvent event) {
		System.out.println("Server started event");

		synchronizeConfig();

		if (clientOnlyAPI) {
			return;
		}

		if (clientEventHandler != null) {
			clientEventHandler.closeAPI();
		}

		apiActive = true;

		serverEventHandler = new MCEventHandlerServer();
		MinecraftForge.EVENT_BUS.register(serverEventHandler);
		try {
			currentPortNumber = -1;
			fullAPIServer = new APIServer(serverEventHandler, portNumber, searchForPort ? 65535 : portNumber, wsPort,
					false);
			currentPortNumber = fullAPIServer.getPortNumber();

			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						fullAPIServer.communicate();
					} catch (IOException e) {
						System.out.println("RaspberryJamMod error " + e);
					} finally {
						System.out.println("Closing RaspberryJamMod");
						if (fullAPIServer != null) {
							fullAPIServer.close();
						}
					}
				}

			}).start();
		} catch (IOException e1) {
			System.out.println("Threw " + e1);
		}

		scriptExternalCommands = new ScriptExternalCommand[] { new PythonExternalCommand(false),
				new AddPythonExternalCommand(false) };
		for (ScriptExternalCommand c : scriptExternalCommands) {
			event.registerServerCommand(c);
		}
	}

	@EventHandler
	public void onServerStopping(FMLServerStoppingEvent event) {
		if (clientOnlyAPI) {
			return;
		}

		apiActive = false;

		if (serverEventHandler != null) {
			MinecraftForge.EVENT_BUS.unregister(serverEventHandler);
			serverEventHandler = null;
		}

		if (fullAPIServer != null) {
			fullAPIServer.close();
		}
		closeAllScripts();
		if (scriptExternalCommands != null) {
			s = MinecraftServer.getServer();
			if (s != null) {
				for (ICommand c : scriptExternalCommands) {
					unregisterCommand((CommandHandler) s.getCommandManager(), c);
				}
			}
			scriptExternalCommands = null;
		}
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		integrated = true;
		try {
			Class.forName("net.minecraft.client.Minecraft");
		} catch (ClassNotFoundException e) {
			integrated = false;
		}

		configFile = new Configuration(event.getSuggestedConfigurationFile());
		configFile.load();
		System.out.println("configFile = " + configFile.getConfigFile().getPath());
		// KeyBindings.init();

		synchronizeConfig();
	}
}
