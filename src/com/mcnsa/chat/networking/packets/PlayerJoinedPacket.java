package com.mcnsa.chat.networking.packets;

import java.io.Serializable;

import com.mcnsa.chat.type.ChatPlayer;

public class PlayerJoinedPacket implements Serializable{

	private static final long serialVersionUID = -2229794131987627502L;
	public ChatPlayer player;
	public String server;
	
	public PlayerJoinedPacket(ChatPlayer player, String server) {
		this.player = player;
		this.server = server;
	}
}
