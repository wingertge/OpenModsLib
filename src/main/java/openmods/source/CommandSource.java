package openmods.source;

import static openmods.utils.CommandUtils.filterPrefixes;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.command.*;
import net.minecraft.util.ChatComponentTranslation;
import openmods.Log;
import openmods.source.ClassSourceCollector.ApiInfo;
import openmods.source.ClassSourceCollector.ClassMeta;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

public class CommandSource implements ICommand {

	private static final String COMMAND_CLASS = "class";

	private final List<String> subcommands = ImmutableList.of(COMMAND_CLASS);

	private final String name;
	private final boolean restricted;
	private final ClassSourceCollector classMeta;

	public CommandSource(String name, boolean restricted, ClassSourceCollector classMeta) {
		this.name = name;
		this.restricted = restricted;
		this.classMeta = classMeta;
	}

	@Override
	public int compareTo(Object o) {
		return name.compareTo(((ICommand)o).getCommandName());
	}

	@Override
	public String getCommandName() {
		return name;
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return name + " class <class name>";
	}

	@Override
	@SuppressWarnings("rawtypes")
	public List getCommandAliases() {
		return null;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (args.length < 1) throw new SyntaxErrorException();

		final String subCommand = args[0];

		if (subCommand.equals(COMMAND_CLASS)) {
			if (args.length < 2) throw new SyntaxErrorException();
			final String clsName = args[1];
			final ClassMeta meta = getMeta(clsName);

			sender.addChatMessage(new ChatComponentTranslation("openmodslib.command.class_source", meta.cls.getName(), meta.source()));

			final ApiInfo api = meta.api;
			if (api != null) {
				sender.addChatMessage(new ChatComponentTranslation("openmodslib.command.api_class",
						api.api, api.owner, api.version));
			}

			for (Map.Entry<File, Set<String>> e : meta.providerMods.entrySet())
				sender.addChatMessage(new ChatComponentTranslation("openmodslib.command.class_provider",
						e.getKey().getAbsolutePath(),
						Joiner.on(',').join(e.getValue()))
						);
		}
	}

	private ClassMeta getMeta(final String clsName) {
		try {
			return classMeta.getClassInfo(clsName);
		} catch (ClassNotFoundException e) {
			throw new CommandException("openmodslib.command.invalid_class_name", clsName);
		} catch (Throwable t) {
			Log.warn(t, "Failed to get information for class %s", clsName);
			throw new CommandException("openmodslib.command.unknown_error_details");
		}
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		int level = restricted? 4 : 0;
		return sender.canCommandSenderUseCommand(level, name);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
		if (args.length == 1) return filterPrefixes(args[0], subcommands);

		return null;
	}

	@Override
	public boolean isUsernameIndex(String[] astring, int i) {
		return false;
	}

}
