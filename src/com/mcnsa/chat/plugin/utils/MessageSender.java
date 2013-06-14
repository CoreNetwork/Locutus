package com.mcnsa.chat.plugin.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.mcnsa.chat.plugin.managers.Permissions;

public class MessageSender {
	public void send(String message, String playerSender, String player) {
		//Function sends from one player to another
		Player playerRecieving = Bukkit.getPlayer(player);
		playerRecieving.sendMessage(message);
	}
}
