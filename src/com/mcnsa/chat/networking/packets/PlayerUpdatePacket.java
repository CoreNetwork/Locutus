package com.mcnsa.chat.networking.packets;

import java.io.Serializable;

import com.mcnsa.chat.type.ChatPlayer;

public class PlayerUpdatePacket implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6577498526159507045L;
	public ChatPlayer player;
	
	public PlayerUpdatePacket(ChatPlayer newplayer) {
		player = newplayer;
	}
}
