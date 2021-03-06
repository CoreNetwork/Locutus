package com.mcnsa.chat.networking.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.mcnsa.chat.type.ChatPlayer;

public class PlayerJoinedPacket implements BasePacket {
	public static short id = 5;
	public ChatPlayer player = null;
	public String server;
	
	public PlayerJoinedPacket() {
	}
	
	public PlayerJoinedPacket(ChatPlayer player, String server){
		this.player = player;
		this.server = server;
	}
	
	public void write(DataOutputStream out) throws IOException{
		out.writeInt(id);
		player.write(out);
		out.writeUTF(this.server);
		out.flush();
	}
	
	public void read(DataInputStream in) throws IOException {
		this.player = ChatPlayer.read(in);
		this.server = in.readUTF();
	}
}
