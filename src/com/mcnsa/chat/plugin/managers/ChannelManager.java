package com.mcnsa.chat.plugin.managers;

import java.util.ArrayList;

import com.mcnsa.chat.type.ChatChannel;
import com.mcnsa.chat.type.ChatPlayer;

public class ChannelManager {
	public static ArrayList<ChatChannel> channels = new ArrayList<ChatChannel>();
	public ChannelManager(){
	}
	public void removeChannel(String chan) {
		channels.remove(getChannel(chan));
	}
	public static ChatChannel getChannel(String chan){
		for (int i = 0; i < channels.size(); i++){
			ChatChannel channel = channels.get(i);
			if (channel.name.equalsIgnoreCase(chan)) {
				return channel;
			}
		}
		return null;
	}
	public static ArrayList<ChatPlayer> getPlayersListening(String channel){
		ArrayList<ChatPlayer> players = new ArrayList<ChatPlayer>();
		for (ChatPlayer player: PlayerManager.players) {
			if (player.channel.equalsIgnoreCase(channel)| player.listening.contains(channel)) {
				players.add(player);
			}
		}
		
		if (players.isEmpty())
			return null;
		
		return players;
		
	}
}
