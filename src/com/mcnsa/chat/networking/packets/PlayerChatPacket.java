package com.mcnsa.chat.networking.packets;

import java.io.Serializable;

public class PlayerChatPacket implements Serializable {

	private static final long serialVersionUID = -4334247452705226826L;
	public String playername;
	public String serverShortCode;
	public String Channel;
	public String message;
	
	public PlayerChatPacket(String player, String serverSC, String channel, String message){
		this.playername = player;
		this.serverShortCode = serverSC;
		this.Channel = channel;
		this.message = message;
	}
}
