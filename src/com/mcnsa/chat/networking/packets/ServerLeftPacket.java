package com.mcnsa.chat.networking.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ServerLeftPacket implements BasePacket {
	public static short id = 2;
	public String serverName = null;
	
	public ServerLeftPacket() {
	}
	
	public ServerLeftPacket(String shortCode){
		this.serverName = shortCode;
	}
	
	public void write(DataOutputStream out) throws IOException {
		out.writeInt(id);
		out.writeUTF(serverName);
		out.flush();
	}
	
	public void read(DataInputStream in) throws IOException {
		this.serverName = in.readUTF();
	}
}
