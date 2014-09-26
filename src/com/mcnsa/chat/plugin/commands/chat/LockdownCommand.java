package com.mcnsa.chat.plugin.commands.chat;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;

import com.mcnsa.chat.plugin.MCNSAChat;
import com.mcnsa.chat.plugin.abstracts.AbstractChatCommand;
import com.mcnsa.chat.plugin.abstracts.AbstractSubCommand;
import com.mcnsa.chat.plugin.commands.chat.lockdown.LockdownOffCommand;
import com.mcnsa.chat.plugin.commands.chat.lockdown.LockdownOnCommand;
import com.mcnsa.chat.plugin.utils.MessageSender;

public class LockdownCommand extends AbstractSubCommand {

    public LockdownCommand() {
	super("lockdown", "Locks down the server for a specified time", "none", false, "lockdown");
	commands = new ArrayList<AbstractChatCommand>();
	commands.add(new LockdownOnCommand());
	commands.add(new LockdownOffCommand());
    }

    // @Command(command = "lockdown", description =
    // "Locks down the server, not letting any new players enter", permissions =
    // { "lockdown.enable" }, arguments = { "reason" })
    public static boolean lockdown(CommandSender sender, long time) {
	if (!MCNSAChat.isLockdown) {
	    // lockdownTimed(sender, time);

	} else {
	    String message = MCNSAChat.plugin.getConfig().getString("strings.lockdown-failed");
	    message = message.replace("%reason%", "Server already in temporary lockdown");
	    MessageSender.send(message, sender);
	}
	return true;
    }

    @Override
    public boolean selfrun(CommandSender sender, String[] args) {
	if (args.length == 0) {
	    if (MCNSAChat.isLockdown) {
		sender.sendMessage("The server is currently in lockdown");
	    } else {
		sender.sendMessage("The server is not currently in lockdown");
	    }
	    return true;
	} else if (args.length == 1) {
	    lockdown(sender, Long.parseLong(args[0]));
	    return true;
	}
	return false;
    }
}
