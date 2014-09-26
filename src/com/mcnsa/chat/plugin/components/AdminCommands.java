package com.mcnsa.chat.plugin.components;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.mcnsa.chat.plugin.MCNSAChat;
import com.mcnsa.chat.plugin.exceptions.DatabaseException;
import com.mcnsa.chat.plugin.managers.ChannelManager;
import com.mcnsa.chat.plugin.managers.DatabaseManager;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.plugin.utils.Colors;
import com.mcnsa.chat.plugin.utils.ConsoleLogging;
import com.mcnsa.chat.plugin.utils.MessageSender;
import com.mcnsa.chat.type.ChatChannel;
import com.mcnsa.chat.type.ChatPlayer;

//@ComponentInfo(friendlyName = "Admin", description = "Admin commands", permsSettingsPrefix = "admin")
public class AdminCommands {

    // TODO change command structure including names
    // github issue #22
    // @Command(command = "cto", description = "Player chat timeout",
    // permissions = { "timeout" })
    public static boolean ctoList(CommandSender sender) {
	// Function lists players in timeout

	// Start output arraylist
	Map<String, Long> output = new HashMap<String, Long>();
	// Get players in timeout
	for (ChatPlayer player : PlayerManager.players) {
	    if (player.modes.get("MUTE"))
		if (!output.containsKey(player.getName()))
		    output.put(player.getName(), player.timeoutTill);
	}

	if (output.isEmpty()) {
	    MessageSender.send("&6There is no-one currently in timeout", sender);
	    return true;
	}

	// Start the output
	MessageSender.send("&6Players in timeout", sender);

	// Loop through players in timeout
	for (Entry<String, Long> entry : output.entrySet()) {
	    String player = entry.getKey();
	    Date timeoutTime = new Date(entry.getValue());
	    SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss dd.MMM");
	    MessageSender.send("&c" + player + " Until " + df.format(timeoutTime), sender);
	}

	return true;
    }

    // @Command(command = "untimeout", description = "Untimeout a player",
    // arguments = { "Player" }, permissions = { "timeout" })
    public static boolean timeoutRemove(CommandSender sender, String target) {

	ArrayList<ChatPlayer> targetPlayers = PlayerManager.searchPlayers(target);
	if (targetPlayers.isEmpty()) {
	    try {

		ResultSet playerRS = DatabaseManager.accessQuery("SELECT * FROM chat_Players where UPPER(player) = upper(?)", target);
		if (!playerRS.next()) {
		    MessageSender.send("&cCould not find player", sender);
		    return true;
		}
		if (playerRS.getLong("timeouttill") == 0) {
		    MessageSender.send("&cThat player is not in timeout", sender);
		    return true;
		}

		ResultSet chatRS = DatabaseManager.accessQuery("SELECT * FROM chat_Modes WHERE upper(playerName) = upper(?) AND modeName = ?", target, "MUTE");
		if (!chatRS.next()) {
		    MessageSender.send("&cCould not find player", sender);
		    return true;
		}
		if (chatRS.getBoolean("modeStatus") == false) {
		    MessageSender.send("&cThat player is not in timeout", sender);
		    return true;
		}

		DatabaseManager.updateQuery("UPDATE chat_Players set timeouttill=? WHERE upper(player) = upper(?)", 0, target);
		DatabaseManager.updateQuery("UPDATE chat_Modes set modeStatus=0 WHERE upper(playerName) = upper(?) AND modeName= ?", target, "MUTE");
		MessageSender.send("&6" + target + " has been removed from timeout", sender);
		return true;
	    } catch (DatabaseException e) {
		MessageSender.send("&4A DB Error has occurred", sender);
		e.printStackTrace();
	    } catch (SQLException e) {
		MessageSender.send("&4A DB Error has occurred", sender);
		e.printStackTrace();
	    }
	    return true;
	}

	ChatPlayer targetPlayer = targetPlayers.get(0);

	if (!targetPlayer.modes.get("MUTE")) {
	    MessageSender.send("&cThat player is not in timeout", sender);
	    return true;
	}

	PlayerManager.unmutePlayer(targetPlayer.getUUID());
	MessageSender.send("&6" + targetPlayer.getName() + " has been removed from timeout", sender);

	return true;
    }

    // TODO Allow multiple names
    // This'll be hard
    // @Command(command = "timeout", description = "Timeout a player", arguments
    // = { "Player", "time", "reason" }, permissions = { "timeout" })
    public static boolean timeoutAdd(CommandSender sender, String target, String time, String... reason) {
	ArrayList<ChatPlayer> targetPlayers = PlayerManager.searchPlayers(target);
	if (targetPlayers.isEmpty()) {
	    // Find offline player
	    try {
		ResultSet playerRS = DatabaseManager.accessQuery("SELECT * FROM chat_Players where upper(player) = upper(?)", target);
		if (!playerRS.next()) {
		    MessageSender.send("&cCould not find player", sender);
		    return true;
		}
		if (playerRS.getLong("timeouttill") != 0) {
		    MessageSender.send("&cThat player already in timeout", sender);
		    return true;
		}
		ResultSet chatRS = DatabaseManager.accessQuery("SELECT * FROM chat_Modes where upper(playerName) = upper(?)", target);
		if (!chatRS.next()) {
		    MessageSender.send("&cCould not find player", sender);
		    return true;
		}
		long timeout = (long) (new Date().getTime() + (Double.valueOf(time) * 60000));
		DatabaseManager.updateQuery("UPDATE chat_Players set timeouttill=? WHERE upper(player) = upper(?)", timeout, target);
		DatabaseManager.updateQuery("UPDATE chat_Modes set modeStatus=1 WHERE upper(playerName) = upper(?) AND modeName= ?", target, "MUTE");

		MessageSender.send("&6" + target + " has been added to timeout", sender);
		return true;
	    } catch (DatabaseException e) {
		MessageSender.send("&4A DB Error has occurred", sender);
		e.printStackTrace();
	    } catch (SQLException e) {
		MessageSender.send("&4A DB Error has occurred", sender);
		e.printStackTrace();
	    }
	}
	ChatPlayer targetPlayer = targetPlayers.get(0);

	if (targetPlayer.modes.get("MUTE")) {
	    MessageSender.send("&cThat player already in timeout", sender);
	    return true;
	}
	// Build reason
	StringBuffer sb = new StringBuffer();
	for (String reasonPart : reason) {
	    if (sb.length() < 1)
		sb.append(reasonPart);
	    else
		sb.append(" " + reasonPart);
	}
	// Mute and notify
	PlayerManager.mutePlayer(targetPlayer.getUUID(), time, sb.toString());

	// Start timer
	final UUID finalUUID = targetPlayer.getUUID();
	long timeleft = (long) (Double.valueOf(time) * 1200);
	Bukkit.getScheduler().scheduleSyncDelayedTask(MCNSAChat.plugin, new Runnable() {
	    public void run() {
		if (PlayerManager.getPlayer(finalUUID, MCNSAChat.shortCode) != null && PlayerManager.getPlayer(finalUUID, MCNSAChat.shortCode).modes.get("MUTE")) {
		    PlayerManager.unmutePlayer(finalUUID);
		}
	    }
	}, timeleft);

	return true;
    }

    // @Command(command = "cregister", description =
    // "Register a channel with the channel manager", permissions = { "register"
    // }, arguments = { "Channel" }, playerOnly = true)
    public static boolean registerChannel(CommandSender sender, String channel) {
	// Function registers a channel with the channel manager
	channel = channel.substring(0, 1).toUpperCase() + channel.substring(1);
	// Check to make sure the channel isn't already registered.
	if (ChannelManager.getChannel(channel) != null) {
	    MessageSender.send("&cChannel is already registered.", sender);
	    return true;
	}

	// Create the channel
	ChatChannel chatChannel = new ChatChannel(channel);
	// make the channel persistent
	chatChannel.modes.put("PERSIST", true);

	// Let the sender know that its created
	MessageSender.send("&6Channel " + chatChannel + " registered", sender);

	// Add to channel Manager
	ChannelManager.addChannel(chatChannel);
	return true;
    }

    // @Command(command = "cmode", description =
    // "Add a mode to the channel your in: Rave, Boring, or Local", arguments =
    // { "action", "Mode" }, permissions = { "mode" }, playerOnly = true)
    public static boolean addMode(CommandSender sender, String action, String mode) {
	// Function adds modes to the channel
	// Get the channel
	ChatChannel channel = ChannelManager.getChannel(PlayerManager.getPlayer(sender).channel);
	if (channel == null) {
	    MessageSender.send("&cChannel is not registered. Please register first by /cregister <channel>", sender);
	    return true;
	}
	if (action.equalsIgnoreCase("add")) {
	    if (mode.equalsIgnoreCase("rave")) {
		channel.modes.put("RAVE", true);
		MessageSender.send("&6Rave mode activated!", sender);
	    } else if (mode.equalsIgnoreCase("boring")) {
		channel.modes.put("BORING", true);
		MessageSender.send("&6Boring mode activated!", sender);
	    } else if (mode.equalsIgnoreCase("local")) {
		channel.modes.put("LOCAL", true);
		MessageSender.send("&6Local mode activated!", sender);
	    }
	} else if (action.equalsIgnoreCase("del")) {
	    if (mode.equalsIgnoreCase("rave")) {
		channel.modes.put("RAVE", false);
		MessageSender.send("&6Rave mode deactivated!", sender);
	    } else if (mode.equalsIgnoreCase("boring")) {
		channel.modes.put("BORING", false);
		MessageSender.send("&6Boring mode deactivated!", sender);
	    } else if (mode.equalsIgnoreCase("local")) {
		channel.modes.put("LOCAL", false);
		MessageSender.send("&6Local mode deactivated!", sender);
	    }
	} else {
	    MessageSender.send("&cUsage: /cmode [add|del] [rave|boring|mute|local]", sender);
	}
	return true;
    }

    // @Command(command = "cdelete", permissions = "register", description =
    // "Deregister a channel", arguments = { "Channel" })
    public static boolean deregister(CommandSender sender, String channel) {
	channel = channel.substring(0, 1).toUpperCase() + channel.substring(1);
	ChatChannel chan = ChannelManager.getChannel(channel);
	if (chan == null) {
	    MessageSender.send("&cChannel is not registered.", sender);
	    return true;
	}

	// Remove from channel Manager
	ChannelManager.removeChannel(chan.name);

	chan.modes.put("PERSIST", false);

	// Inform user
	MessageSender.send("&cChannel: " + channel + " has been removed.", sender);

	return true;
    }

    // @Command(command = "say", arguments = { "Message" }, permissions = {
    // "say" }, description = "Send a message as console")
    public static boolean say(CommandSender sender, String... message) {
	// Function allows mods to speak as [Console]
	StringBuffer Message = new StringBuffer();
	Message.append(MCNSAChat.plugin.getConfig().getString("consoleSender-colour") + "[" + MCNSAChat.plugin.getConfig().getString("consoleSender") + "]");
	for (String part : message) {
	    Message.append(" " + part);
	}

	Bukkit.broadcastMessage(Colors.processConsoleColours(Message.toString()));
	return true;
    }

    // @Command(command = "cmove", arguments = { "Player", "Channel" },
    // permissions = { "move" }, description =
    // "Move a player to a different channel")

    // @Command(command = "creload", description = "Reload chat configuration",
    // permissions = { "reload" })
    public static boolean reload(CommandSender sender) {
	// Reloads the config file
	MCNSAChat.plugin.reloadConfig();
	MessageSender.send("&6Chat Config reloaded", sender);
	return true;
    }

    // @Command(command = "cnet", description =
    // "Cross server chat controls, use off, on, or reset", permissions = {
    // "csccontrol" })
    public static boolean net(CommandSender sender, String action) {
	throw new UnsupportedOperationException();
    }

    // @Command(command = "seeall", description = "View all channels",
    // permissions = { "seeall" }, playerOnly = true)
    public static boolean seeall(CommandSender sender) {
	// Function sets the mode to allow to see all channels
	// get player
	ChatPlayer player = PlayerManager.getPlayer(sender, MCNSAChat.shortCode);

	if (player.modes.get("SEEALL")) {
	    player.modes.put("SEEALL", false);
	    MessageSender.send("&6You are no longer listening to all channels", player);
	} else {
	    player.modes.put("SEEALL", true);
	    MessageSender.send("&6You are now listening to all channels", player);
	}
	return true;
    }

    // @Command(command = "chansay", description =
    // "send a message to channel via console", arguments = { "Channel",
    // "message" }, permissions = { "console" })
    public static boolean consolechat(CommandSender sender, String Channel, String... rawMessage) {

	StringBuffer message = new StringBuffer();
	for (String part : rawMessage) {
	    if (message.length() < 1)
		message.append(part);
	    else {
		message.append(" " + part);
	    }
	}

	MessageSender.consoleChat(message.toString(), Channel);

	return true;

    }

    // @Command(command = "forceremove", description =
    // "Strips everyone's listens for a channel", arguments = { "Channel" })
    public static boolean stripListens(CommandSender sender, String channel) {
	// Strips everyone's listens for a channel
	for (int i = 0; i < PlayerManager.players.size(); i++) {
	    ChatPlayer player = PlayerManager.players.get(i);
	    if (player.isListening(channel)) {
		player.stopListening(channel);
		MessageSender.send("removed " + player.getName() + "from listening to " + channel, sender);
	    }
	}

	return true;
    }

    // @Command(command = "lockdown", description =
    // "Locks down the server, not letting any new players enter", permissions =
    // { "lockdown.disable" })
    public static boolean unLockdown(CommandSender sender) {
	if (MCNSAChat.isLockdown) {

	    String message = MCNSAChat.plugin.getConfig().getString("strings.lockdown-disable");
	    MessageSender.sendToPerm(message, "admin.notify");
	    if (MCNSAChat.lockdownTimerID != 0) {
		Bukkit.getScheduler().cancelTask(MCNSAChat.lockdownTimerID);
		MCNSAChat.lockdownTimerID = 0;
		MCNSAChat.lockdownUnlockTime = 0;
		MCNSAChat.lockdownReason = "";
	    }
	    MCNSAChat.isLockdown = false;
	} else {

	    String message = MCNSAChat.plugin.getConfig().getString("strings.lockdown-failed");
	    message = message.replace("%reason%", "Server not in lockdown");
	    MessageSender.send(message, sender);
	}
	return true;
    }

    // @Command(command = "lockdown", description =
    // "Locks down the server, not letting any new players enter", permissions =
    // { "lockdown.timed" }, arguments = { "time", "reason" })
    public static boolean lockdownTimed(CommandSender sender, int time, String reason) {
	if (!MCNSAChat.isLockdown) {
	    String message = MCNSAChat.plugin.getConfig().getString("strings.lockdown-enable-temp");
	    message = message.replace("%reason%", reason);
	    MCNSAChat.isLockdown = true;
	    message = message.replace("%minutes%", String.valueOf(time));
	    MessageSender.sendToPerm(message, "admin.notify");
	    long timeleft = time * 1200;
	    long currentTime = new Date().getTime();
	    MCNSAChat.lockdownUnlockTime = currentTime + time * 60 * 1000;
	    MCNSAChat.lockdownReason = reason;
	    MCNSAChat.lockdownTimerID = Bukkit.getScheduler().scheduleSyncDelayedTask(MCNSAChat.plugin, new Runnable() {
		public void run() {
		    String message = MCNSAChat.plugin.getConfig().getString("strings.lockdown-disable");
		    MCNSAChat.isLockdown = false;
		    message = message.replace("%player%", "timer");
		    MCNSAChat.lockdownUnlockTime = 0;
		    MCNSAChat.lockdownReason = "";
		    MessageSender.sendToPerm(message, "admin.notify");
		}
	    }, timeleft);
	} else {
	    String message = MCNSAChat.plugin.getConfig().getString("strings.lockdown-failed");
	    message = message.replace("%reason%", "Server already in temporary lockdown");
	    MessageSender.send(message, sender);
	}
	return true;
    }

    // @Command(command = "lockdownpersist", description =
    // "Locks down the server, not letting any new players enter", permissions =
    // { "lockdown.persist" }, arguments = { "reason" }, playerOnly = false)
    public static boolean lockdownPersist(CommandSender sender, String reason) {
	ConsoleLogging.info("done persist");
	if (MCNSAChat.isLockdown && MCNSAChat.lockdownTimerID != 0) {
	    Bukkit.getScheduler().cancelTask(MCNSAChat.lockdownTimerID);
	    MCNSAChat.lockdownTimerID = 0;
	    MCNSAChat.lockdownUnlockTime = 0;
	}
	MCNSAChat.lockdownReason = reason;
	String message = MCNSAChat.plugin.getConfig().getString("strings.lockdown-enable-persist");
	message = message.replace("%reason%", reason);
	ConsoleLogging.info("done persist");
	MessageSender.sendToPerm(message, "admin.notify");
	MCNSAChat.isLockdown = true;
	return true;
    }

}
