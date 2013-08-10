package com.mcnsa.chat.networking.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.mcnsa.chat.type.ChatPlayer;

public class PlayerUpdatePacket implements BasePacket {
	public static short id = 9;
	public ChatPlayer player = null;
	public String server;
	
	public PlayerUpdatePacket(){
		
	}
	
	public PlayerUpdatePacket(ChatPlayer player, String server) {
		this.player = player;
		this.server = server;
	}
	
	public void write(DataOutputStream out) throws IOException {
		out.writeInt(id);
		out.writeUTF(this.server);
		this.player.write(out);
		out.flush();
	}
	
	public void read(DataInputStream in) throws IOException {
		this.server = in.readUTF();
		this.player = ChatPlayer.read(in);
	}
}
