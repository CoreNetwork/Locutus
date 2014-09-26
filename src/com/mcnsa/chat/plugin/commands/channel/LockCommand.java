package com.mcnsa.chat.plugin.commands.channel;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;

import com.mcnsa.chat.plugin.abstracts.AbstractChatCommand;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.plugin.utils.ConsoleLogging;
import com.mcnsa.chat.plugin.utils.MessageSender;
import com.mcnsa.chat.type.ChatPlayer;

public class LockCommand extends AbstractChatCommand {

    public LockCommand() {
	super("lock", "Lock a player to a channel", "None", false, "lock", new String[0]);
    }

    @Override
    public boolean run(CommandSender sender, String[] args) {
	ConsoleLogging.info("stuff happened");
	if (args.length < 1 || args.length > 2) {
	    sender.sendMessage(usage);
	    return false;
	}
	String player = args[0];
	// Function locks player in their channel
	// Try and get player
	ArrayList<ChatPlayer> target = PlayerManager.searchPlayers(player);
	if (target.isEmpty()) {
	    MessageSender.send("&4Could not find player.", sender);
	    return true;
	}

	ChatPlayer targetPlayer = target.get(0);

	if (args.length == 2) {
	    targetPlayer.changeChannel(args[1]);
	}
	// Check to see if the player is already locked
	if (!targetPlayer.modes.get("LOCKED")) {
	    // Unlock player
	    targetPlayer.modes.put("LOCKED", true);
	    // Inform player
	    MessageSender.send("&6You have been locked in your channel", targetPlayer);
	    // inform command sender
	    MessageSender.send("&6" + targetPlayer.getName() + " has been locked in channel: " + targetPlayer.channel, sender);
	} else {
	    // lock player
	    targetPlayer.modes.put("LOCKED", false);
	    // Inform player
	    MessageSender.send("&6You can now change channels", targetPlayer);
	    // inform command sender
	    MessageSender.send("&6" + targetPlayer.getName() + " has been unlocked", sender);
	}
	return true;
    }

}
