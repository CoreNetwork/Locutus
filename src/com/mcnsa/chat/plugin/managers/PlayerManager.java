package com.mcnsa.chat.plugin.managers;

import java.util.ArrayList;
import java.util.List;

import com.mcnsa.chat.plugin.MCNSAChat;
import com.mcnsa.chat.type.ChatPlayer;

public class PlayerManager {
	public static ArrayList<ChatPlayer> players;
	
	public PlayerManager() {
		players = new ArrayList<ChatPlayer>();
	}
	
	public static void PlayerLogin(String player){
		ChatPlayer newPlayer= new ChatPlayer(player);
		players.add(newPlayer);
		MCNSAChat.console.info("Added "+player);
	}
	public static void PlayerLogout(String player){
		ChatPlayer cplayer = getPlayer(player);
		cplayer.savePlayer();
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
	public static ChatPlayer getPlayer(String name) {
		for (ChatPlayer player: players) {
			if (player.name.equalsIgnoreCase(name)){
				return player;
			}
		}
		return null;
	}
	public static List<ChatPlayer> getServerPlayers(String server) {
		List<ChatPlayer> serverPlayers = new ArrayList<ChatPlayer>();
		for (int i = 0; i < players.size(); i++) {
			ChatPlayer player = players.get(i);
			if (player.server.equals(server) && !serverPlayers.contains(player)) {
				serverPlayers.add(player);
			}
		}
		
		return serverPlayers;
	}

	public static ArrayList<ChatPlayer> playerSearch(String string) {
		ArrayList<ChatPlayer> results = new ArrayList<ChatPlayer>();
		for (int i = 0; i < players.size(); i++){
			if (players.get(i).name.startsWith(string) && !results.contains(players.get(i))) {
				results.add(players.get(i));
			}
		}
		return results;
	}
}
