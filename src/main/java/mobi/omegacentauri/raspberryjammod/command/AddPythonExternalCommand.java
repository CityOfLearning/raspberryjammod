package mobi.omegacentauri.raspberryjammod.command;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.ICommandSender;

public class AddPythonExternalCommand extends PythonExternalCommand {

	public AddPythonExternalCommand(boolean clientSide) {
		super(clientSide);
	}

	@Override
	public boolean addMode() {
		return true;
	}

	@Override
	public List getCommandAliases() {
		List<String> aliases = new ArrayList<>();
		aliases.add(getCommandName());
		aliases.add("apy");
		return aliases;
	}

	@Override
	public String getCommandName() {
		return "addpython";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "addpython script arguments: run a new script without stopping old one(s)";
	}
}
