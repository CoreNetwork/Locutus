package com.mcnsa.chat.networking.packets;

import java.util.ArrayList;

import com.mcnsa.chat.plugin.MCNSAChat;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.type.ChatPlayer;

public class ServerJoined {
	public String serverName;
	public String shortCode;
	public String passcode;
	public ArrayList<ChatPlayer> players;
	
	public ServerJoined() {
		this.serverName = MCNSAChat.plugin.serverName;
		this.shortCode = MCNSAChat.plugin.shortCode;
		this.passcode = MCNSAChat.plugin.getConfig().getString("chatServerPassword");
		this.players = PlayerManager.players;
	}
}
