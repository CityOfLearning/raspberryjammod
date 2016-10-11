package mobi.omegacentauri.raspberryjammod.command;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;

public class PythonExternalCommand extends ScriptExternalCommand {

	public PythonExternalCommand(boolean clientSide) {
		super(clientSide);
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
		aliases.add("py");
		return aliases;
	}

	@Override
	public String getCommandName() {
		return "python";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "python <script> [arguments]: run script, stopping old one(s) (omit script to stop previous script)";
	}

	@Override
	protected String getExtension() {
		return ".py";
	}

	@Override
	protected String[] getScriptPaths() {
		return new String[] { "mcpipy/", "mcpimods/python/" };
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return false;
	}
}
