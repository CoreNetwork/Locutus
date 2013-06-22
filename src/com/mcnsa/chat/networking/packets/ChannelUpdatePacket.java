package com.mcnsa.chat.networking.packets;

import java.io.Serializable;

import com.mcnsa.chat.type.ChatChannel;

public class ChannelUpdatePacket implements Serializable{

	private static final long serialVersionUID = -176054826447694136L;
	public ChatChannel channel;
	
	public ChannelUpdatePacket(ChatChannel channel) {
		this.channel = channel;
	}
}
