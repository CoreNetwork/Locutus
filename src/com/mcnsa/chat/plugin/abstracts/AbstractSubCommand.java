package com.mcnsa.chat.plugin.abstracts;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.mcnsa.chat.plugin.utils.ConsoleLogging;

public abstract class AbstractSubCommand extends AbstractChatCommand {

    public AbstractSubCommand(String name, String desc, String usage, boolean needPlayer, String permission, String... aliases) {
	super(name, desc, usage, needPlayer, permission, aliases);
    }

    public List<AbstractChatCommand> commands;

    @Override
    public boolean run(CommandSender sender, String[] args) {
	if (args.length == 0) {
	    return selfrun(sender, args);
	}
	ConsoleLogging.debug("Have been executed: " + name);
	for (AbstractChatCommand c : this.commands) {
	    if (c.name.equalsIgnoreCase(args[0]) || (c.aliases != null && c.aliases.contains(args[0].toLowerCase()))) {
		ConsoleLogging.debug("Executing: " + c.name);
		return c.execute(sender, Arrays.copyOfRange(args, 1, args.length));
	    }
	    ConsoleLogging.debug("Looking at: " + c.name);
	}
	ConsoleLogging.debug("Nothing worked");
	return false;
    }

    public abstract boolean selfrun(CommandSender sender, String[] args);

}
