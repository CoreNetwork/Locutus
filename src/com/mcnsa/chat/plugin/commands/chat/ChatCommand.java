package com.mcnsa.chat.plugin.commands.chat;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;

import com.mcnsa.chat.plugin.abstracts.AbstractChatCommand;
import com.mcnsa.chat.plugin.abstracts.AbstractSubCommand;

public class ChatCommand extends AbstractSubCommand {

    public ChatCommand() {
	super("chat", "Chat related commands", "none", false, "chat");
	this.commands = new ArrayList<AbstractChatCommand>();
	this.commands.add(new LockdownCommand());
	this.commands.add(new NetworkCommand());
	this.commands.add(new ReloadCommand());
    }

    @Override
    public boolean selfrun(CommandSender sender, String[] args) {
	usage(sender);
	return false;
    }

}
