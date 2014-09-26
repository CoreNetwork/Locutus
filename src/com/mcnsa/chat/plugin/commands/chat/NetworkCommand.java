package com.mcnsa.chat.plugin.commands.chat;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;

import com.mcnsa.chat.plugin.abstracts.AbstractChatCommand;
import com.mcnsa.chat.plugin.abstracts.AbstractSubCommand;
import com.mcnsa.chat.plugin.commands.chat.network.NetworkOffCommand;
import com.mcnsa.chat.plugin.commands.chat.network.NetworkOnCommand;
import com.mcnsa.chat.plugin.commands.chat.network.NetworkResetCommand;

public class NetworkCommand extends AbstractSubCommand {

    public NetworkCommand() {
	super("network", "Network related commands", "none", false, "network");
	commands = new ArrayList<AbstractChatCommand>();
	commands.add(new NetworkOnCommand());
	commands.add(new NetworkOffCommand());
	commands.add(new NetworkResetCommand());
    }

    @Override
    public boolean selfrun(CommandSender sender, String[] args) {
	// TODO Auto-generated method stub
	return false;
    }

}
