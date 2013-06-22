package com.mcnsa.chat.networking.packets;

import java.io.Serializable;

import com.mcnsa.chat.type.ChatPlayer;

public class PlayerQuitPacket implements Serializable{

	private static final long serialVersionUID = 4679247087707804961L;
	public ChatPlayer player;
	public String server;
	
	public PlayerQuitPacket(ChatPlayer player, String server) {
		this.player = player;
		this.server = server;
	}
}