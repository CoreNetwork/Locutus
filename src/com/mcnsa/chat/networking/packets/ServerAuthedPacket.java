package com.mcnsa.chat.networking.packets;

import java.io.Serializable;

public class ServerAuthedPacket implements Serializable {

	private static final long serialVersionUID = -464814065732095192L;
	public int id = 003;
	public String servername;
	
	public ServerAuthedPacket(String serverName) {
		this.servername = serverName;
	}
}
