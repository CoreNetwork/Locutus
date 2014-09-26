package com.mcnsa.chat.plugin.commands.channel;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;

import com.mcnsa.chat.plugin.abstracts.AbstractChatCommand;
import com.mcnsa.chat.plugin.managers.ChannelManager;
import com.mcnsa.chat.plugin.managers.PermissionManager;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.plugin.utils.Colors;
import com.mcnsa.chat.plugin.utils.MessageSender;
import com.mcnsa.chat.type.ChatChannel;
import com.mcnsa.chat.type.ChatPlayer;

public class MoveCommand extends AbstractChatCommand {

    public MoveCommand() {
	super("move", "Move a player to a channel", "None", false, "move", new String[0]);
    }

    @Override
    public boolean run(CommandSender sender, String[] args) {
	if (args.length < 2)
	    return false;
	String channel = args[0];
	for (int i = 1; i < args.length; i++) {
	    movePlayer(channel, args[i], sender);
	}
	return true;

    }

    private boolean movePlayer(String channel, String player, CommandSender sender) {
	ArrayList<ChatPlayer> target = PlayerManager.searchPlayers(player);
	if (target.isEmpty()) {
	    MessageSender.send("&4Could not find player.", sender);
	    return true;
	}

	ChatPlayer targetPlayer = target.get(0);

	// see if its a persist channel
	if (ChannelManager.getChannel(channel) != null) {
	    ChatChannel chan = ChannelManager.getChannel(channel);

	    if (!PermissionManager.checkPermission(chan.readPermission, targetPlayer)) {
		MessageSender.send("&4" + targetPlayer.getName() + " does not have the required permissions to enter " + channel, sender);
		return true;
	    }
	    channel = chan.name;
	}
	// Get players in channel
	String playersInChannel = ChannelManager.playersInChannel(channel);
	// We can say this player has the permissions. Lets welcome them
	targetPlayer.changeChannel(channel);
	MessageSender.send(Colors.color("&6You have been moved to " + channel + ". Players here: " + playersInChannel), targetPlayer);

	// Notify the mod
	MessageSender.send("&6" + targetPlayer.getName() + " has been moved into " + channel, sender);
	return true;
    }

}
