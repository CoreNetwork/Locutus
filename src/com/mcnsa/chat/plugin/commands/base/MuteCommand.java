package com.mcnsa.chat.plugin.commands.base;

import org.bukkit.command.CommandSender;

import com.mcnsa.chat.plugin.abstracts.AbstractChatCommand;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.plugin.utils.MessageSender;
import com.mcnsa.chat.type.ChatPlayer;

public class MuteCommand extends AbstractChatCommand {

    public MuteCommand() {
	super("mute", "Mutes a given player", "none", true, "mute", "ignore", "cmute");
    }

    @Override
    public boolean run(CommandSender sender, String[] args) {

	for (String arg : args)
	    mutePlayer(sender, arg);
	return true;
    }

    public void mutePlayer(CommandSender sender, String player) {
	// Get the player sending command
	ChatPlayer playerSending = PlayerManager.getPlayer(sender);

	// Try and find the player they are trying to mute
	// TODO support offline
	ChatPlayer mutedPlayer = PlayerManager.searchPlayer(player);
	if (mutedPlayer == null) {
	    MessageSender.send("&6" + player + " could not be found", playerSending);
	    return;
	}
	// See if the player they are trying to mute is already muted
	if (playerSending.hasMuted(mutedPlayer)) {
	    // Let the player know
	    MessageSender.send("&6" + mutedPlayer.getName() + " is already muted", playerSending);
	} else {
	    playerSending.mutePlayer(mutedPlayer);
	    // Let the player know
	    MessageSender.send("&6" + mutedPlayer.getName() + " has been muted", playerSending);
	}

    }

}
