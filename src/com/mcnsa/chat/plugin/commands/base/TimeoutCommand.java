package com.mcnsa.chat.plugin.commands.base;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.mcnsa.chat.plugin.MCNSAChat;
import com.mcnsa.chat.plugin.abstracts.AbstractChatCommand;
import com.mcnsa.chat.plugin.exceptions.DatabaseException;
import com.mcnsa.chat.plugin.managers.DatabaseManager;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.plugin.utils.GenUtil;
import com.mcnsa.chat.plugin.utils.MessageSender;
import com.mcnsa.chat.type.ChatPlayer;

public class TimeoutCommand extends AbstractChatCommand {

    public TimeoutCommand() {
	super("timeout", "Timeout a player", "None", false, "timeout", new String[0]);
    }

    // TODO allow infinite time
    @Override
    public boolean run(CommandSender sender, String[] args) {
	if (args.length < 2)
	    return false;
	ArrayList<String> targets = new ArrayList<String>();
	int i = 0;
	while (!GenUtil.isDouble(args[i])) {
	    targets.add(args[i]);
	    i++;
	}

	for (String target : targets) {
	    silencePlayer(sender, target, args[i]);
	}
	return true;
    }

    public void silencePlayer(CommandSender sender, String target, String time) {
	ArrayList<ChatPlayer> targetPlayers = PlayerManager.searchPlayers(target);
	if (targetPlayers.isEmpty()) {
	    try {
		ResultSet playerRS = DatabaseManager.accessQuery("SELECT * FROM chat_Players where upper(player) = upper(?)", target);
		if (!playerRS.next()) {
		    MessageSender.send("&cCould not find player", sender);
		    return;
		}
		if (playerRS.getLong("timeouttill") != 0) {
		    MessageSender.send("&cThat player already in timeout", sender);
		    return;
		}
		ResultSet chatRS = DatabaseManager.accessQuery("SELECT * FROM chat_Modes where upper(playerName) = upper(?)", target);
		if (!chatRS.next()) {
		    MessageSender.send("&cCould not find player", sender);
		    return;
		}
		long timeout = (long) (new Date().getTime() + (Double.valueOf(time) * 60000));
		DatabaseManager.updateQuery("UPDATE chat_Players set timeouttill=? WHERE upper(player) = upper(?)", timeout, target);
		DatabaseManager.updateQuery("UPDATE chat_Modes set modeStatus=1 WHERE upper(playerName) = upper(?) AND  modeName= ?", target, "MUTE");

		MessageSender.send("&6" + target + " has been added to timeout", sender);
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
	if (targetPlayer.modes.get("MUTE")) {
	    MessageSender.send("&cThat player already in timeout", sender);
	    return;
	}

	// Mute and notify
	PlayerManager.shadowMutePlayer(targetPlayer.getUUID(), time, "");

	// Start timer
	final UUID finalUUID = targetPlayer.getUUID();
	long timeleft = (long) (Double.valueOf(time) * 1200);
	Bukkit.getScheduler().scheduleSyncDelayedTask(MCNSAChat.plugin, new Runnable() {
	    public void run() {
		if (PlayerManager.getPlayer(finalUUID, MCNSAChat.shortCode) != null && PlayerManager.getPlayer(finalUUID, MCNSAChat.shortCode).modes.get("MUTE")) {
		    PlayerManager.shadowUnmutePlayer(finalUUID);
		}
	    }
	}, timeleft);

	return;
    }

}
