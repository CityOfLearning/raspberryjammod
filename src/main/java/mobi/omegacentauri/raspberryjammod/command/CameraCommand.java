package mobi.omegacentauri.raspberryjammod.command;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import mobi.omegacentauri.raspberryjammod.RaspberryJamMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

public class CameraCommand implements ICommand {
	static public void setField(Class c, String field, Object object, Object value) {
		try {
			Field f = c.getDeclaredField(field);
			f.setAccessible(true);
			f.set(object, value);
		} catch (Exception e) {
			RaspberryJamMod.logger.error("" + e);
		}
	}

	public CameraCommand() {
	}

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {

		if (args.length == 1) {
			List<String> options = new ArrayList<>();
			if ("distance".startsWith(args[0])) {
				options.add("distance");
			}
			if ("debug".startsWith(args[0])) {
				options.add("debug");
			}
			return options;
		} else if ((args.length == 2) && args[0].equals("debug")) {
			List<String> options = new ArrayList<>();
			options.add("on");
			options.add("off");
			options.add("toggle");
			return options;
		}
		return null;
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return true;
	}

	@Override
	public int compareTo(ICommand o) {
		return 0;
	}

	@Override
	public List<String> getCommandAliases() {
		List<String> aliases = new ArrayList<>();
		aliases.add(getCommandName());
		return aliases;
	}

	@Override
	public String getCommandName() {
		return "camera";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "camera debug [on|off]\ncamera distance length";
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return false;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		Minecraft mc = Minecraft.getMinecraft();

		if ((args.length >= 1) && args[0].equals("debug")) {
			if ((args.length == 1) || args[1].equals("toggle")) {
				mc.gameSettings.debugCamEnable = !mc.gameSettings.debugCamEnable;
			} else if (args.length == 2) {
				mc.gameSettings.debugCamEnable = args[1].equals("on") || args[1].equals("1");
			} else {
				usage(sender);
			}
		} else if ((args.length >= 2) && args[0].equals("distance")) {
			try {
				setThirdPersonDistance(Float.parseFloat(args[1]));
			} catch (NumberFormatException e) {
			}
		} else {
			usage(sender);
		}
	}

	private void setThirdPersonDistance(float x) {
		Class c = net.minecraft.client.renderer.EntityRenderer.class;
		EntityRenderer r = Minecraft.getMinecraft().entityRenderer;
		setField(c, "thirdPersonDistance", r, x);
		setField(c, "thirdPersonDistanceTemp", r, x);
		setField(c, "field_78490_B", r, x);
		setField(c, "field_78491_C", r, x);
	}

	public void usage(ICommandSender sender) {
		Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(getCommandUsage(sender)));
	}
}
