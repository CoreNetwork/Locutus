package com.mcnsa.chat.networking.packets;

public class ChatPacket {
	public String player;
	public String serverCode;
	public String channel;
	public String message;
	
	public ChatPacket(String player, String serverCode, String channel, String message){
		this.player = player;
		this.serverCode = serverCode;
		this.channel = channel;
		this.message = message;
	}
}
