package com.mcnsa.chat.networking.newPackets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.mcnsa.chat.type.ChatPlayer;

public class ServerJoinedPacket {
	public int id = 001;
	public String serverName;
	public String serverShortCode;
	public String passcode;
	public ArrayList<ChatPlayer> players;
	
	public ServerJoinedPacket() {
	}
	
	public ServerJoinedPacket(String ServerName, String shortCode, String passcode, ArrayList<ChatPlayer> players) {
		this.serverName = ServerName;
		this.serverShortCode = shortCode;
		this.passcode = passcode;
		this.players = players;
	}
	
	public void write(DataOutputStream out) throws IOException{
		out.writeInt(this.id);
		out.writeUTF(this.serverName);
		out.writeUTF(this.serverShortCode);
		out.writeUTF(this.passcode);
		if (players == null) {
			out.writeInt(0);
		}
		else {
			out.writeInt(players.size());
			for (ChatPlayer player: players){
				player.write(out);
			}
		}
		
		out.flush();
	}
	
	public void read(DataInputStream in) throws IOException{
		this.serverName = in.readUTF();
		this.serverShortCode = in.readUTF();
		this.passcode = in.readUTF();
		int playerNumbers = in.readInt();
		
		for (int i = 0; i < playerNumbers; i++){
			players.add(ChatPlayer.read(in));
		}
	}
}
