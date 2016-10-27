package mobi.omegacentauri.raspberryjammod.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import mobi.omegacentauri.raspberryjammod.RaspberryJamMod;
import mobi.omegacentauri.raspberryjammod.api.APIRegistry.Python2MinecraftApi.ChatDescription;
import mobi.omegacentauri.raspberryjammod.events.MCEventHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class APIHandler {

	static protected String getRest(Scanner scan) {
		StringBuilder out = new StringBuilder();

		while (scan.hasNext()) {
			if (out.length() > 0) {
				out.append(",");
			}
			out.append(scan.next());
		}
		return out.toString();
	}

	public static void globalMessage(String message) {
		for (World w : MinecraftServer.getServer().worldServers) {
			for (EntityPlayer p : w.playerEntities) {
				p.addChatComponentMessage(new ChatComponentText(message));
			}
		}
	}

	protected MCEventHandler eventHandler;

	public APIHandler(MCEventHandler eventHandler, PrintWriter writer) throws IOException {
		this.eventHandler = eventHandler;
		APIRegistry.Python2MinecraftApi.setWriter(writer);
		eventHandler.registerAPIHandler(this);
	}

	public void addChatDescription(ChatDescription cd) {
		APIRegistry.Python2MinecraftApi.addChatDescription(cd);
	}

	public void close() {
		eventHandler.unregisterAPIHandler(this);
	}

	protected void fail(String string) {
		System.err.println("Error: " + string);
		APIRegistry.Python2MinecraftApi.sendLine("Fail");
	}

	public PrintWriter getWriter() {
		return APIRegistry.Python2MinecraftApi.getWriter();
	}

	public void onClick(PlayerInteractEvent event) {
		APIRegistry.Python2MinecraftApi.onClick(event, eventHandler);
	}

	public void process(String clientSentence) {
		if (!APIRegistry.Python2MinecraftApi.refresh()) {
			return;
		}

		Scanner scan = null;

		try {
			int paren = clientSentence.indexOf('(');
			if (paren < 0) {
				return;
			}

			String cmd = clientSentence.substring(0, paren);
			String args = clientSentence.substring(paren + 1).replaceAll("[\\s\r\n]+$", "").replaceAll("\\)$", "");
			
			if (cmd.startsWith("player.")) {
				// Compatibility with the mcpi library included with Juice
				if (args.startsWith("None,")) {
					args = args.substring(5);
				} else if (args.equals("None")) {
					args = "";
				}
			}

			scan = new Scanner(args);
			scan.useDelimiter(",");

			synchronized (eventHandler) {
				runCommand(cmd, args, scan);
			}

			scan.close();
			scan = null;
		} catch (Exception e) {
			RaspberryJamMod.logger.error("" + e);
			e.printStackTrace();
		} finally {
			if (scan != null) {
				scan.close();
			}
		}
	}

	protected void runCommand(String cmd, String args, Scanner scan)
			throws InputMismatchException, NoSuchElementException, IndexOutOfBoundsException {

		if (!APIRegistry.runCommand(cmd, args, scan, eventHandler)) {
			unknownCommand();
		}
	}

	protected void unknownCommand() {
		fail("unknown command");
	}

}
