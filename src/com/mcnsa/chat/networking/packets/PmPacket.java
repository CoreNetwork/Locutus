package com.mcnsa.chat.networking.packets;

import java.io.Serializable;

public class PmPacket implements Serializable{

	private static final long serialVersionUID = 6625983951605179888L;
	public String sender;
	public String target;
	public String message;
	
	public PmPacket(String sender, String target, String message) {
		this.sender = sender;
		this.target = target;
		this.message = message;
	}
}
