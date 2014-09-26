package com.mcnsa.chat.plugin.utils;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.mcnsa.chat.plugin.MCNSAChat;
import com.mcnsa.chat.plugin.managers.ChannelManager;
import com.mcnsa.chat.plugin.managers.DatabaseManager;
import com.mcnsa.chat.plugin.managers.PermissionManager;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.plugin.managers.StringManager;
import com.mcnsa.chat.type.ChatChannel;
import com.mcnsa.chat.type.ChatPlayer;
import com.mcnsa.chat.type.Pair;

public class MessageSender {
    public static String stripUnicode(String message) {
	return message;
    }

    public static void joinMessage(Player player, PlayerJoinEvent event) {

	String message = formJoinMessage(player, null);

	// Notify console
	ConsoleLogging.info(Colors.processConsoleColours(message));
	// Set the join message
	event.setJoinMessage(Colors.processConsoleColours(message));
    }

    public static void quitMessage(Player player, PlayerQuitEvent event) {
	// Build the message
	String message = MCNSAChat.plugin.getConfig().getString("strings.player-quit");
	message = message.replace("%server%", MCNSAChat.serverName);
	message = message.replace("%group%", Colors.PlayerGroup(player));
	message = message.replace("%prefix%", Colors.PlayerPrefix(player));
	message = message.replace("%player%", player.getName());
	message = message.replace("%suffix%", Colors.PlayerSuffix(player));

	// Notify console
	ConsoleLogging.info(Colors.processConsoleColours(message));
	// Set quit message
	event.setQuitMessage(Colors.processConsoleColours(message));
    }

    public static String formJoinMessage(Player player, String server) {

	String message = null;
	long lastLogin;
	try {
	    ResultSet result = DatabaseManager.accessQuery("SELECT lastLogin FROM chat_players WHERE player = ?;", player.getName());
	    lastLogin = result.getLong("lastLogin");
	} catch (Exception e) {
	    lastLogin = 0;
	    ConsoleLogging.warning("Could not find a players last login");

	}
	long loginSince = (new Date().getTime() - lastLogin) / 1000; // Convert
								     // it to
								     // seconds
	// Load our list of greetings
	HashMap<Long, String> greetings = new HashMap<Long, String>();
	ConfigurationSection configSet = MCNSAChat.plugin.getConfig().getConfigurationSection("greetings");
	for (String key : configSet.getKeys(false)) {
	    greetings.put(Long.valueOf(key), configSet.getString(key));
	}
	Object[] keys = greetings.keySet().toArray();
	Arrays.sort(keys);
	if (greetings.size() != 0) {
	    int i;
	    for (i = keys.length - 1; i >= 0; i--) {
		long seconds = (Long) keys[i];
		if (loginSince > seconds) {

		    seconds = (Long) keys[i];
		    message = greetings.get(keys[i]);
		    message = message.replace("%seconds%", String.valueOf(loginSince % 60));
		    message = message.replace("%minutes%", String.valueOf(loginSince / 60 % 60));
		    message = message.replace("%hours%", String.valueOf(loginSince / 60 / 60 % 24));
		    message = message.replace("%days%", String.valueOf(loginSince / 60 / 60 / 24 % 30));
		    message = message.replace("%months%", String.valueOf(loginSince / 60 / 60 / 24 / 30 % 12)); // Not
														// exactly
														// months,
														// but
														// close
														// enough
		    message = message.replace("%years%", String.valueOf(loginSince / 60 / 60 / 24 / 30 / 12)); // Probably
													       // unneccessary
		    break;
		}
	    }
	    if (message == null && i == 0 && greetings.size() > 1) {

		message = greetings.get(keys[0]);
		message = message.replace("%seconds%", String.valueOf(loginSince % 60));
		message = message.replace("%minutes%", String.valueOf(loginSince / 60 % 60));
		message = message.replace("%hours%", String.valueOf(loginSince / 60 / 60 % 24));
		message = message.replace("%days%", String.valueOf(loginSince / 60 / 60 / 24 % 30));
		message = message.replace("%months%", String.valueOf(loginSince / 60 / 60 / 24 / 30 % 12)); // Not
													    // exactly
													    // months,
													    // but
													    // close
													    // enough
		message = message.replace("%years%", String.valueOf(loginSince / 60 / 60 / 24 / 30 / 12)); // Probably
													   // unneccessary

	    }
	} else {
	    ConsoleLogging.warning("Could not load greetings from config");
	}
	if (message == null || lastLogin == 0) {
	    ConsoleLogging.warning("Couldn't get a good message");
	    message = MCNSAChat.plugin.getConfig().getString("strings.player-join");
	}

	// Build the message
	if (server == null)
	    message = message.replace("%server%", MCNSAChat.serverName);
	else
	    message = message.replace("%server%", server);

	message = message.replace("%group%", Colors.PlayerGroup(player));
	message = message.replace("%prefix%", Colors.PlayerPrefix(player));
	message = message.replace("%player%", player.getName());
	message = message.replace("%suffix%", Colors.PlayerSuffix(player));

	return message;
    }

    public static void sendPM(String rawMessage, UUID sender, UUID target) {
	sendPM(rawMessage, PlayerManager.getPlayer(sender), PlayerManager.getPlayer(target));

    }

    public static void sendPM(String rawMessage, ChatPlayer sender, ChatPlayer target) {
	// Function sends the message back to the player sending the pm
	String message = MCNSAChat.plugin.getConfig().getString("strings.pm_send");
	message = message.replace("%prefix%", Colors.PlayerPrefix(sender));
	message = message.replace("%from%", sender.getName());
	message = message.replace("%to%", target.getName());
	message = message.replace("%message%", rawMessage);

	// Send to sender
	send(Colors.processConsoleColours(message), sender);

	// set last pm to the target
	sender.lastPm = target.getUUID();

    }

    public static void sendPM(String string, CommandSender sender, ChatPlayer searchPlayer) {
	if (sender instanceof Player) {
	    Player p = (Player) sender;
	    sendPM(string, p.getUniqueId(), searchPlayer.getUUID());
	} else {
	    // TODO
	    // CONSOLE SENDPM MESSAGE
	}

    }

    public static void sendPMConsole(String string, ChatPlayer player) {
	// Function sends the message back to the player sending the pm
	String message = MCNSAChat.plugin.getConfig().getString("strings.pm_send");
	message = message.replace("%prefix%", Colors.PlayerPrefix(player));
	message = message.replace("%from%", "console");
	message = message.replace("%to%", player.getName());
	message = message.replace("%message%", string);

	// Send to sender
	sendToConsole(Colors.processConsoleColours(message));
    }

    public static void sendPMConsole(String string, Player player) {
	sendPMConsole(string, PlayerManager.getPlayer(player));
    }

    public static void sendPMConsole(String string, CommandSender player) {
	sendPMConsole(string, PlayerManager.getPlayer(player));
    }

    public static void sendPMConsoleConsole(String string) {
	String message = MCNSAChat.plugin.getConfig().getString("strings.pm_send");
	message = message.replace("%from%", "console");
	message = message.replace("%to%", "console");
	message = message.replace("%message%", string);

	// Send to sender
	sendToConsole(message);

    }

    public static void receivePMConsole(String string, CommandSender player) {
	receivePMConsole(string, PlayerManager.getPlayer(player));
    }

    public static void receivePMConsole(String string, Player player) {
	receivePMConsole(string, PlayerManager.getPlayer(player));
    }

    public static void receivePMConsole(String string, ChatPlayer player) {
	String message = MCNSAChat.plugin.getConfig().getString("strings.pm_receive");
	message = message.replace("%from%", "console");
	message = message.replace("%to%", player.getName());
	message = message.replace("%message%", string);
	send(message, player);
    }

    public static void receivePMConsoleConsole(String string) {
	String message = MCNSAChat.plugin.getConfig().getString("strings.pm_receive");
	message = message.replace("%from%", "console");
	message = message.replace("%to%", "console");
	message = message.replace("%message%", string);
	sendToConsole(message);
    }

    public static void recievePM(String rawMessage, ChatPlayer sender, ChatPlayer target) {// Function
											   // sends
											   // the
											   // message
											   // to
											   // the
											   // player
	String message = MCNSAChat.plugin.getConfig().getString("strings.pm_receive");
	message = message.replace("%prefix%", Colors.PlayerPrefix(sender));
	message = message.replace("%from%", sender.getName());
	message = message.replace("%to%", target.getName());
	message = message.replace("%message%", rawMessage);

	// Check if the target has muted the sender
	if (target.hasMuted(sender)) {
	    // send(Colours.processConsoleColours(message), target);
	    send(message, target);
	    // Set the targets last pm
	    target.lastPm = sender.getUUID();

	}
    }

    public static void recievePM(String rawMessage, UUID sender, UUID target) {
	recievePM(rawMessage, PlayerManager.getPlayer(sender), PlayerManager.getPlayer(target));
    }

    public static void recievePM(String string, CommandSender sender, ChatPlayer searchPlayer) {
	if (sender instanceof Player) {
	    Player p = (Player) sender;
	    recievePM(string, p.getUniqueId(), searchPlayer.getUUID());
	} else {
	    // TODO
	    // CONSOLE SENDPM MESSAGE
	}

    }

    public static void send(String message, UUID uuid) {
	Player playerRecieving = Bukkit.getPlayer(uuid);
	if (playerRecieving != null) {
	    playerRecieving.sendMessage(Colors.processConsoleColours(message));
	    if (message.matches(MCNSAChat.subredditNameMatch) || message.matches(MCNSAChat.userNameMatch))
		sendRawMessage(message, playerRecieving);
	}
    }

    public static void send(String message, CommandSender commandSender) {
	if (commandSender instanceof Player) {
	    Player reciever = (Player) commandSender;
	    reciever.sendMessage(Colors.processConsoleColours(message));
	    if (message.matches(MCNSAChat.subredditNameMatch) || message.matches(MCNSAChat.userNameMatch))
		sendRawMessage(message, reciever);

	} else {
	    sendToConsole(message);
	}

    }

    private static void sendRawMessage(String message, Player player) {
	// Testing stuff for subreddit linking
	String jason = " {\"text\":\"\",\"extra\":[{\"text\":\"%before%\",\"color\":\"%before-col%\"},{\"text\":\"%link%\",\"color\":\"%link-col%\",\"underlined\":\"true\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"http://reddit.com%link%\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"%hover%\"}},{\"text\":\"%after%\",\"color\":\"%after-col%\"}]}";
	jason.replace("%before%", "first ");
	jason.replace("%before-col%", "grey");
	jason.replace("%link%", "/r/corenet");
	jason.replace("%link-col%", "orange");
	jason.replace("%after%", " more stuff");
	jason.replace("%after-col%", "blue");
	player.sendRawMessage(jason);
    }

    public static void send(String message, ChatPlayer chatPlayer) {
	send(message, chatPlayer.getUUID());
    }

    public static void send(String message, Player player) {
	send(message, player.getUniqueId());
    }

    public static void sendToConsole(String message) {
	Bukkit.getConsoleSender().sendMessage(Colors.processConsoleColours(message));
    }

    public static void sendToPerm(String message, String perm) {
	for (Player p : Bukkit.getOnlinePlayers()) {
	    if (PermissionManager.checkPermission(perm, p.getName())) {
		MessageSender.send(message, p);
	    }
	}
	MessageSender.sendToConsole(message);
    }

    public static void broadcast(String message) {
	for (Player player : Bukkit.getOnlinePlayers()) {
	    player.sendMessage(Colors.processConsoleColours(message));
	}

    }

    public static void channelMessage(String channel, String serverCode, ChatPlayer player, String rawMessage) {

	if (!MCNSAChat.bannedWordsNotify.isEmpty() && !PermissionManager.checkPermission("chat.banned-word-immunity", player) && rawMessage.matches(MCNSAChat.bannedWordsNotify)) {
	    String playerMessage = MCNSAChat.plugin.getConfig().getString("strings.banned-word-player", "That message contains banned words or characters");
	    playerMessage = playerMessage.replace("%player%", player.getName());
	    playerMessage = playerMessage.replace("%message%", rawMessage);
	    send(playerMessage, player);

	    String adminMessage = MCNSAChat.plugin.getConfig().getString("strings.banned-word-admin", "%player% tried to send a message containing banned words: %message%");
	    adminMessage = adminMessage.replace("%player%", player.getName());
	    adminMessage = adminMessage.replace("%message%", rawMessage);
	    sendToPerm(adminMessage, "admin.notify");
	    return;
	}
	if (!MCNSAChat.bannedWordsSilent.isEmpty() && !PermissionManager.checkPermission("chat.banned-word-immunity", player) && rawMessage.matches(MCNSAChat.bannedWordsSilent)) {
	    String playerMessage = MCNSAChat.plugin.getConfig().getString("strings.banned-word-player", "That message contains banned words or characters");
	    playerMessage = playerMessage.replace("%player%", player.getName());
	    playerMessage = playerMessage.replace("%message%", rawMessage);
	    send(playerMessage, player);
	    return;
	}
	String processedMessage = rawMessage;

	// Channel modes
	if (ChannelManager.getChannel(channel) != null) {
	    ChatChannel chan = ChannelManager.getChannel(channel);

	    if (!PermissionManager.checkPermission(chan.writePermission, player) && serverCode.equalsIgnoreCase(MCNSAChat.shortCode)) {
		send("&cYou do not have permission to chat in this channel", player);
		return;
	    }

	}

	// Get the base message
	String message = MCNSAChat.plugin.getConfig().getString("strings.message");
	message = message.replace("%server%", serverCode);

	// Support for channelcolours
	if (ChannelManager.getChannel(channel) != null)
	    message = message.replace("%channel%", ChannelManager.getChannel(channel).color + ChannelManager.getChannel(channel).name);
	else
	    message = message.replace("%channel%", channel);

	message = message.replace("%prefix%", Colors.PlayerPrefix(player));
	message = message.replace("%group%", Colors.PlayerGroup(player));
	message = message.replace("%player%", player.getName());
	message = message.replace("%suffix%", Colors.PlayerSuffix(player));
	message = Colors.processConsoleColours(message);
	// Strip colour if no permissions
	if (PermissionManager.canUseColours(player))
	    processedMessage = Colors.processConsoleColours(rawMessage);
	message = message.replace("%message%", processedMessage);
	ArrayList<ChatPlayer> players = ChannelManager.getPlayersListening(channel);
	if (players != null) {
	    for (ChatPlayer sendPlayer : players) {
		if (!sendPlayer.server.equals(MCNSAChat.shortCode))
		    continue;
		// Check if the sending player is muted by the player recieving
		// the message
		if (!sendPlayer.hasMuted(player) && !sendPlayer.equals(player)) {
		    if ((!player.modes.get("S-MUTE")) && ChannelManager.getChannel(channel) != null && PermissionManager.checkPermission(ChannelManager.getChannel(channel).readPermission, sendPlayer)
			    || ChannelManager.getChannel(channel) == null)
			send(message, sendPlayer);
		}
	    }
	}
	send(message, player);
	// Log to file
	FileLog.writeChat(serverCode, player.getName(), channel, rawMessage);

	// Check if logging to console
	if (MCNSAChat.plugin.getConfig().getBoolean("consoleLogChat")) {
	    // Check for network message logging
	    if (MCNSAChat.plugin.getConfig().getBoolean("consoleLogServers") && !serverCode.equals(MCNSAChat.shortCode) || serverCode.equals(MCNSAChat.shortCode))
		Bukkit.getConsoleSender().sendMessage(message);
	}

    }

    public static void actionMessage(ChatPlayer player, String rawMessage, String server, String channel) {
	String processedMessage = rawMessage;

	// Get the base message
	String message = MCNSAChat.plugin.getConfig().getString("strings.action");
	message = message.replace("%server%", server);

	// Support for channelcolours
	if (ChannelManager.getChannel(channel) != null)
	    message = message.replace("%channel%", ChannelManager.getChannel(channel).color + ChannelManager.getChannel(channel).name);
	else
	    message = message.replace("%channel%", channel);

	message = message.replace("%prefix%", Colors.PlayerPrefix(player));
	message = message.replace("%group%", Colors.PlayerGroup(player));
	message = message.replace("%player%", player.getName());
	message = message.replace("%suffix%", Colors.PlayerSuffix(player));
	message = Colors.processConsoleColours(message);
	// Strip colours if needed
	if (PermissionManager.canUseColours(player))
	    processedMessage = Colors.processConsoleColours(rawMessage);
	message = message.replace("%message%", processedMessage);
	ArrayList<ChatPlayer> players = ChannelManager.getPlayersListening(channel);
	if (players != null) {
	    for (ChatPlayer sendPlayer : players) {
		if (!sendPlayer.server.equals(MCNSAChat.shortCode))
		    continue;
		// Check if the sending player is muted by the player recieving
		// the message
		if (!sendPlayer.hasMuted(player) && !player.modes.get("S-MUTE") && !sendPlayer.equals(player)) {
		    send(message, sendPlayer);
		}
	    }
	}
	send(message, player);

	// Log to file
	FileLog.writeChat(server, "*" + player, channel, rawMessage);

	// Check if logging to console
	if (MCNSAChat.plugin.getConfig().getBoolean("consoleLogChat")) {
	    // Check for network message logging
	    if (MCNSAChat.plugin.getConfig().getBoolean("consoleLogServers") && !server.equals(MCNSAChat.shortCode) || server.equals(MCNSAChat.shortCode))
		Bukkit.getConsoleSender().sendMessage(message);
	}

    }

    public static void timeoutPlayer(UUID uuid, String time, String reason) {
	if (reason.length() < 1)
	    reason = "Breaking chat rules";
	// Get base string
	String notifyMessage = MCNSAChat.plugin.getConfig().getString("strings.timeout-player");
	notifyMessage = notifyMessage.replace("%time%", time);

	String reasonMessage = MCNSAChat.plugin.getConfig().getString("strings.timeout-reason");
	reasonMessage = reasonMessage.replace("%reason%", reason);

	MessageSender.send(notifyMessage, uuid);
	MessageSender.send(reasonMessage, uuid);

	// send to everyone in the players channel
	String playernotify = MCNSAChat.plugin.getConfig().getString("strings.timeout-players");
	playernotify = playernotify.replace("%prefix%", Colors.PlayerPrefix(uuid));
	playernotify = playernotify.replace("%player%", Bukkit.getPlayer(uuid).getDisplayName());
	playernotify = playernotify.replace("%time%", time);

	ArrayList<ChatPlayer> players = ChannelManager.getPlayersListening(PlayerManager.getPlayer(uuid).channel);
	if (players != null) {
	    for (ChatPlayer sendPlayer : players) {
		if (!sendPlayer.server.equals(MCNSAChat.shortCode))
		    continue;
		// Check if the sending player is the player in timeout
		if (!sendPlayer.getUUID().equals(uuid)) {

		    MessageSender.send(playernotify, sendPlayer.getUUID());
		    MessageSender.send(reasonMessage, sendPlayer.getUUID());
		}
	    }
	}

	// log to console
	Bukkit.getConsoleSender().sendMessage(Colors.processConsoleColours(playernotify + " " + reasonMessage));
    }

    public static void consoleChat(String rawMessage, String channel) {
	// used for console to send messages to a channel
	String message = MCNSAChat.plugin.getConfig().getString("strings.message");
	message = message.replace("%server%", MCNSAChat.shortCode);

	// Support for channelcolours
	if (ChannelManager.getChannel(channel) != null)
	    message = message.replace("%channel%", ChannelManager.getChannel(channel).color + ChannelManager.getChannel(channel).name);
	else
	    message = message.replace("%channel%", channel);

	message = message.replace("%prefix%", MCNSAChat.plugin.getConfig().getString("consoleSender-colour"));
	message = message.replace("%player%", MCNSAChat.plugin.getConfig().getString("consoleSender"));
	message = message.replace("%message%", rawMessage);
	message = message.replace("%suffix%", "");

	for (Player player : Bukkit.getOnlinePlayers()) {
	    ChatPlayer chatPlayer = PlayerManager.getPlayer(player.getUniqueId(), MCNSAChat.shortCode);
	    // Sanity check
	    if (chatPlayer != null) {
		if (chatPlayer.channel.equalsIgnoreCase(channel) || chatPlayer.isListening(channel.toLowerCase()) || PermissionManager.getForceListens(chatPlayer).contains(channel)
			|| chatPlayer.modes.get("SEEALL"))
		    send(Colors.processConsoleColours(message), player);
	    }
	}
	// Log to file
	FileLog.writeChat(MCNSAChat.shortCode, "[" + MCNSAChat.plugin.getConfig().getString("consoleSender") + "]", channel, rawMessage);

	Bukkit.getConsoleSender().sendMessage(Colors.processConsoleColours(message));
    }

    public static String timeSince(long initialTime, long secondTime, String formatting) {
	return timeSince(secondTime - initialTime, formatting);
    }

    // TODO Needs testing
    public static String timeSince(long timeGap, String formatting) {
	Pair seconds = new Pair("%seconds%", String.valueOf(TimeUnit.MILLISECONDS.toSeconds(timeGap) % 60));
	Pair minutes = new Pair("%minutes%", String.valueOf(TimeUnit.MILLISECONDS.toMinutes(timeGap) % 60));
	Pair hours = new Pair("%hours%", String.valueOf(TimeUnit.MILLISECONDS.toHours(timeGap) % 24));
	Pair days = new Pair("%days%", String.valueOf(TimeUnit.MILLISECONDS.toDays(timeGap) % 30));
	Pair months = new Pair("%months%", String.valueOf((int) (TimeUnit.MILLISECONDS.toDays(timeGap) / 30)));
	Pair years = new Pair("%years%", String.valueOf((int) TimeUnit.MILLISECONDS.toDays(timeGap) / 365));
	StringManager.replaceVariables(formatting, seconds, minutes, hours, days, months, years);
	return formatting;
    }
}
