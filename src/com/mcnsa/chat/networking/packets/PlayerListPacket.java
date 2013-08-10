package com.mcnsa.chat.networking.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.mcnsa.chat.type.ChatPlayer;

public class PlayerListPacket implements BasePacket {
	public static short id = 12;
	public ArrayList<ChatPlayer> players = new ArrayList<ChatPlayer>();
	
	public PlayerListPacket() {
		
	}
	
	public PlayerListPacket(ArrayList<ChatPlayer> players) {
		this.players = players;
	}
	
	public void write(DataOutputStream out) throws IOException {
		out.writeInt(id);
		out.writeInt(players.size());
		for (ChatPlayer player: players) {
			player.write(out);
		}
		out.flush();
	}
	
	public void read(DataInputStream in) throws IOException {
		int num = in.readInt();
		for (int i = 0; i < num; i++) {
			this.players.add(ChatPlayer.read(in));
		}
	}
	
}
