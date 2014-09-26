package com.mcnsa.chat.plugin.commands.base;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import org.bukkit.command.CommandSender;

import com.mcnsa.chat.plugin.MCNSAChat;
import com.mcnsa.chat.plugin.abstracts.AbstractChatCommand;
import com.mcnsa.chat.plugin.exceptions.DatabaseException;
import com.mcnsa.chat.plugin.managers.DatabaseManager;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.plugin.utils.ConsoleLogging;
import com.mcnsa.chat.plugin.utils.MessageSender;

public class SeenCommand extends AbstractChatCommand {

    public SeenCommand() {
	super("seen", "Displays the last time the person logged on", "None", false, "seen", new String[0]);
    }

    @Override
    public boolean run(CommandSender sender, String[] args) {
	if (args.length == 0)
	    return false;
	else {
	    for (String name : args) {
		seenPlayer(sender, name);
	    }
	}
	return true;
    }

    private void seenPlayer(CommandSender sender, String playerName) {
	if (PlayerManager.searchPlayer(playerName) != null) {
	    String message = MCNSAChat.plugin.getConfig().getString("strings.seen-online");
	    message = message.replace("%player%", playerName);
	    MessageSender.send(message, sender);
	} else {
	    try {
		ResultSet rs = DatabaseManager.accessQuery("SELECT lastLogin FROM chat_Players WHERE UPPER(player)=UPPER(?)", playerName);
		long last = rs.getLong(1);
		long currentTime = System.currentTimeMillis();
		long time = currentTime - last;
		String message = "&%player% has been last seen ";
		message = message.replace("%player%", playerName);

		String seconds = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(time) % 60);
		String minutes = String.valueOf(TimeUnit.MILLISECONDS.toMinutes(time) % 60);
		String hours = String.valueOf(TimeUnit.MILLISECONDS.toHours(time) % 24);
		String days = String.valueOf(TimeUnit.MILLISECONDS.toDays(time) % 30);
		String months = String.valueOf((int) (TimeUnit.MILLISECONDS.toDays(time) / 30));
		String years = String.valueOf((int) TimeUnit.MILLISECONDS.toDays(time) / 365);

		ConsoleLogging.info(seconds);
		ConsoleLogging.info(minutes);
		ConsoleLogging.info(hours);
		ConsoleLogging.info(days);
		ConsoleLogging.info(months);
		ConsoleLogging.info(years);
		ConsoleLogging.info(String.valueOf(seconds.isEmpty()));
		message += (!years.equals("0") ? years + " years " : "");
		message += (!months.equals("0") ? months + " months " : "");
		message += (!days.equals("0") ? days + "d " : "");
		message += (!hours.equals("0") ? hours + "h " : "");
		message += (!minutes.equals("0") ? minutes + "m " : "");
		message += (!seconds.equals("0") ? seconds + "s " : "");
		message += "ago";
		MessageSender.send(message, sender);
	    } catch (DatabaseException e) {
		String message = MCNSAChat.plugin.getConfig().getString("strings.seen-never");
		message = message.replace("%player%", playerName);
		MessageSender.send(message, sender);
	    } catch (SQLException e) {
		String message = MCNSAChat.plugin.getConfig().getString("strings.seen-never");
		message = message.replace("%player%", playerName);
		MessageSender.send(message, sender);

	    }
	}
    }

}
