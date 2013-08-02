package com.mcnsa.chat.networking.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class NetworkBroadcastPacket implements BasePacket {
	public static int id = 4;
	public String message = null;
	
	public NetworkBroadcastPacket() {
	}
	
	public NetworkBroadcastPacket(String message) {
		this.message = message;
	}
	
	public void write(DataOutputStream out) throws IOException {
		out.writeInt(id);
		out.writeUTF(message);
		out.flush();
	}
	
	public void read(DataInputStream in) throws IOException {
		this.message = in.readUTF();
	}
}
