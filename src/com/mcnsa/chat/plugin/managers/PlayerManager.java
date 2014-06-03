package com.mcnsa.chat.plugin.managers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.chat.plugin.MCNSAChat;
import com.mcnsa.chat.plugin.utils.ConsoleLogging;
import com.mcnsa.chat.plugin.utils.MessageSender;
import com.mcnsa.chat.type.ChatPlayer;

public class PlayerManager {
	public static ArrayList<ChatPlayer> players;
	
	public PlayerManager() {
		players = new ArrayList<ChatPlayer>();
	}
	
	public static void PlayerLogin(UUID uuid){
		
		if (PlayerManager.getPlayer(uuid, MCNSAChat.shortCode) == null) {
			ChatPlayer newPlayer= new ChatPlayer(uuid);

			if (MCNSAChat.isLockdown && newPlayer.firstTime)
			{
				return;
			}
			players.add(newPlayer);
			if (PermissionManager.checkPermission("admin.notify", newPlayer.name) && MCNSAChat.isLockdown)
			{
				if (MCNSAChat.lockdownTimerID == 0)
				{

					String message = MCNSAChat.plugin.getConfig()
					.getString("strings.lockdown-login-persist");
					message.replace("%reason%", MCNSAChat.lockdownReason);
					MessageSender.send(message,newPlayer );
				}
				else
				{
					String message = MCNSAChat.plugin.getConfig()
					.getString("strings.lockdown-login-temp");
					
					long currentTime = new Date().getTime();
					long timeLeft = MCNSAChat.lockdownUnlockTime - currentTime;
					message = message.replace("%seconds%", String.valueOf(TimeUnit.MILLISECONDS.toSeconds(timeLeft) % 60));
					message = message.replace("%minutes%", String.valueOf(TimeUnit.MILLISECONDS.toMinutes(timeLeft) % 60));
					message = message.replace("%reason%", MCNSAChat.lockdownReason);
					MessageSender.send(message, uuid );
				}
			}
		}
	}
	public static void PlayerLogout(UUID uuid){
		ChatPlayer cplayer = getPlayer(uuid, MCNSAChat.shortCode);
		if (cplayer == null)
			return;
		if (!MCNSAChat.isLockdown)
			cplayer.savePlayer();
		removePlayer(uuid, MCNSAChat.shortCode);
	}
	public static void removePlayer(UUID uuid, String server) {
	    for (int i = 0; i < players.size(); i++) {
	      ChatPlayer play = (ChatPlayer)players.get(i);
	      if (play.getUUID().equals(uuid) && play.server.equalsIgnoreCase(server)) {
	        players.remove(play);
	      }
	    }
	}
	public static ChatPlayer getPlayer(UUID uuid) {
		for (ChatPlayer player: players) {
			if (player.getUUID().equals(uuid)){
				return player;
			}
		}
		return null;
	}
	public static ChatPlayer getPlayer(CommandSender commandSender) {
		if (commandSender instanceof Player){
			Player player = (Player) commandSender;
			return getPlayer(player.getUniqueId());
		} else {
			throw new IllegalArgumentException("Cannot convert from console to ChatPlayer");
		}
	}
	public static ChatPlayer getPlayer(UUID uuid, String server) {
		for (ChatPlayer player: players) {
			if (player.getUUID().equals(uuid) && player.server.equals(server)){
				return player;
			}
		}
		return null;
	}

	public static ChatPlayer getPlayer(CommandSender commandSender, String shortCode) {
		if (commandSender instanceof Player){
			Player player = (Player) commandSender;
			return getPlayer(player.getUniqueId(), shortCode);
		} else {
			throw new IllegalArgumentException("Cannot convert from console to ChatPlayer");
		}
	}
	
	public static UUID searchPlayerUUID(String playerName){
		return searchPlayers(playerName).get(0).getUUID();
	}
	public static ChatPlayer searchPlayer(String playerName){
		List<ChatPlayer> search = searchPlayers(playerName);
		if (search.size() == 0)
			return null;
		return search.get(0);
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

	public static ArrayList<ChatPlayer> searchPlayers(String string) {
		ArrayList<ChatPlayer> results = new ArrayList<ChatPlayer>();
		for (int i = 0; i < players.size(); i++){
			if (players.get(i).name.toLowerCase().startsWith(string.toLowerCase()) && !results.contains(players.get(i))) {
				results.add(players.get(i));
			}
		}
		return results;
	}
	public static void unmutePlayer(UUID uuid) {
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i).getUUID().equals(uuid)) {
				players.get(i).modes.put("MUTE", false);
				
				//Notify player
				MessageSender.send("&6You have been removed from timeout", uuid);
			}
		}
	}
	public static void unmutePlayer(Player p) {
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i).getUUID().equals(p.getUniqueId())) {
				players.get(i).modes.put("MUTE", false);
				
				//Notify player
				MessageSender.send("&6You have been removed from timeout", p);
			}
		}
	}
	public static void unmutePlayer(ChatPlayer p) {
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i).equals(p)) {
				players.get(i).modes.put("MUTE", false);
				
				//Notify player
				MessageSender.send("&6You have been removed from timeout", p);
			}
		}
	}
	public static void shadowUnmutePlayer(UUID uuid) {
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i).getUUID().equals(uuid)) {
				players.get(i).modes.put("S-MUTE", false);
				
				String notifyMessage = MCNSAChat.plugin.getConfig().getString("strings.shadow-unmute-notify");
				for (ChatPlayer p : players)
				{
					if(PermissionManager.checkPermission("admin.shadow-notify", p.name))
					{
						MessageSender.send(notifyMessage.replace("%player%", players.get(i).name), p);
					}
				}
			}
		}
	}
	
	public static void mutePlayer(UUID uuid, String time, String reason) {
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i).getUUID().equals(uuid)) {
				players.get(i).modes.put("MUTE", true);
				players.get(i).timeoutTill = (long) (new Date().getTime() + (Double.valueOf(time) * 60000));
				//Inform the player
				if (players.get(i).server.equals(MCNSAChat.shortCode))
					MessageSender.timeoutPlayer(uuid, time, reason);
			}
		}
	}
	
	public static void shadowMutePlayer(UUID uuid, String time, String reason) {
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i).getUUID().equals(uuid)) {
				players.get(i).modes.put("S-MUTE", true);
				players.get(i).timeoutTill = (long) (new Date().getTime() + (Double.valueOf(time) * 60000));
				
				
				String notifyMessage = MCNSAChat.plugin.getConfig().getString("strings.shadow-mute-notify");
				for (ChatPlayer p : players)
				{
					if(PermissionManager.checkPermission("admin.shadow-notify", p.name))
					{
						MessageSender.send(notifyMessage.replace("%player%", players.get(i).name), p);
					}
				}
				return;
			}
		}
	}

	public static void updatePlayer(ChatPlayer player) {
		PlayerManager.players.remove(PlayerManager.getPlayer(player.getUUID(), player.server));
		PlayerManager.players.add(player);
	}

	public static void addPlayers(ArrayList<ChatPlayer> players2) {
		for (ChatPlayer play: players2) {
			if (getPlayer(play.getUUID(), play.server) == null)
				players.add(play);
		}
		
	}

	public static void removeNonServerPlayers() {
		ArrayList<ChatPlayer> newPlayers = new ArrayList<ChatPlayer>();
		Player[] bukkitPlayers = Bukkit.getOnlinePlayers();
		for (Player player: bukkitPlayers) {
			newPlayers.add(getPlayer(player.getUniqueId(), MCNSAChat.shortCode));
		}
		players = newPlayers;
	}
	
	public static void kickPlayer(UUID uuid)
	{
		kickPlayer(Bukkit.getServer().getPlayer(uuid));
	}
	
	public static void kickPlayer(ChatPlayer player)
	{
		kickPlayer(player.getUUID());
	}

	public static void kickPlayer(Player player)
	{
		player.kickPlayer("");
	}
	
	public static void kickPlayer(UUID uuid, String message)
	{
		kickPlayer(Bukkit.getServer().getPlayer(uuid), message);
	}
	
	public static void kickPlayer(ChatPlayer player, String message)
	{
		kickPlayer(player.getUUID(), message);
	}
	
	public static void kickPlayer(Player player, String message)
	{
		player.kickPlayer(message);
	}

}
