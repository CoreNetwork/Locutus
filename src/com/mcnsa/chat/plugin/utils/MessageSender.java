package com.mcnsa.chat.plugin.utils;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.mcnsa.chat.plugin.MCNSAChat;
import com.mcnsa.chat.plugin.managers.ChannelManager;
import com.mcnsa.chat.plugin.managers.Permissions;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.type.ChatPlayer;

public class MessageSender {
	public static void send(String message, String playerSender, String player) {
		//Function sends from one player to another
		Player playerRecieving = Bukkit.getPlayer(player);
		playerRecieving.sendMessage(message);
	}
	public static void joinMessage(String playerName, String server) {
		//Build the message
		String message = MCNSAChat.plugin.getConfig().getString("strings.player-join");
		message = message.replaceAll("%server%", server);
		message = message.replaceAll("%prefix%", Colours.PlayerPrefix(playerName));
		message = message.replaceAll("%player%", playerName);
		Bukkit.broadcastMessage(Colours.processConsoleColours(message));
	}
	public static void quitMessage(String playerName, String server) {
		//Build the message
		String message = MCNSAChat.plugin.getConfig().getString("strings.player-quit");
		message = message.replaceAll("%server%", server);
		message = message.replaceAll("%prefix%", Colours.PlayerPrefix(playerName));
		message = message.replaceAll("%player%", playerName);
		Bukkit.broadcastMessage(Colours.processConsoleColours(message));
	}
	public static void sendChannel(String playerName, String chatMessage){
		ChatPlayer player = PlayerManager.getPlayer(playerName, MCNSAChat.plugin.shortCode);
		String channel = player.channel;
		
		if (!Permissions.useColours(player.name)) {
			chatMessage = Colours.stripColor(chatMessage);
		}
		else {
			chatMessage = Colours.color(chatMessage);
		}
		ArrayList<ChatPlayer> players = ChannelManager.getPlayersListening(channel);
		for (ChatPlayer sendPlayer: players) {
			//Check if the sending player is muted by the player recieving the message
			if (!sendPlayer.muted.contains(playerName)) {
				//Build the message
				String message = MCNSAChat.plugin.getConfig().getString("strings.message");
				message = message.replaceAll("%server%", MCNSAChat.plugin.shortCode);
				message = message.replaceAll("%channel%", channel);
				message = message.replaceAll("%prefix%", Colours.PlayerPrefix(playerName));
				message = message.replaceAll("%player%", playerName);
				message = message.replaceAll("%message%", chatMessage);
				Bukkit.getPlayer(sendPlayer.name).sendMessage(Colours.processConsoleColours(message));
			}
		}
	}
	public static void send(String message, String player) {
		Player playerRecieving = Bukkit.getPlayer(player);
		playerRecieving.sendMessage(Colours.color(message));
	}
}
