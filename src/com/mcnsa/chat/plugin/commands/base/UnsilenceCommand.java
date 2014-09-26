package com.mcnsa.chat.plugin.commands.base;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.command.CommandSender;

import com.mcnsa.chat.plugin.abstracts.AbstractChatCommand;
import com.mcnsa.chat.plugin.exceptions.DatabaseException;
import com.mcnsa.chat.plugin.managers.DatabaseManager;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.plugin.utils.MessageSender;
import com.mcnsa.chat.type.ChatPlayer;

public class UnsilenceCommand extends AbstractChatCommand {

    public UnsilenceCommand() {
	super("unsilence", "Unilence a player", "None", false, "unsilence", new String[0]);
    }

    @Override
    public boolean run(CommandSender sender, String[] args) {
	if (args.length < 1)
	    return false;
	for (String target : args) {
	    unsilencePlayer(sender, target);
	}
	return true;
    }

    public void unsilencePlayer(CommandSender sender, String target) {
	ArrayList<ChatPlayer> targetPlayers = PlayerManager.searchPlayers(target);
	if (targetPlayers.isEmpty()) {
	    try {

		ResultSet playerRS = DatabaseManager.accessQuery("SELECT * FROM chat_Players where upper(player) = upper(?)", target);
		if (!playerRS.next()) {
		    MessageSender.send("&cCould not find player", sender);
		    return;
		}
		if (playerRS.getLong("timeouttill") == 0) {
		    MessageSender.send("&cThat player is not in timeout", sender);
		    return;
		}

		ResultSet chatRS = DatabaseManager.accessQuery("SELECT * FROM chat_Modes where upper(playerName) = upper(?) and modeName = ?", target, "S-MUTE");
		if (!chatRS.next()) {
		    MessageSender.send("&cCould not find player", sender);
		    return;
		}
		if (chatRS.getBoolean("modeStatus") == false) {
		    MessageSender.send("&cThat player is not in timeout", sender);
		    return;
		}

		DatabaseManager.updateQuery("UPDATE chat_Players set timeouttill=? WHERE upper(player) = upper(?)", 0, target);
		DatabaseManager.updateQuery("UPDATE chat_Modes set modeStatus=0 WHERE upper(playerName) = upper(?) AND  modeName= ?", "S-MUTE");
		MessageSender.send("&6" + target + " has been removed from timeout", sender);
		return;
	    } catch (DatabaseException e) {
		MessageSender.send("&4A DB Error has occurred", sender);
		e.printStackTrace();
	    } catch (SQLException e) {
		MessageSender.send("&4A DB Error has occurred", sender);
		e.printStackTrace();
	    }
	    return;
	}

	ChatPlayer targetPlayer = targetPlayers.get(0);

	if (!targetPlayer.modes.get("S-MUTE")) {
	    MessageSender.send("&cThat player is not in timeout", sender);
	    return;
	}

	PlayerManager.shadowUnmutePlayer(targetPlayer.getUUID());

	return;
    }

}
