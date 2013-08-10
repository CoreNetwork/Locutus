package com.mcnsa.chat.networking.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ServerAuthPacket implements BasePacket {
	public static short id = 13;
	public String status;
	
	public ServerAuthPacket() {
		
	}
	
	public ServerAuthPacket(String status) {
		this.status = status;
	}
	
	public void write(DataOutputStream out) throws IOException {
		out.writeInt(id);
		out.writeUTF(this.status);
		out.flush();
	}
	
	public void read(DataInputStream in) throws IOException {
		this.status = in.readUTF();
	}
}
