package com.mcnsa.chat.plugin.utils;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.mcnsa.chat.networking.Network;
import com.mcnsa.chat.plugin.MCNSAChat;
import com.mcnsa.chat.plugin.managers.ChannelManager;
import com.mcnsa.chat.plugin.managers.Permissions;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.type.ChatChannel;
import com.mcnsa.chat.type.ChatPlayer;

public class MessageSender {
	public static void joinMessage(String playerName, PlayerJoinEvent event) {
		//Build the message
		String message = MCNSAChat.plugin.getConfig().getString("strings.player-join");
		message = message.replaceAll("%server%", MCNSAChat.serverName);
		message = message.replaceAll("%prefix%", Colours.PlayerPrefix(playerName));
		message = message.replaceAll("%player%", playerName);
		//Notify console
		MCNSAChat.console.info(Colours.processConsoleColours(message));
		//Set the join message
		event.setJoinMessage(Colours.processConsoleColours(message));
	}
	public static void quitMessage(String playerName, PlayerQuitEvent event) {
		//Build the message
		String message = MCNSAChat.plugin.getConfig().getString("strings.player-quit");
		message = message.replaceAll("%server%", MCNSAChat.serverName);
		message = message.replaceAll("%prefix%", Colours.PlayerPrefix(playerName));
		message = message.replaceAll("%player%", playerName);
		
		//Notify console
		MCNSAChat.console.info(Colours.processConsoleColours(message));
		//Set quit message
		event.setQuitMessage(Colours.processConsoleColours(message));
	}
	public static void joinMessage(ChatPlayer player, String server) {
		//Build the message
		String message = MCNSAChat.plugin.getConfig().getString("strings.player-join");
		message = message.replaceAll("%server%", server);
		message = message.replaceAll("%prefix%", Colours.PlayerPrefix(player.name));
		message = message.replaceAll("%player%", player.name);
		
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
		for (ChatPlayer reciever: sendTo) {
			//Get the bukkit player
			Player to = Bukkit.getPlayer(reciever.name);
			if (to != null)
				to.sendMessage(Colours.processConsoleColours(message));
		}
		
	}
	public static void quitMessage(ChatPlayer player, String server) {
		//Build the message
		String message = MCNSAChat.plugin.getConfig().getString("strings.player-quit");
		message = message.replaceAll("%server%", server);
		message = message.replaceAll("%prefix%", Colours.PlayerPrefix(player.name));
		message = message.replaceAll("%player%", player.name);
		
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
		for (ChatPlayer reciever: sendTo) {
			//Get the bukkit player
			Player to = Bukkit.getPlayer(reciever.name);
			if (to != null)
				to.sendMessage(Colours.processConsoleColours(message));
		}
	}
	public static void sendPM(String rawMessage, String sender, String target) {
		if (sender.equalsIgnoreCase("console")) {
			MCNSAChat.console.pm_send(target, rawMessage);
		}
		else {
			//Function sends the message back to the player sending the pm
			String message = MCNSAChat.plugin.getConfig().getString("strings.pm_send");
			message = message.replaceAll("%prefix%", Colours.PlayerPrefix(sender));
			message = message.replaceAll("%from%", sender);
			message = message.replaceAll("%to%", target);
			message = message.replaceAll("%message%", rawMessage);
			Bukkit.getPlayer(sender).sendMessage(Colours.processConsoleColours(message));
			
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
			message = message.replaceAll("%prefix%", Colours.PlayerPrefix(sender));
			message = message.replaceAll("%from%", sender);
			message = message.replaceAll("%to%", target);
			message = message.replaceAll("%message%", rawMessage);
			
			//Check if the target has muted the sender
			if (!PlayerManager.getPlayer(target).muted.contains(sender) && Bukkit.getPlayer(target) != null) {
				Bukkit.getPlayer(target).sendMessage(Colours.processConsoleColours(message));
				
				//Set the targets last pm
				PlayerManager.getPlayer(target).lastPm = sender;
				
				//update player
				Network.updatePlayer(PlayerManager.getPlayer(target));
			}
		}
	}
	public static void send(String message, String player) {
		Player playerRecieving = Bukkit.getPlayer(player);
		if (playerRecieving != null) {
			playerRecieving.sendMessage(Colours.processConsoleColours(message));
		}
		else {
			//send to console
			Bukkit.getConsoleSender().sendMessage(Colours.processConsoleColours(message));
		}
	}
	public static void broadcast(String message) {
		for (Player player: Bukkit.getOnlinePlayers()) {
			player.sendMessage(Colours.processConsoleColours(message));
		}
		
	}
	public static void channelMessage(String channel, String serverCode, String player, String rawMessage) {
		//Strip colour if no permissions
		if (!Permissions.useColours(player))
			rawMessage = Colours.stripColor(rawMessage);
		
		//Channel modes
		if (ChannelManager.getChannel(channel) != null) {
			ChatChannel chan = ChannelManager.getChannel(channel);
			
			if (chan.modes.get("RAVE")) {
				//Colourise the message
				rawMessage = Colours.raveColor(rawMessage);
			}
			else if (chan.modes.get("BORING")) {
				//Strip all colour
				rawMessage = Colours.stripColor(rawMessage);
			}
		}
		//Get the base message
		String message = MCNSAChat.plugin.getConfig().getString("strings.message");
		message = message.replaceAll("%server%", serverCode);
		message = message.replaceAll("%channel%", channel);
		message = message.replaceAll("%prefix%", Colours.PlayerPrefix(player));
		message = message.replaceAll("%player%", player);
		message = message.replaceAll("%message%", rawMessage);
		
		ArrayList<ChatPlayer> players = ChannelManager.getPlayersListening(channel);
		if (players != null) {
			for (ChatPlayer sendPlayer: players) {
				if (!sendPlayer.server.equals(MCNSAChat.shortCode))
					continue;
				//Check if the sending player is muted by the player recieving the message
				if (!sendPlayer.muted.contains(player)) {
					Bukkit.getPlayer(sendPlayer.name).sendMessage(Colours.processConsoleColours(message));
				}
			}
		}
		//Log to file
		FileLog.writeChat(rawMessage);
		
		//Check if logging to console
		if (MCNSAChat.plugin.getConfig().getBoolean("consoleLogChat")) {
			//Check for network message logging
			if (MCNSAChat.plugin.getConfig().getBoolean("consoleLogServers") && !serverCode.equals(MCNSAChat.shortCode) || serverCode.equals(MCNSAChat.shortCode))
				Bukkit.getConsoleSender().sendMessage((Colours.processConsoleColours(message)));
		}
		
		//See if need to send to other servers
		if (serverCode.equals(MCNSAChat.shortCode)) {
			if (ChannelManager.getChannel(channel) != null && !ChannelManager.getChannel(channel).modes.get("LOCAL") || ChannelManager.getChannel(channel) == null)
				Network.chatMessage(PlayerManager.getPlayer(player), channel, rawMessage, "CHAT");
		}
	}
	public static void actionMessage(ChatPlayer player, String rawMessage) {
		//Strip colours if needed
		if (!Permissions.useColours(player.name))
			rawMessage = Colours.stripColor(rawMessage);
		//Get the base message
		String message = MCNSAChat.plugin.getConfig().getString("strings.action");
		message = message.replaceAll("%server%", player.server);
		message = message.replaceAll("%channel%", player.channel);
		message = message.replaceAll("%prefix%", Colours.PlayerPrefix(player.name));
		message = message.replaceAll("%player%", player.name);
		message = message.replaceAll("%message%", rawMessage);
		
		ArrayList<ChatPlayer> players = ChannelManager.getPlayersListening(player.channel);
		if (players != null) {
			for (ChatPlayer sendPlayer: players) {
				MCNSAChat.console.info("Loop through player: "+player.name);
				if (!sendPlayer.server.equals(MCNSAChat.shortCode))
					continue;
				//Check if the sending player is muted by the player recieving the message
				if (!sendPlayer.muted.contains(player)) {
					
					Bukkit.getPlayer(sendPlayer.name).sendMessage(Colours.processConsoleColours(message));
				}
			}
		}
		
		//Log to file
		FileLog.writeChat(rawMessage);
		
		//Check if logging to console
		if (MCNSAChat.plugin.getConfig().getBoolean("consoleLogChat")) {
			//Check for network message logging
			if (MCNSAChat.plugin.getConfig().getBoolean("consoleLogServers") && !player.server.equals(MCNSAChat.shortCode) || player.server.equals(MCNSAChat.shortCode))
				Bukkit.getConsoleSender().sendMessage((Colours.processConsoleColours(message)));
		}
		
		//See if need to send to other servers
		if (player.server.equals(MCNSAChat.shortCode)) {
			if (ChannelManager.getChannel(player.channel) != null && !ChannelManager.getChannel(player.channel).modes.get("LOCAL")|| ChannelManager.getChannel(player.channel) == null)
				Network.chatMessage(player, player.channel, rawMessage, "ACTION");
		}
	}
	public static void timeoutPlayer(String player, String time, String reason) {
		//Get base string
		String notifyMessage = MCNSAChat.plugin.getConfig().getString("strings.timeout-player");
		notifyMessage = notifyMessage.replace("%time%", time);
		
		String reasonMessage = MCNSAChat.plugin.getConfig().getString("strings.timeout-reason");
		reasonMessage = reasonMessage.replace("%reason%", reason);
		
		MessageSender.send(notifyMessage, player);
		MessageSender.send(reasonMessage, player);
	}
	public static void consoleChat(String rawMessage, String channel) {
		// used for console to send messages to a channel
		String message = MCNSAChat.plugin.getConfig().getString("strings.message");
		message = message.replaceAll("%server%", MCNSAChat.shortCode);
		message = message.replaceAll("%channel%", channel);
		message = message.replaceAll("%prefix%", MCNSAChat.plugin.getConfig().getString("consoleSender-colour"));
		message = message.replaceAll("%player%", "["+MCNSAChat.plugin.getConfig().getString("consoleSender")+"]");
		message = message.replaceAll("%message%", rawMessage);
		
		ArrayList<ChatPlayer> players = ChannelManager.getPlayersListening(channel);
		if (players != null) {
			for (ChatPlayer sendPlayer: players) {
				if (!sendPlayer.server.equals(MCNSAChat.shortCode))
					continue;
				
				Bukkit.getPlayer(sendPlayer.name).sendMessage(Colours.processConsoleColours(message));
			}
		}
		//Log to file
		FileLog.writeChat(rawMessage);
		
		Bukkit.getConsoleSender().sendMessage((Colours.processConsoleColours(message)));
	}
}
