package com.mcnsa.chat.networking.packets;

import java.io.Serializable;

public class NetworkBroadcastPacket implements Serializable{
	private static final long serialVersionUID = 7492165104092530908L;
	public int id = 004;
	public String message;

	public NetworkBroadcastPacket(String Message) {
		this.message = Message;
	}
}
