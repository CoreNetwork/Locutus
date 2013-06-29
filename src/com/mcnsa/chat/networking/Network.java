package com.mcnsa.chat.networking;

import com.mcnsa.chat.networking.packets.PlayerChatPacket;
import com.mcnsa.chat.networking.packets.PlayerJoinedPacket;
import com.mcnsa.chat.networking.packets.PlayerQuitPacket;
import com.mcnsa.chat.networking.packets.PlayerUpdatePacket;
import com.mcnsa.chat.networking.packets.PmPacket;
import com.mcnsa.chat.networking.packets.ServerLeftPacket;
import com.mcnsa.chat.plugin.MCNSAChat;
import com.mcnsa.chat.type.ChatChannel;
import com.mcnsa.chat.type.ChatPlayer;

public class Network {
	public static void chatMessage(ChatPlayer player, String channel, String message, String action) {
		if (MCNSAChat.network != null) {
			PlayerChatPacket packet = new PlayerChatPacket(player, player.server, channel, message, action);
			MCNSAChat.network.write(packet);
		}
	}
	public static void playerJoined(ChatPlayer player) {
		if (MCNSAChat.network != null) {
			PlayerJoinedPacket packet = new PlayerJoinedPacket(player, MCNSAChat.serverName);
			MCNSAChat.network.write(packet);
		}
	}
	public static void playerQuit(ChatPlayer player) {
		if (MCNSAChat.network != null) {
			PlayerQuitPacket packet = new PlayerQuitPacket(player, MCNSAChat.serverName);
			MCNSAChat.network.write(packet);
		}
	}
	public static void serverLeft() {
		if (MCNSAChat.network != null) {
			ServerLeftPacket packet = new ServerLeftPacket(MCNSAChat.serverName);
			MCNSAChat.network.write(packet);
		}
	}
	public static void updatePlayer(ChatPlayer player) {
		if (MCNSAChat.network != null) {
			PlayerUpdatePacket packet = new PlayerUpdatePacket(player);
			MCNSAChat.network.write(packet);
		}
	}
	public static void PmSend(String sender, String target, String message) {
		if (MCNSAChat.network != null) {
			PmPacket packet = new PmPacket(sender, target, message);
			MCNSAChat.network.write(packet);
		}
	}
	public static void actionMessage(ChatPlayer player, String message) {
		// TODO Auto-generated method stub
		
	}
	public static void channelUpdate(ChatChannel channel) {
		// TODO Auto-generated method stub
		
	}
}
