package com.mcnsa.chat.networking.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.mcnsa.chat.type.ChatChannel;

public class ChannelListingPacket implements BasePacket {
	public static int id = 3;
	public ArrayList<ChatChannel> channels;
	
	public ChannelListingPacket() {
	}
	
	public ChannelListingPacket(ArrayList<ChatChannel> channels) {
		this.channels = channels;
	}
	
	public void write(DataOutputStream out) throws IOException{
		out.writeInt(id);
		out.writeInt(channels.size());
		for (ChatChannel channel: channels) {
			channel.write(out);
		}
		out.flush();
	}
	public void read(DataInputStream in) throws IOException {
		channels = new ArrayList<ChatChannel>();
		int num = in.readInt();
		for(int i = 0; i < num; i++)
			channels.add(ChatChannel.read(in));
	}
}
