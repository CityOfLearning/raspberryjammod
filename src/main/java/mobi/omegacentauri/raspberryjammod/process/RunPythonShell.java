package mobi.omegacentauri.raspberryjammod.process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import mobi.omegacentauri.raspberryjammod.RaspberryJamMod;
import mobi.omegacentauri.raspberryjammod.api.APIHandler;
import mobi.omegacentauri.raspberryjammod.network.NetworkHandler;
import mobi.omegacentauri.raspberryjammod.network.message.RawErrorMessage;
import mobi.omegacentauri.raspberryjammod.util.PathUtility;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class RunPythonShell {

	private static String scriptProcessorPath;
	private static Process runningScript;

	private static String lineNum = "";

	private static String codeLine = "";

	private static void globalMessage(String msg) {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(msg));
		} else {
			APIHandler.globalMessage(msg);
		}
	}

	private static void gobble(final InputStream stream, final EntityPlayer entity, final String label) {
		Thread t = new Thread() {

			@Override
			public void run() {
				BufferedReader br;

				br = new BufferedReader(new InputStreamReader(stream));

				String line;
				try {
					while (null != (line = br.readLine())) {
						line = line.replaceAll(Pattern.quote(">>>"), "");
						line = line.replaceAll(Pattern.quote("..."), "");
						line = line.trim();
						if (!line.contains("copyright") && !line.contains("Python") && !line.isEmpty()) {
							if (entity == null) {
								globalMessage(label + line);
							} else {
								entity.addChatComponentMessage(new ChatComponentText(label + line));
							}
						}
					}
				} catch (IOException e) {
				}

				try {
					br.close();
				} catch (IOException e) {
				}
			}
		};
		t.setDaemon(true);
		t.start();
	}

	private static void gobbleError(final InputStream stream, final EntityPlayer entity, final String label) {
		Thread t = new Thread() {

			@Override
			public void run() {
				BufferedReader br;

				br = new BufferedReader(new InputStreamReader(stream));

				String line;
				try {
					while (null != (line = br.readLine())) {
						if (!line.contains("copyright") && !line.contains("Python")) {
							if (line.contains(">>>")) {
								lineNum = line;
							} else {
								// stop the script from executing
								if (line.contains("Error:")) {
									try {
										runningScript.exitValue();
									} catch (IllegalThreadStateException e) {
										// script was still running
										runningScript.destroy();
									}
									// send the error to the player and then
									// determine the problem client side
									NetworkHandler.channel.sendTo(new RawErrorMessage(codeLine, line, lineNum),
											(EntityPlayerMP) entity);
									lineNum = "";
									codeLine = "";
									// only report the first error
									break;
								} else if (!line.contains("<stdin>") && !line.trim().equals("^")) {
									codeLine = line;
								}
							}
						}
					}
				} catch (IOException e) {
				}

				try {
					br.close();
				} catch (IOException e) {
				}
			}
		};
		t.setDaemon(true);
		t.start();
	}

	private static boolean isProcessAlive(Process proc) {
		try {
			proc.exitValue();
			return false;
		} catch (Exception e) {
			return true;
		}

	}

	public static void run(List<String> program, EntityPlayer player) {
		try {
			String path = PathUtility.getPythonExecutablePath();
			if (path.contains("/") || path.contains(System.getProperty("file.separator"))) {
				scriptProcessorPath = new File(path).getAbsolutePath().toString();
			} else {
				scriptProcessorPath = path;
			}

			if ((runningScript != null) && isProcessAlive(runningScript)) {
				runningScript.destroy();
			}

			List<String> cmd = new ArrayList<String>();
			cmd.add(scriptProcessorPath);
			cmd.add("-i");

			ProcessBuilder builder = new ProcessBuilder(scriptProcessorPath);

			builder.directory(new File("mcpipy/"));

			Map<String, String> environment = builder.environment();
			environment.put("MINECRAFT_PLAYER_NAME", player.getName());
			environment.put("MINECRAFT_PLAYER_ID", "" + player.getEntityId());
			environment.put("MINECRAFT_API_PORT", "" + RaspberryJamMod.currentPortNumber);

			builder.command(cmd);

			runningScript = builder.start();

			// we dont have to worry about checking if the script is alive since
			// it gets destroyed earlier
			if (RaspberryJamMod.playerProcesses.containsKey(player)) {
				RaspberryJamMod.playerProcesses.replace(player, runningScript);
			} else {
				RaspberryJamMod.playerProcesses.put(player, runningScript);
			}

			gobble(runningScript.getInputStream(), player, "");
			gobbleError(runningScript.getErrorStream(), player, "[ERR] ");

			OutputStream in = runningScript.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(in));

			boolean codeBlock = false;
			int blockDepth = 0;
			for (String lines : program) {
				// if (!isProcessAlive(runningScript)) {
				// writer.flush();
				// break;
				// }
				if (codeBlock && ((lines.split(Pattern.quote("    ")).length - 1) < blockDepth)) {
					for (int i = blockDepth - lines.split(Pattern.quote("    ")).length - 1; i > 0; i--) {
						writer.newLine();
					}
					blockDepth = lines.split(Pattern.quote("    ")).length - 1;
					if (blockDepth == 0) {
						codeBlock = false;
					}
				}
				writer.write(lines);
				if (lines.contains("    ")) {
					blockDepth = lines.split(Pattern.quote("    ")).length - 1;
					codeBlock = true;
				}
				writer.newLine();
			}
			if (codeBlock) {
				for (int i = blockDepth; i > 0; i--) {
					writer.newLine();
				}
			}
			writer.write("exit()");
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
