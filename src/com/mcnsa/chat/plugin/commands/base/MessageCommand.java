package com.mcnsa.chat.plugin.commands.base;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.chat.plugin.abstracts.AbstractChatCommand;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.plugin.utils.MessageSender;
import com.mcnsa.chat.type.ChatPlayer;

public class MessageCommand extends AbstractChatCommand {

    public MessageCommand() {
	super("msg", "Sends a message to a player", "none", false, "msg", "message", "whisper", "tell", "w");
    }

    @Override
    public boolean run(CommandSender sender, String[] args) {
	if (args.length < 2) {
	    usage(sender);
	    return false;
	}
	String player = args[0];
	String[] messageArray = Arrays.copyOfRange(args, 1, args.length);
	StringBuffer messageString = new StringBuffer();
	for (String message : messageArray) {
	    messageString.append(message + " ");
	}
	String message = messageString.toString();
	if (sender instanceof Player) {
	    ChatPlayer playerSender = PlayerManager.getPlayer(sender);
	    if (playerSender.modes.get("MUTE")) {
		MessageSender.send("&c You are in timeout. Please try again later", playerSender);
		return true;
	    }
	    if (player.equalsIgnoreCase("console")) {
		if (!playerSender.modes.get("S-MUTE"))
		    MessageSender.sendToConsole(message);
		MessageSender.sendPMConsole(messageString.toString(), sender);
		return true;
	    }
	    ChatPlayer target = PlayerManager.searchPlayer(player);
	    if (target == null) {
		MessageSender.send("Could not find player: " + player, sender);
		return true;
	    }
	    if (target.equals(playerSender)) {
		MessageSender.recievePM(messageString.toString(), sender, target);
		MessageSender.sendPM(messageString.toString(), sender, target);
		return true;
	    }

	    MessageSender.sendPM(messageString.toString(), sender, target);
	    if (!(playerSender.modes.get("S-MUTE") || target.modes.get("S-MUTE"))) {
		MessageSender.recievePM(messageString.toString(), sender, target);
	    }
	    return true;

	} else {
	    if (player.equalsIgnoreCase("console")) {
		MessageSender.sendPMConsoleConsole(message);
		MessageSender.receivePMConsoleConsole(message);
		return true;
	    }
	    ChatPlayer target = PlayerManager.searchPlayer(player);
	    if (target == null) {
		MessageSender.sendToConsole("Could not find player: " + player);
		return true;
	    }
	    MessageSender.sendPMConsole(messageString.toString(), target);
	    if (!target.modes.get("S-MUTE")) {
		MessageSender.receivePMConsole(messageString.toString(), target);
	    }
	    return true;

	}

    }

}
