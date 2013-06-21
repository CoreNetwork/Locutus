package com.mcnsa.chat.networking.packets;

import java.io.Serializable;
import java.util.ArrayList;

import com.mcnsa.chat.plugin.MCNSAChat;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.type.ChatPlayer;

public class ServerJoinedPacket implements Serializable{

	private static final long serialVersionUID = 5049728612844186804L;
	public int id = 001;
	public String serverName;
	public String shortCode;
	public String passcode;
	public ArrayList<ChatPlayer> players;
	
	public ServerJoinedPacket() {
		this.serverName = MCNSAChat.serverName;
		this.shortCode = MCNSAChat.shortCode;
		this.passcode = MCNSAChat.plugin.getConfig().getString("chatServerPassword");
		this.players = PlayerManager.players;
	}
}
