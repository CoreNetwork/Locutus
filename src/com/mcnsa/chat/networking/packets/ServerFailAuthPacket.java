package com.mcnsa.chat.networking.packets;

import java.io.Serializable;

public class ServerFailAuthPacket implements Serializable {

	private static final long serialVersionUID = 1686403778947742603L;
	public int id = 002;
	public String servername;
	
	public ServerFailAuthPacket(String serverName){
		this.servername = serverName;
	}
}
