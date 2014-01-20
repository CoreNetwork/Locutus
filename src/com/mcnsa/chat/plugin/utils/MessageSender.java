package com.mcnsa.chat.plugin.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.mcnsa.chat.networking.Network;
import com.mcnsa.chat.plugin.MCNSAChat;
import com.mcnsa.chat.plugin.exceptions.DatabaseException;
import com.mcnsa.chat.plugin.managers.ChannelManager;
import com.mcnsa.chat.plugin.managers.DatabaseManager;
import com.mcnsa.chat.plugin.managers.Permissions;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.type.ChatChannel;
import com.mcnsa.chat.type.ChatPlayer;

public class MessageSender {
	public static String stripUnicode(String message){
		return message;
	}
	public static void joinMessage(String playerName, PlayerJoinEvent event) {

		
		String message = formJoinMessage(playerName, null);

		//Notify console
		MCNSAChat.console.info(Colours.processConsoleColours(message));
		//Set the join message
		event.setJoinMessage(Colours.processConsoleColours(message));
	}
	public static void quitMessage(String playerName, PlayerQuitEvent event) {
		//Build the message
		String message = MCNSAChat.plugin.getConfig().getString("strings.player-quit");
		message = message.replace("%server%", MCNSAChat.serverName);
		message = message.replace("%group%", Colours.PlayerGroup(playerName));
		message = message.replace("%prefix%", Colours.PlayerPrefix(playerName));
		message = message.replace("%player%", playerName);
		message = message.replace("%suffix%", Colours.PlayerSuffix(playerName));
		
		//Notify console
		MCNSAChat.console.info(Colours.processConsoleColours(message));
		//Set quit message
		event.setQuitMessage(Colours.processConsoleColours(message));
	}
	public static String formJoinMessage(String playerName, String server)
	{
		
		String message = null;
		long lastLogin;
		try {
			ResultSet result = DatabaseManager.accessQuery("SELECT lastLogin FROM chat_players WHERE player = ?;", playerName);
			lastLogin = result.getLong("lastLogin");
		} catch (Exception e) {
			lastLogin = 0;
			ConsoleLogging.warning("Could not find a players last login");
			
		} 
		long loginSince = (new Date().getTime() - lastLogin) / 1000;	//Convert it to seconds
		//Load our list of greetings
		HashMap<Long, String> greetings = new HashMap<Long, String>();
		ConfigurationSection configSet =MCNSAChat.plugin.getConfig().getConfigurationSection("greetings");
		for (String key : configSet.getKeys(false))
		{
			greetings.put(Long.valueOf(key), configSet.getString(key));
		}
		Object[] keys =  greetings.keySet().toArray();
		Arrays.sort(keys);
		if (greetings.size() != 0)
		{
			int i;
			for (i = keys.length - 1; i >= 0; i--)
			{
				long seconds = (Long) keys[i];
				if (loginSince > seconds)
				{
				
					seconds = (Long) keys[i];
					message = 	greetings.get(keys[i]);
					message = 	message.replace("%seconds%", String.valueOf(loginSince % 60));
					message = 	message.replace("%minutes%", String.valueOf(loginSince / 60 % 60));
					message = 	message.replace("%hours%", String.valueOf(loginSince / 60 / 60 % 24));
					message = 	message.replace("%days%", String.valueOf(loginSince / 60 / 60 / 24 % 30));
					message = 	message.replace("%months%", String.valueOf(loginSince / 60 / 60 / 24 / 30 % 12));	//Not exactly months, but close enough
					message = 	message.replace("%years%", String.valueOf(loginSince / 60 / 60 / 24 / 30 / 12));	//Probably unneccessary
					break;
				}
			}
			if (message == null && i == 0 && greetings.size() > 1)
			{

				message = 	greetings.get(keys[0]);
				message = 	message.replace("%seconds%", String.valueOf(loginSince % 60));
				message = 	message.replace("%minutes%", String.valueOf(loginSince / 60 % 60));
				message = 	message.replace("%hours%", String.valueOf(loginSince / 60 / 60 % 24));
				message = 	message.replace("%days%", String.valueOf(loginSince / 60 / 60 / 24 % 30));
				message = 	message.replace("%months%", String.valueOf(loginSince / 60 / 60 / 24 / 30 % 12));	//Not exactly months, but close enough
				message = 	message.replace("%years%", String.valueOf(loginSince / 60 / 60 / 24 / 30 / 12));	//Probably unneccessary
				
			}
		}
		else
		{
			ConsoleLogging.warning("Could not load greetings from config");
		}
		if (message == null || lastLogin == 0)
		{
			ConsoleLogging.warning("Couldn't get a good message");
			message = MCNSAChat.plugin.getConfig().getString("strings.player-join");
		}
		
		//Build the message
		if (server == null)
			message = message.replace("%server%", MCNSAChat.serverName);
		else
			message = message.replace("%server%", server);

		message = message.replace("%group%", Colours.PlayerGroup(playerName));
		message = message.replace("%prefix%", Colours.PlayerPrefix(playerName));
		message = message.replace("%player%", playerName);
		message = message.replace("%suffix%", Colours.PlayerSuffix(playerName));
		
		return message;
	}
	public static void joinMessage(ChatPlayer player, String server) {

		String message = formJoinMessage(player.name, server);
		
		//Loop through players listening list and collect names
		ArrayList<ChatPlayer> sendTo = new ArrayList<ChatPlayer>();
		for(String channel: player.listening) {
			ArrayList<ChatPlayer> targetPlayers = ChannelManager.getPlayersListening(channel);
			if (targetPlayers != null) {
				for (ChatPlayer targetPlayer: targetPlayers){
					if (!sendTo.contains(targetPlayer) && targetPlayer.server.equals(MCNSAChat.shortCode))
						sendTo.add(targetPlayer);
				}
			}
		}
		//Now send to players
		if (!sendTo.isEmpty()) {
			for (ChatPlayer reciever: sendTo) {
				if (reciever.server.equals(MCNSAChat.shortCode))
					send(Colours.processConsoleColours(message), reciever.name);
			}
		}
	}
	public static void quitMessage(ChatPlayer player, String server) {
		//Build the message
		String message = MCNSAChat.plugin.getConfig().getString("strings.player-quit");
		message = message.replace("%server%", server);
		message = message.replace("%group%", Colours.PlayerGroup(player.name));
		message = message.replace("%prefix%", Colours.PlayerPrefix(player.name));
		message = message.replace("%player%", player.name);
		message = message.replace("%suffix%", Colours.PlayerSuffix(player.name));
		
		//Loop through players listening list and collect names
		ArrayList<ChatPlayer> sendTo = new ArrayList<ChatPlayer>();
		//Loop through player's listening channels
		for(String channel: player.listening) {
			//Get the players in the channel
			ArrayList<ChatPlayer> targetPlayers = ChannelManager.getPlayersListening(channel);
			//Loop through the players
			for (ChatPlayer targetPlayer: targetPlayers){
				//Check if not already in the list
				if (!sendTo.contains(targetPlayer) && targetPlayer.server.equals(MCNSAChat.shortCode))
					//Add to the list. Player is on this server and not already in the send to list
					sendTo.add(targetPlayer);
			}
		}
		//Now send to players
		if (!sendTo.isEmpty()) {
			for (ChatPlayer reciever: sendTo) {
				if (reciever.server.equals(MCNSAChat.shortCode))
					send(Colours.processConsoleColours(message), reciever.name);
			}
		}
	}
	public static void shadowSendPM(String rawMessage, String sender, String target)
	{
		if (sender.equalsIgnoreCase("console")) {
			
		}
		else {
			//Function sends the message back to the player sending the pm
			String message = MCNSAChat.plugin.getConfig().getString("strings.pm_send");
			message = message.replace("%prefix%", Colours.PlayerPrefix(sender));
			message = message.replace("%from%", sender);
			message = message.replace("%to%", target);
			message = message.replace("%message%", rawMessage);
			
			//Send to sender
			send(Colours.processConsoleColours(message), sender);
			
			//set last pm to the target
			PlayerManager.getPlayer(sender).lastPm = target;
			
			//update player
			Network.updatePlayer(PlayerManager.getPlayer(sender));
		}
	}
	public static void sendPM(String rawMessage, String sender, String target) {
		if (sender.equalsIgnoreCase("console")) {
			MCNSAChat.console.pm_send(target, rawMessage);
		}
		else {
			//Function sends the message back to the player sending the pm
			String message = MCNSAChat.plugin.getConfig().getString("strings.pm_send");
			message = message.replace("%prefix%", Colours.PlayerPrefix(sender));
			message = message.replace("%from%", sender);
			message = message.replace("%to%", target);
			message = message.replace("%message%", rawMessage);
			
			//Send to sender
			send(Colours.processConsoleColours(message), sender);
			
			//set last pm to the target
			PlayerManager.getPlayer(sender).lastPm = target;
			
			//update player
			Network.updatePlayer(PlayerManager.getPlayer(sender));
		}
	}
	public static void recievePM(String rawMessage, String sender, String target) {
		if (target.equalsIgnoreCase("console")) {
			MCNSAChat.console.pm_recieved(sender, rawMessage);
		}
		else {
			//Function sends the message to the player
			String message = MCNSAChat.plugin.getConfig().getString("strings.pm_receive");
			message = message.replace("%prefix%", Colours.PlayerPrefix(sender));
			message = message.replace("%from%", sender);
			message = message.replace("%to%", target);
			message = message.replace("%message%", rawMessage);
			
			//Check if the target has muted the sender
			if (!PlayerManager.getPlayer(target).muted.contains(sender) && Bukkit.getPlayer(target) != null) {
				//send(Colours.processConsoleColours(message), target);
				send(message, target);
				//Set the targets last pm
				PlayerManager.getPlayer(target).lastPm = sender;
				
				//update player
				Network.updatePlayer(PlayerManager.getPlayer(target));
			}
		}
	}
	public static void send(String message, String player) {
		if (player.equalsIgnoreCase("console")) {
			//send to console
			Bukkit.getConsoleSender().sendMessage(Colours.processConsoleColours(message));
			return;
		}
		Player playerRecieving = Bukkit.getPlayer(player);
		if (playerRecieving != null) {
			playerRecieving.sendMessage(Colours.processConsoleColours(message));
		}
	}
	
	public static void sendToPerm(String message, String perm) {
		for (Player p : Bukkit.getOnlinePlayers())
		{
			if(Permissions.checkPermission(perm, p.getName()))
			{
				MessageSender.send(message, p.getName());
			}
		}
		MessageSender.send(message, "console");
	}
	public static void broadcast(String message) {
		for (Player player: Bukkit.getOnlinePlayers()) {
			player.sendMessage(Colours.processConsoleColours(message));
		}
		
	}
	public static void shadowChannelMessage(String channel, String serverCode, String player, String rawMessage)
	{
		String processedMessage = rawMessage;
		//Strip colour if no permissions
		if (!Permissions.useColours(player))
			processedMessage = Colours.stripColor(rawMessage);
		
		//Channel modes
		if (ChannelManager.getChannel(channel) != null) {
			ChatChannel chan = ChannelManager.getChannel(channel);
			
			if (chan.modes.get("RAVE")) {
				//Colourise the message
				processedMessage = Colours.raveColor(rawMessage);
			}
			else if (chan.modes.get("BORING")) {
				//Strip all colour
				processedMessage = Colours.stripColor(processedMessage);
			}
			if (!Permissions.checkWritePerm(chan.write_permission, player) && serverCode.equalsIgnoreCase(MCNSAChat.shortCode)) {
				send("&cYou do not have permission to chat in this channel", player);
				return;
			}
			
		}
						
		//Get the base message
		String message = MCNSAChat.plugin.getConfig().getString("strings.message");
		message = message.replace("%server%", serverCode);
		
		//Support for channelcolours
		if (ChannelManager.getChannel(channel) != null)
			message = message.replace("%channel%", ChannelManager.getChannel(channel).color + ChannelManager.getChannel(channel).name);
		else
			message = message.replace("%channel%", channel);
		
		message = message.replace("%prefix%", Colours.PlayerPrefix(player));
		message = message.replace("%group%", Colours.PlayerGroup(player));
		message = message.replace("%player%", player);
		message = message.replace("%suffix%", Colours.PlayerSuffix(player));
		message = message.replace("%message%", processedMessage);
		send(Colours.processConsoleColours(message), player);
		message = "[SHADOW-MUTED]" + message;
		//Log to file
		FileLog.writeChat(serverCode, player, channel+"[SHADOW-MUTED]", rawMessage);
		
		//Check if logging to console
		if (MCNSAChat.plugin.getConfig().getBoolean("consoleLogChat")) {
			//Check for network message logging
			if (MCNSAChat.plugin.getConfig().getBoolean("consoleLogServers") && !serverCode.equals(MCNSAChat.shortCode) || serverCode.equals(MCNSAChat.shortCode))
				Bukkit.getConsoleSender().sendMessage((Colours.processConsoleColours(message)));
		}
		
	}
	public static void channelMessage(String channel, String serverCode, String player, String rawMessage) {
		
		String processedMessage = rawMessage;
		//Strip colour if no permissions
		if (!Permissions.useColours(player))
			processedMessage = Colours.stripColor(rawMessage);
		
		//Channel modes
		if (ChannelManager.getChannel(channel) != null) {
			ChatChannel chan = ChannelManager.getChannel(channel);
			
			if (chan.modes.get("RAVE")) {
				//Colourise the message
				processedMessage = Colours.raveColor(rawMessage);
			}
			else if (chan.modes.get("BORING")) {
				//Strip all colour
				processedMessage = Colours.stripColor(processedMessage);
			}
			if (!Permissions.checkWritePerm(chan.write_permission, player) && serverCode.equalsIgnoreCase(MCNSAChat.shortCode)) {
				send("&cYou do not have permission to chat in this channel", player);
				return;
			}
			
		}
						
		//Get the base message
		String message = MCNSAChat.plugin.getConfig().getString("strings.message");
		message = message.replace("%server%", serverCode);
		
		//Support for channelcolours
		if (ChannelManager.getChannel(channel) != null)
			message = message.replace("%channel%", ChannelManager.getChannel(channel).color + ChannelManager.getChannel(channel).name);
		else
			message = message.replace("%channel%", channel);
		
		message = message.replace("%prefix%", Colours.PlayerPrefix(player));
		message = message.replace("%group%", Colours.PlayerGroup(player));
		message = message.replace("%player%", player);
		message = message.replace("%suffix%", Colours.PlayerSuffix(player));
		message = message.replace("%message%", processedMessage);
		
		ArrayList<ChatPlayer> players = ChannelManager.getPlayersListening(channel);
		if (players != null) {
			for (ChatPlayer sendPlayer: players) {
				if (!sendPlayer.server.equals(MCNSAChat.shortCode))
					continue;
				//Check if the sending player is muted by the player recieving the message
				if (!sendPlayer.muted.contains(player)) {
					if (ChannelManager.getChannel(channel) != null && Permissions.checkReadPerm(ChannelManager.getChannel(channel).read_permission, sendPlayer.name) || ChannelManager.getChannel(channel) == null)
						send(Colours.processConsoleColours(message), sendPlayer.name);
				}
			}
		}
		//Log to file
		FileLog.writeChat(serverCode, player, channel, rawMessage);
		
		//Check if logging to console
		if (MCNSAChat.plugin.getConfig().getBoolean("consoleLogChat")) {
			//Check for network message logging
			if (MCNSAChat.plugin.getConfig().getBoolean("consoleLogServers") && !serverCode.equals(MCNSAChat.shortCode) || serverCode.equals(MCNSAChat.shortCode))
				Bukkit.getConsoleSender().sendMessage((Colours.processConsoleColours(message)));
		}
		
		//See if need to send to other servers
		if (serverCode.equals(MCNSAChat.shortCode)) {
			if (ChannelManager.getChannel(channel) != null && !ChannelManager.getChannel(channel).modes.get("LOCAL") || ChannelManager.getChannel(channel) == null)
				Network.chatMessage(player, channel, rawMessage, "CHAT");
		}
	}
	public static void shadowActionMessage(String player, String rawMessage, String server, String channel)
	{
		String processedMessage = rawMessage;
		//Strip colours if needed
		if (!Permissions.useColours(player))
			processedMessage = Colours.stripColor(rawMessage);
		
		//Get the base message
		String message = MCNSAChat.plugin.getConfig().getString("strings.action");
		message = message.replace("%server%", server);
		
		//Support for channelcolours
		if (ChannelManager.getChannel(channel) != null)
			message = message.replace("%channel%", ChannelManager.getChannel(channel).color + ChannelManager.getChannel(channel).name);
		else
			message = message.replace("%channel%", channel);
		
		message = message.replace("%prefix%", Colours.PlayerPrefix(player));
		message = message.replace("%group%", Colours.PlayerGroup(player));
		message = message.replace("%player%", player);
		message = message.replace("%suffix%", Colours.PlayerSuffix(player));
		message = message.replace("%message%", processedMessage);

		send(Colours.processConsoleColours(message), player);
		//Log to file
		FileLog.writeChat(server, "*"+player, channel+"[SHADOW-MUTED]", rawMessage);
		
		//Check if logging to console
		if (MCNSAChat.plugin.getConfig().getBoolean("consoleLogChat")) {
			//Check for network message logging
			if (MCNSAChat.plugin.getConfig().getBoolean("consoleLogServers") && !server.equals(MCNSAChat.shortCode) || server.equals(MCNSAChat.shortCode))
				Bukkit.getConsoleSender().sendMessage((Colours.processConsoleColours(message)));
		}
		
	}
	public static void actionMessage(String player, String rawMessage, String server, String channel) {
		String processedMessage = rawMessage;
		//Strip colours if needed
		if (!Permissions.useColours(player))
			processedMessage = Colours.stripColor(rawMessage);
		
		//Get the base message
		String message = MCNSAChat.plugin.getConfig().getString("strings.action");
		message = message.replace("%server%", server);
		
		//Support for channelcolours
		if (ChannelManager.getChannel(channel) != null)
			message = message.replace("%channel%", ChannelManager.getChannel(channel).color + ChannelManager.getChannel(channel).name);
		else
			message = message.replace("%channel%", channel);
		
		message = message.replace("%prefix%", Colours.PlayerPrefix(player));
		message = message.replace("%group%", Colours.PlayerGroup(player));
		message = message.replace("%player%", player);
		message = message.replace("%suffix%", Colours.PlayerSuffix(player));
		message = message.replace("%message%", processedMessage);
		
		ArrayList<ChatPlayer> players = ChannelManager.getPlayersListening(channel);
		if (players != null) {
			for (ChatPlayer sendPlayer: players) {
				if (!sendPlayer.server.equals(MCNSAChat.shortCode))
					continue;
				//Check if the sending player is muted by the player recieving the message
				if (!sendPlayer.muted.contains(player)) {
					send(Colours.processConsoleColours(message), sendPlayer.name);
				}
			}
		}
		
		//Log to file
		FileLog.writeChat(server, "*"+player, channel, rawMessage);
		
		//Check if logging to console
		if (MCNSAChat.plugin.getConfig().getBoolean("consoleLogChat")) {
			//Check for network message logging
			if (MCNSAChat.plugin.getConfig().getBoolean("consoleLogServers") && !server.equals(MCNSAChat.shortCode) || server.equals(MCNSAChat.shortCode))
				Bukkit.getConsoleSender().sendMessage((Colours.processConsoleColours(message)));
		}
		
		//See if need to send to other servers
		if (server.equals(MCNSAChat.shortCode)) {
			if (ChannelManager.getChannel(channel) != null && !ChannelManager.getChannel(channel).modes.get("LOCAL")|| ChannelManager.getChannel(channel) == null)
				Network.chatMessage(player, channel, rawMessage, "ACTION");
		}
	}
	public static void timeoutPlayer(String player, String time, String reason) {
		if (reason.length() < 1)
			reason = "annoying a mod";
		//Get base string
		String notifyMessage = MCNSAChat.plugin.getConfig().getString("strings.timeout-player");
		notifyMessage = notifyMessage.replace("%time%", time);
		
		String reasonMessage = MCNSAChat.plugin.getConfig().getString("strings.timeout-reason");
		reasonMessage = reasonMessage.replace("%reason%", reason);
		
		MessageSender.send(notifyMessage, player);
		MessageSender.send(reasonMessage, player);
		
		//send to everyone in the players channel
		String playernotify = MCNSAChat.plugin.getConfig().getString("strings.timeout-players");
		playernotify = playernotify.replace("%prefix%", Colours.PlayerPrefix(player));
		playernotify = playernotify.replace("%player%", player);
		playernotify = playernotify.replace("%time%", time);
		
		ArrayList<ChatPlayer> players = ChannelManager.getPlayersListening(PlayerManager.getPlayer(player).channel);
		if (players != null) {
			for (ChatPlayer sendPlayer: players) {
				if (!sendPlayer.server.equals(MCNSAChat.shortCode))
					continue;
				//Check if the sending player is the player in timeout
				if (!sendPlayer.name.equalsIgnoreCase(player)) {
					
					MessageSender.send(playernotify, sendPlayer.name);
					MessageSender.send(reasonMessage, sendPlayer.name);
				}
			}
		}
		
		//log to console
		Bukkit.getConsoleSender().sendMessage(Colours.processConsoleColours(playernotify + " "+ reasonMessage));
	}
	public static void consoleChat(String rawMessage, String channel) {
		// used for console to send messages to a channel
		String message = MCNSAChat.plugin.getConfig().getString("strings.message");
		message = message.replace("%server%", MCNSAChat.shortCode);

		//Support for channelcolours
		if (ChannelManager.getChannel(channel) != null)
			message = message.replace("%channel%", ChannelManager.getChannel(channel).color + ChannelManager.getChannel(channel).name);
		else
			message = message.replace("%channel%", channel);
		
		message = message.replace("%prefix%", MCNSAChat.plugin.getConfig().getString("consoleSender-colour"));
		message = message.replace("%player%", MCNSAChat.plugin.getConfig().getString("consoleSender"));
		message = message.replace("%message%", rawMessage);
		message = message.replace("%suffix%", "");
		
		for (Player player: Bukkit.getOnlinePlayers()) {
			ChatPlayer chatPlayer = PlayerManager.getPlayer(player.getName(), MCNSAChat.shortCode);
			//Sanity check
			if (chatPlayer != null) {
				if (chatPlayer.channel.equalsIgnoreCase(channel) || chatPlayer.listening.contains(channel.toLowerCase()) || Permissions.getForceListens(chatPlayer.name).contains(channel) ||chatPlayer.modes.get("SEEALL"))
					send(Colours.processConsoleColours(message), player.getName());
			}
		}
		//Log to file
		FileLog.writeChat(MCNSAChat.shortCode, "["+MCNSAChat.plugin.getConfig().getString("consoleSender")+"]", channel, rawMessage);
		
		Bukkit.getConsoleSender().sendMessage(Colours.processConsoleColours(message));
	}
}
