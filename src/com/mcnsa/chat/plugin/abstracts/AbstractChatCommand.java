package com.mcnsa.chat.plugin.abstracts;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.chat.plugin.managers.PermissionManager;
import com.mcnsa.chat.plugin.utils.ConsoleLogging;
import com.mcnsa.chat.plugin.utils.MessageSender;

public abstract class AbstractChatCommand {
    public String name;
    public String desc;
    public Boolean needPlayer;
    public String usage;
    public List<String> aliases;

    public String permissionNode = "mcnsachat";
    public String permission;
    public Command command;

    public AbstractChatCommand(String name, String desc, String usage, boolean needPlayer, String permission, String... aliases) {
	this.name = name;
	this.desc = desc;
	this.usage = usage;
	this.needPlayer = needPlayer;
	this.aliases = Arrays.asList(aliases);
	this.permission = permission;

    }

    public abstract boolean run(CommandSender sender, String[] args);

    public boolean execute(CommandSender sender, String[] args) {
	if (!(sender instanceof Player) && needPlayer) {
	    MessageSender.send("Sorry, but you need to execute this command as player.", sender);
	    return true;
	}

	if (sender instanceof Player && !PermissionManager.checkPermission(permissionNode + "." + permission, sender)) {
	    ConsoleLogging.debug(permissionNode + permission);
	    MessageSender.send("No permission!", sender);
	    return true;
	}
	ConsoleLogging.debug("Executing command: " + name);
	return run(sender, args);
    }

    public void usage(CommandSender sender) {
	sender.sendMessage(usage);
    }

}
