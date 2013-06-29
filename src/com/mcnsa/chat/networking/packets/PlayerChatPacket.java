package com.mcnsa.chat.networking.packets;

import java.io.Serializable;

import com.mcnsa.chat.type.ChatPlayer;

public class PlayerChatPacket implements Serializable {

	private static final long serialVersionUID = -4334247452705226826L;
	public ChatPlayer player;
	public String serverShortCode;
	public String Channel;
	public String message;
	public String action;
	
	public PlayerChatPacket(ChatPlayer player, String serverSC, String channel, String message, String action){
		this.player = player;
		this.serverShortCode = serverSC;
		this.Channel = channel;
		this.message = message;
		this.action = action;
	}
}
