package com.mcnsa.chat.networking.packets;

import java.io.Serializable;

public class ServerLeftPacket implements Serializable {
	private static final long serialVersionUID = 5790139073678904397L;
	public String serverName;
	
	public ServerLeftPacket(String Server) {
		this.serverName = Server;
	}
}
