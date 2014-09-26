package com.mcnsa.chat.plugin.commands.base;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.chat.plugin.MCNSAChat;
import com.mcnsa.chat.plugin.abstracts.AbstractChatCommand;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.plugin.utils.ConsoleLogging;
import com.mcnsa.chat.plugin.utils.MessageSender;
import com.mcnsa.chat.type.ChatPlayer;

public class MeCommand extends AbstractChatCommand {

    public MeCommand() {
	super("me", "Does that thing", "none", true, "me", "action");
    }

    @Override
    public boolean run(CommandSender sender, String[] args) {
	ConsoleLogging.debug("done me");
	Player p = (Player) sender;
	// Build the chat message
	ChatPlayer player = PlayerManager.getPlayer(p);
	StringBuffer messageString = new StringBuffer();
	for (String message : args) {
	    messageString.append(message + " ");
	}
	if (player.modes.get("MUTE")) {
	    MessageSender.send("&c You are in timeout. Please try again later", player);
	} else {
	    MessageSender.actionMessage(player, messageString.toString(), MCNSAChat.shortCode, PlayerManager.getPlayer(p, MCNSAChat.shortCode).channel);
	}
	return true;
    }

}
