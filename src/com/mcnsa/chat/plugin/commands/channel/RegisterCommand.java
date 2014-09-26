package com.mcnsa.chat.plugin.commands.channel;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.chat.plugin.abstracts.AbstractChatCommand;
import com.mcnsa.chat.plugin.managers.ChannelManager;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.plugin.utils.MessageSender;
import com.mcnsa.chat.type.ChatChannel;

public class RegisterCommand extends AbstractChatCommand {
    public RegisterCommand() {

	super("register", "Registers a specific channel", "usage", false, "register", new String[0]);
    }

    @Override
    public boolean run(CommandSender sender, String[] args) {
	if (args.length > 1)
	    return false;
	String channel;
	if (args.length == 0) {
	    if (sender instanceof Player) {
		channel = PlayerManager.getPlayer(sender).channel;
	    } else {
		return false;
	    }

	} else {
	    channel = args[0];
	}
	channel = channel.substring(0, 1).toUpperCase() + channel.substring(1);
	// Check to make sure the channel isn't already registered.
	if (ChannelManager.getChannel(channel) != null) {
	    MessageSender.send("&cChannel is already registered.", sender);
	    return true;
	}

	// Create the channel
	ChatChannel chatChannel = new ChatChannel(channel);
	// make the channel persistent
	chatChannel.modes.put("PERSIST", true);

	// Let the sender know that its created
	MessageSender.send("&6Channel " + chatChannel + " registered", sender);

	// Add to channel Manager
	ChannelManager.addChannel(chatChannel);
	return true;
    }
}
