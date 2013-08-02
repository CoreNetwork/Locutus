package com.mcnsa.chat.networking.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.mcnsa.chat.type.ChatPlayer;

public class PlayerTimeoutPacket implements BasePacket {
	public static int id = 11;
	public ChatPlayer player;
	public long time = 0;
	public String reason = null;
	
	public PlayerTimeoutPacket() {
		
	}
	
	public PlayerTimeoutPacket(ChatPlayer player, long time, String reason) {
		this.player = player;
		this.time = time;
		this.reason = reason;
	}
	
	public PlayerTimeoutPacket(ChatPlayer player) {
		this.player = player;
	}
	
	public void write(DataOutputStream out) throws IOException {
		out.writeInt(id);
		out.writeUTF(reason);
		out.writeLong(time);
		this.player.write(out);
		out.flush();
	}
	
	public void read(DataInputStream in) throws IOException {
		this.reason = in.readUTF();
		this.time = in.readLong();
		this.player = ChatPlayer.read(in);
	}
}
