package com.mcnsa.chat.plugin.commands.chat.network;

import org.bukkit.command.CommandSender;

import com.mcnsa.chat.plugin.abstracts.AbstractChatCommand;

public class NetworkOffCommand extends AbstractChatCommand {

    public NetworkOffCommand() {
	super("off", "Turns chat network off", "None", false, "off");
	// TODO Auto-generated constructor stub
    }

    @Override
    public boolean run(CommandSender sender, String[] args) {
	// TODO Auto-generated method stub
	return false;
    }

}
