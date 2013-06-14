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
	public static void joinQuitMessage(String message) {
		//Send to players first
		Bukkit.broadcastMessage(message);
	}
	public static void sendChannel(String playerName, String chatMessage){
		ChatPlayer player = PlayerManager.getPlayer(playerName, MCNSAChat.plugin.shortCode);
		String channel = player.channel;
		
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
}
