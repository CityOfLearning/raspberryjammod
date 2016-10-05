package mobi.omegacentauri.raspberryjammod.command;

import java.util.ArrayList;
import java.util.List;

import mobi.omegacentauri.raspberryjammod.events.ClientEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

public class NightVisionExternalCommand implements ICommand {
	private ClientEventHandler eventHandler;

	public NightVisionExternalCommand(ClientEventHandler eventHandler2) {
		eventHandler = eventHandler2;
	}

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {

		if (args.length == 1) {
			List<String> options = new ArrayList<String>();
			options.add("off");
			options.add("on");
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
		List<String> aliases = new ArrayList<String>();
		aliases.add(getCommandName());
		aliases.add("nv");
		return aliases;
	}

	@Override
	public String getCommandName() {
		return "nightvision";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "nightvision [on|off]";
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return false;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		boolean nv;

		if (args.length == 0) {
			nv = !eventHandler.getNightVision();
		} else if (args[0].toLowerCase().equals("on")) {
			nv = true;
		} else if (args[0].toLowerCase().equals("off")) {
			nv = false;
		} else {
			throw new CommandException("Usage: /nightvision [on|off]");
		}

		eventHandler.setNightVision(nv);
		EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
		if (player != null) {
			if (nv) {
				player.addPotionEffect(new PotionEffect(Potion.nightVision.id, 4096));
				player.addChatComponentMessage(new ChatComponentText("Enabled night vision"));
			} else {
				player.removePotionEffect(Potion.nightVision.id);
				player.addChatComponentMessage(new ChatComponentText("Disabled night vision"));
			}
		}
	}
}
