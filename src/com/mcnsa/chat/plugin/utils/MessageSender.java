package com.mcnsa.chat.plugin.utils;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.mcnsa.chat.plugin.MCNSAChat;
import com.mcnsa.chat.plugin.managers.ChannelManager;
import com.mcnsa.chat.plugin.managers.Permissions;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.type.ChatPlayer;

public class MessageSender {
	public static void sendmsg(String rawmessage, String playerSender, String player) {
		//Get the player
		ChatPlayer cPlayer = PlayerManager.getPlayer(player);
		
		//Check if player has muted playersender
		if (!cPlayer.muted.contains(playerSender)) {
			//Player is not muted 
			String message = MCNSAChat.plugin.getConfig().getString("strings.pm_receive");	
			message = message.replaceAll("%prefix%", Colours.PlayerPrefix(playerSender));
			message = message.replaceAll("%from%", playerSender);
			message = message.replaceAll("%to%", player);
			
			Bukkit.getPlayer(player).sendMessage(Colours.stripColor(message));
		}
	}
	public static void joinMessage(String playerName, String server, PlayerJoinEvent event) {
		//Build the message
		String message = MCNSAChat.plugin.getConfig().getString("strings.player-join");
		message = message.replaceAll("%server%", server);
		message = message.replaceAll("%prefix%", Colours.PlayerPrefix(playerName));
		message = message.replaceAll("%player%", playerName);
		//Notify console
		MCNSAChat.plugin.console.info(Colours.processConsoleColours(message));
		//Set the join message
		event.setJoinMessage(Colours.processConsoleColours(message));
	}
	public static void quitMessage(String playerName, String server, PlayerQuitEvent event) {
		//Build the message
		String message = MCNSAChat.plugin.getConfig().getString("strings.player-quit");
		message = message.replaceAll("%server%", server);
		message = message.replaceAll("%prefix%", Colours.PlayerPrefix(playerName));
		message = message.replaceAll("%player%", playerName);
		
		//Notify console
		MCNSAChat.plugin.console.info(Colours.processConsoleColours(message));
		//Set quit message
		event.setQuitMessage(Colours.processConsoleColours(message));
	}
	public static void joinMessage(String playerName, String server) {
		//Build the message
		String message = MCNSAChat.plugin.getConfig().getString("strings.player-join");
		message = message.replaceAll("%server%", server);
		message = message.replaceAll("%prefix%", Colours.PlayerPrefix(playerName));
		message = message.replaceAll("%player%", playerName);
		//Notify everyone
		Bukkit.broadcastMessage(Colours.processConsoleColours(message));
	}
	public static void quitMessage(String playerName, String server) {
		//Build the message
		String message = MCNSAChat.plugin.getConfig().getString("strings.player-quit");
		message = message.replaceAll("%server%", server);
		message = message.replaceAll("%prefix%", Colours.PlayerPrefix(playerName));
		message = message.replaceAll("%player%", playerName);
		
		//Notify everyone
		Bukkit.broadcastMessage(Colours.processConsoleColours(message));
	}
	public static void sendChannel(String playerName, String chatMessage){
		ChatPlayer player = PlayerManager.getPlayer(playerName);
		String channel = player.channel;
		
		if (!Permissions.useColours(player.name)) {
			chatMessage = Colours.stripColor(chatMessage);
		}
		else {
			chatMessage = Colours.color(chatMessage);
		}
		//Build the message
		String message = MCNSAChat.plugin.getConfig().getString("strings.message");
		message = message.replaceAll("%server%", MCNSAChat.plugin.shortCode);
		message = message.replaceAll("%channel%", channel);
		message = message.replaceAll("%prefix%", Colours.PlayerPrefix(playerName));
		message = message.replaceAll("%player%", playerName);
		message = message.replaceAll("%message%", chatMessage);
		
		ArrayList<ChatPlayer> players = ChannelManager.getPlayersListening(channel);
		for (ChatPlayer sendPlayer: players) {
			//Check if the sending player is muted by the player recieving the message
			if (!sendPlayer.muted.contains(playerName)) {
				Bukkit.getPlayer(sendPlayer.name).sendMessage(Colours.processConsoleColours(message));
			}
		}
		
		//Log to file
		FileLog.writeChat(Colours.stripColor(message));
		
		//Check if logging to console
		if (MCNSAChat.plugin.getConfig().getBoolean("consoleLogChat")) {
			//Check for network message logging
			if (MCNSAChat.plugin.getConfig().getBoolean("consoleLogServers") && !player.server.equals(MCNSAChat.plugin.shortCode) || player.server.equals(MCNSAChat.plugin.shortCode))
				Bukkit.getConsoleSender().sendMessage((Colours.processConsoleColours(message)));
		}
	}
	public static void send(String message, String player) {
		Player playerRecieving = Bukkit.getPlayer(player);
		playerRecieving.sendMessage(Colours.processConsoleColours(message));
	}
}
