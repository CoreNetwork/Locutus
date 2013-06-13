package com.mcnsa.chat.plugin.managers;

import java.util.ArrayList;

import com.mcnsa.chat.plugin.MCNSAChat;
import com.mcnsa.chat.type.ChatPlayer;

public class PlayerManager {
	public static ArrayList<ChatPlayer> players;
	private MCNSAChat plugin;
	
	public PlayerManager() {
		this.plugin = MCNSAChat.plugin;
		players = new ArrayList<ChatPlayer>();
	}
	
	public static void PlayerLogin(String player){
		ChatPlayer newPlayer= new ChatPlayer(player);
		players.add(newPlayer);
	}
	public static void PlayerLogout(String player){
		removePlayer(player, MCNSAChat.plugin.shortCode);
	}
	public static void removePlayer(String name, String server) {
	    for (int i = 0; i < players.size(); i++) {
	      ChatPlayer play = (ChatPlayer)players.get(i);
	      if ((play.name.equalsIgnoreCase(name)) && (play.server.equalsIgnoreCase(server))) {
	        players.remove(play);
	        break;
	      }
	    }
	}
	public static ChatPlayer getPlayer(String name, String server) {
		for (ChatPlayer player: players) {
			if (player.name.equalsIgnoreCase(name)){
				return player;
			}
		}
		return null;
	}
}
