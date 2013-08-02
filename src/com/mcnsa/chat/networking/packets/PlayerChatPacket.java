package com.mcnsa.chat.networking.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PlayerChatPacket implements BasePacket {
	public static int id = 7;
	public String player;
	public String channel;
	public String message;
	public String type;
	public String server;
	
	public PlayerChatPacket() {
	}
	
	public PlayerChatPacket(String player, String channel, String message, String type, String server) {
		this.player = player;
		this.channel = channel;
		this.message = message;
		this.type = type;
		this.server = server;
	}
	
	public void write(DataOutputStream out) throws IOException {
		out.writeInt(id);
		out.writeUTF(player);
		out.writeUTF(channel);
		out.writeUTF(message);
		out.writeUTF(type);
		out.writeUTF(server);
		out.flush();
	}
	
	public void read(DataInputStream in) throws IOException{
		this.player = in.readUTF();
		this.channel = in.readUTF();
		this.message = in.readUTF();
		this.type = in.readUTF();
		this.server = in.readUTF();
	}
}
