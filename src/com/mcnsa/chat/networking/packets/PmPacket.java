package com.mcnsa.chat.networking.packets;

import java.io.Serializable;

import com.mcnsa.chat.type.ChatPlayer;

public class PmPacket implements Serializable{

	private static final long serialVersionUID = 6625983951605179888L;
	public ChatPlayer sender;
	public String target;
	public String message;
	
	public PmPacket(ChatPlayer sender, String target, String message) {
		this.sender = sender;
		this.target = target;
		this.message = message;
	}
}
