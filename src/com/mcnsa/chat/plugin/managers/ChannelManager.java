package com.mcnsa.chat.plugin.managers;

import java.util.ArrayList;

import com.mcnsa.chat.plugin.MCNSAChat;
import com.mcnsa.chat.type.ChatChannel;

public class ChannelManager {
	public static ArrayList<ChatChannel> channels = new ArrayList<ChatChannel>();
	private MCNSAChat plugin;
	
	public ChannelManager(){
		this.plugin = MCNSAChat.plugin;
	}
	public void removeChannel(String chan) {
		channels.remove(getChannel(chan));
	}
	public ChatChannel getChannel(String chan){
		for (int i = 0; i < channels.size(); i++){
			ChatChannel channel = channels.get(i);
			if (channel.name.equalsIgnoreCase(chan)) {
				return channel;
			}
		}
		return null;
	}
}
