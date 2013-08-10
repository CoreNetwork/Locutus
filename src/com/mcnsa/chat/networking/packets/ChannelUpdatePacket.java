package com.mcnsa.chat.networking.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.mcnsa.chat.type.ChatChannel;

public class ChannelUpdatePacket implements BasePacket {
	
	public static short id = 8;
	public ChatChannel channel;
	
	public ChannelUpdatePacket(){
		
	}
	
	public ChannelUpdatePacket(ChatChannel channel) {
		this.channel = channel;
	}
	
	public void write(DataOutputStream out) throws IOException {
		out.writeInt(id);
		this.channel.write(out);
		out.flush();
	}
	
	public void read(DataInputStream in) throws IOException {
		this.channel = ChatChannel.read(in);
	}
}
