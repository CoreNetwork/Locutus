package com.mcnsa.chat.plugin.commands.base;

import org.bukkit.command.CommandSender;

import com.mcnsa.chat.plugin.abstracts.AbstractChatCommand;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.plugin.utils.MessageSender;
import com.mcnsa.chat.type.ChatPlayer;

public class UnmuteCommand extends AbstractChatCommand {

    public UnmuteCommand() {
	super("unmute", "Unmute a player", "none", true, "mute", "unsilence", "unignore");
    }

    @Override
    public boolean run(CommandSender sender, String[] args) {
	for (String arg : args) {
	    unmutePlayer(sender, arg);
	}
	return true;
    }

    // @Command(command = "unmute", arguments = { "Player" }, description =
    // "Unmute a player", permissions = { "mute" }, playerOnly = true)
    private void unmutePlayer(CommandSender sender, String mutedPlayerName) {
	// Get the player sending command
	ChatPlayer playerSending = PlayerManager.getPlayer(sender);

	// Try and find the player they are trying to mute
	ChatPlayer mutedPlayer = PlayerManager.searchPlayer(mutedPlayerName);
	if (mutedPlayer == null) {
	    MessageSender.send("&6" + mutedPlayerName + " could not be found", playerSending);
	    return;
	}
	// See if the player they are trying to mute is already muted
	if (playerSending.hasMuted(mutedPlayer)) {
	    // Player is already muted, Un mute them
	    playerSending.unmutePlayer(mutedPlayer);

	    // Let the player know
	    MessageSender.send("&6" + mutedPlayer.getName() + " has been unmuted", playerSending);
	} else {
	    // Let the player know
	    MessageSender.send("&6" + mutedPlayer.getName() + " is not muted", playerSending);
	}

    }
}
