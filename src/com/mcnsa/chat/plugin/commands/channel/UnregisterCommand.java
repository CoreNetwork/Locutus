package com.mcnsa.chat.plugin.commands.channel;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.chat.plugin.abstracts.AbstractChatCommand;
import com.mcnsa.chat.plugin.managers.ChannelManager;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.plugin.utils.MessageSender;
import com.mcnsa.chat.type.ChatChannel;

public class UnregisterCommand extends AbstractChatCommand {

    public UnregisterCommand() {
	super("unregister", "Unregister a channel", "None", false, "register", new String[0]);
    }

    @Override
    public boolean run(CommandSender sender, String[] args) {
	if (args.length == 0) {
	    if (sender instanceof Player)
		unregisterChannel(sender, PlayerManager.getPlayer(sender).channel);
	    else
		MessageSender.send("You can only do that as a player!", sender);
	} else {
	    for (String channel : args) {
		unregisterChannel(sender, channel);
	    }
	}
	return true;
    }

    private void unregisterChannel(CommandSender sender, String channel) {
	channel = channel.substring(0, 1).toUpperCase() + channel.substring(1);
	ChatChannel chan = ChannelManager.getChannel(channel);
	if (chan == null) {
	    MessageSender.send("&cChannel is not registered.", sender);
	}

	// Remove from channel Manager
	ChannelManager.removeChannel(chan.name);

	chan.modes.put("PERSIST", false);

	// Inform user
	MessageSender.send("&cChannel: " + channel + " has been removed.", sender);

    }

}
