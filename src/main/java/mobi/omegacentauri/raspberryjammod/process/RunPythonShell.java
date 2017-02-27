package mobi.omegacentauri.raspberryjammod.process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import mobi.omegacentauri.raspberryjammod.RaspberryJamMod;
import mobi.omegacentauri.raspberryjammod.api.APIHandler;
import mobi.omegacentauri.raspberryjammod.network.CodeEvent;
import mobi.omegacentauri.raspberryjammod.util.PathUtility;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class RunPythonShell {

	private static String scriptProcessorPath;
	private static Process runningScript;

	private static String lineNum = "";

	private static String codeLine = "";

	private static boolean isRobot = false;
	private static int robotId = 0;

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
									int lineLoc = 0;
									String tempLine = lineNum;
									int index = lineNum.indexOf(">>>");
									while (index != -1) {
										lineLoc++;
										tempLine = tempLine.substring(index + 1);
										index = tempLine.indexOf(">>>");
									}

									index = lineNum.indexOf("...");
									tempLine = lineNum;
									while (index != -1) {
										lineLoc++;
										tempLine = tempLine.substring(index + 1);
										index = tempLine.indexOf("...");
									}
									// posts error to bus which is handled
									// server side and
									// translated to client
									if (!isRobot) {
										RaspberryJamMod.EVENT_BUS
												.post(new CodeEvent.ErrorEvent(codeLine, line, lineLoc, entity));
									} else {
										RaspberryJamMod.EVENT_BUS.post(new CodeEvent.RobotErrorEvent(codeLine, line,
												lineLoc, entity, robotId));
									}
									lineNum = "";
									codeLine = "";
									//kill the script if it hasnt been already
									if ((runningScript != null) && isProcessAlive(runningScript)) {
										runningScript.destroy();
									}
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
		run(program, player, false, 0);
	}

	public static void run(List<String> program, EntityPlayer player, boolean isRobot, int robotId) {
		try {
			RunPythonShell.robotId = robotId;
			RunPythonShell.isRobot = isRobot;
			String path = PathUtility.getPythonExecutablePath();
			if (path.contains("/") || path.contains(System.getProperty("file.separator"))) {
				scriptProcessorPath = new File(path).getAbsolutePath().toString();
			} else {
				scriptProcessorPath = path;
			}

			if ((runningScript != null) && isProcessAlive(runningScript)) {
				runningScript.destroy();
			}

			ProcessBuilder builder = new ProcessBuilder(scriptProcessorPath, "-i");

			builder.directory(new File("mcpy/"));

			Map<String, String> environment = builder.environment();
			environment.put("MINECRAFT_PLAYER_NAME", player.getName());
			environment.put("MINECRAFT_PLAYER_ID", "" + player.getEntityId());
			environment.put("MINECRAFT_API_PORT", "" + RaspberryJamMod.currentPortNumber);

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
