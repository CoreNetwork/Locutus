package com.mcnsa.chat.networking;

import com.mcnsa.chat.networking.packets.*;
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
	public static void updatePlayer(ChatPlayer player) {
		if (MCNSAChat.network != null) {
			PlayerUpdatePacket packet = new PlayerUpdatePacket(player);
			MCNSAChat.network.write(packet);
			MCNSAChat.console.info(packet.toString());
		}
	}
	public static void PmSend(ChatPlayer sender, String target, String message) {
		if (MCNSAChat.network != null) {
			PmPacket packet = new PmPacket(sender, target, message);
			MCNSAChat.network.write(packet);
		}
	}
	public static void channelUpdate(ChatChannel channel) {
		if (MCNSAChat.network != null) {
			ChannelUpdatePacket packet = new ChannelUpdatePacket(channel);
			MCNSAChat.console.info("Channels updated");
			MCNSAChat.network.write(packet);
			packet = null;
		}
	}
}
