package com.mcnsa.chat.networking.packets;

import com.mcnsa.chat.type.ChatPlayer;

public class PlayerUpdatePacket {
	public ChatPlayer player;
	
	public PlayerUpdatePacket(ChatPlayer player) {
		this.player = player;
	}
}
