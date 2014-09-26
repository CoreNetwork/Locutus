package com.mcnsa.chat.plugin.commands.base;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.chat.plugin.MCNSAChat;
import com.mcnsa.chat.plugin.abstracts.AbstractChatCommand;
import com.mcnsa.chat.plugin.managers.ChannelManager;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.plugin.utils.MessageSender;
import com.mcnsa.chat.type.ChatPlayer;

public class ListCommand extends AbstractChatCommand {

    public ListCommand(String name, String desc, String usage, boolean needPlayer, String permission, String... aliases) {
	super("list", "Lists the channels in a world", "none", false, permission, aliases);
	// TODO Auto-generated constructor stub
    }

    @Override
    public boolean run(CommandSender sender, String[] args) {
	// TODO Auto-generated method stub
	return false;
    }

    // @Command(command = "channels", aliases = { "Channels" }, description =
    // "Get a list of channels", permissions = { "list" }, playerOnly = false)
    private void channelList(CommandSender sender) {
	// Get the player
	ChatPlayer player = PlayerManager.getPlayer(sender, MCNSAChat.shortCode);
	ArrayList<String> channels;
	if (sender instanceof Player) {
	    channels = ChannelManager.getChannelList(player);
	} else {
	    channels = ChannelManager.getChannelList();
	}
	// Get the channel list

	StringBuffer message = new StringBuffer();
	for (String channel : channels) {
	    if (message.length() < 1)
		if (ChannelManager.getChannel(channel) != null)
		    message.append(ChannelManager.getChannel(channel).color + channel + "&f");
		else
		    message.append(channel);
	    else if (ChannelManager.getChannel(channel) != null)
		message.append(", " + ChannelManager.getChannel(channel).color + channel + "&f");
	    else
		message.append(", " + channel);
	}
	if (sender instanceof Player) {
	    MessageSender.send("Channels: " + message.toString(), player);
	} else {
	    MessageSender.sendToConsole("Channels: " + message.toString());
	}
    }
}
