package com.mcnsa.chat.networking.packets;

import java.io.Serializable;
import java.util.ArrayList;

import com.mcnsa.chat.type.ChatPlayer;

public class PlayerListPacket implements Serializable{

	private static final long serialVersionUID = 2141594001332880031L;
	public ArrayList<ChatPlayer> players;
	public String server;
	
	public PlayerListPacket(ArrayList<ChatPlayer> players, String server) {
		this.players = players;
		this.server = server;
	}
	
}
