package com.mcnsa.chat.networking.packet;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.mcnsa.chat.type.ChatPlayer;

public class ServerJoinPacket {
	public int id = 001;
	public String serverName;
	public String shortCode;
	public String passcode;
	public ArrayList<ChatPlayer> players;
	
	public ServerJoinPacket(String serverName, String shortCode, String passCode, ArrayList<ChatPlayer> players) {
		this.serverName = serverName;
		this.shortCode = shortCode;
		this.passcode = passCode;
		this.players = players;
	}
	public void write(DataOutputStream out) throws IOException {
		out.writeInt(id);
		out.writeUTF(serverName);
		out.writeUTF(shortCode);
		out.writeInt(players.size());
	}
}
