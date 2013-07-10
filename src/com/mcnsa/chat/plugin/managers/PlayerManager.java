package com.mcnsa.chat.plugin.managers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.mcnsa.chat.networking.Network;
import com.mcnsa.chat.plugin.MCNSAChat;
import com.mcnsa.chat.plugin.utils.MessageSender;
import com.mcnsa.chat.type.ChatPlayer;

public class PlayerManager {
	public static ArrayList<ChatPlayer> players;
	
	public PlayerManager() {
		players = new ArrayList<ChatPlayer>();
	}
	
	public static void PlayerLogin(String player){
		if (PlayerManager.getPlayer(player, MCNSAChat.shortCode) == null) {
			ChatPlayer newPlayer= new ChatPlayer(player);
			players.add(newPlayer);
			MCNSAChat.console.info("Added "+player);
		}
	}
	public static void PlayerLogout(String player){
		ChatPlayer cplayer = getPlayer(player, MCNSAChat.shortCode);
		cplayer.savePlayer();
		//Notify network
		Network.playerQuit(cplayer);
		removePlayer(player, MCNSAChat.shortCode);
	}
	public static void removePlayer(String name, String server) {
	    for (int i = 0; i < players.size(); i++) {
	      ChatPlayer play = (ChatPlayer)players.get(i);
	      if (play.name.equalsIgnoreCase(name) && play.server.equalsIgnoreCase(server)) {
	        players.remove(play);
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
	public static ChatPlayer getPlayer(String name, String server) {
		for (ChatPlayer player: players) {
			if (player.name.equalsIgnoreCase(name) && player.server.equals(server)){
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
			if (players.get(i).name.toLowerCase().startsWith(string.toLowerCase()) && !results.contains(players.get(i))) {
				results.add(players.get(i));
			}
		}
		return results;
	}
	public static void unmutePlayer(String Player) {
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i).name.equalsIgnoreCase(Player)) {
				players.get(i).modes.put("MUTE", false);
				
				//Notify player
				MessageSender.send("&6You have been removed from timeout", Player);
				//UpdatePlayers on network
				Network.updatePlayer(players.get(i));
			}
		}
	}
	public static void mutePlayer(String Player, String time, String reason) {
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i).name.equalsIgnoreCase(Player)) {
				players.get(i).modes.put("MUTE", true);
				players.get(i).timeoutTill = new Date().getTime() + (Integer.valueOf(time) * 60000);
				//Inform the player
				if (players.get(i).server.equals(MCNSAChat.shortCode))
					MessageSender.timeoutPlayer(Player, time, reason);
				//UpdatePlayers on network
				Network.updatePlayer(players.get(i));
			}
		}
	}

	public static void updatePlayer(ChatPlayer player) {
		PlayerManager.players.remove(PlayerManager.getPlayer(player.name, player.server));
		PlayerManager.players.add(player);
	}

	public static void addPlayers(ArrayList<ChatPlayer> players2) {
		for (ChatPlayer play: players2) {
			if (getPlayer(play.name, play.server) == null)
				players.add(play);
		}
		
	}

	public static void removeNonServerPlayers() {
		// TODO Auto-generated method stub
		ArrayList<ChatPlayer> newPlayers = new ArrayList<ChatPlayer>();
		Player[] bukkitPlayers = Bukkit.getOnlinePlayers();
		for (Player player: bukkitPlayers) {
			newPlayers.add(getPlayer(player.getName(), MCNSAChat.shortCode));
		}
		players = newPlayers;
	}
}
