package com.mcnsa.chat.plugin.commands.base;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;

import com.mcnsa.chat.plugin.abstracts.AbstractChatCommand;
import com.mcnsa.chat.plugin.abstracts.AbstractSubCommand;
import com.mcnsa.chat.plugin.commands.channel.AliasCommand;
import com.mcnsa.chat.plugin.commands.channel.ColorCommand;
import com.mcnsa.chat.plugin.commands.channel.FindCommand;
import com.mcnsa.chat.plugin.commands.channel.LockCommand;
import com.mcnsa.chat.plugin.commands.channel.MoveCommand;
import com.mcnsa.chat.plugin.commands.channel.RegisterCommand;
import com.mcnsa.chat.plugin.commands.channel.UnregisterCommand;

public class ChannelCommand extends AbstractSubCommand {

    public ChannelCommand() {
	super("channel", "Channel sub-command", "None", false, "channel");
	this.commands = new ArrayList<AbstractChatCommand>();
	this.commands.add(new LockCommand());
	this.commands.add(new MoveCommand());
	this.commands.add(new AliasCommand());
	this.commands.add(new ColorCommand());
	this.commands.add(new RegisterCommand());
	this.commands.add(new UnregisterCommand());
	this.commands.add(new FindCommand());
    }

    @Override
    public boolean selfrun(CommandSender sender, String[] args) {
	usage(sender);
	return false;
    }

}
