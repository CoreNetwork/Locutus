package com.mcnsa.chat.networking.packets;

import java.io.Serializable;
import java.util.ArrayList;

import com.mcnsa.chat.type.ChatChannel;

public class ChannelListPacket implements Serializable{

	private static final long serialVersionUID = -5191613572341954656L;
	public ArrayList<ChatChannel> channels;
	
	public ChannelListPacket(ArrayList<ChatChannel> channels){
		this.channels = channels;
	}
}
