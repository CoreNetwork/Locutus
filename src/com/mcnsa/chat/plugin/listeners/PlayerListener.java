package com.mcnsa.chat.plugin.listeners;

import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.mcnsa.chat.networking.Network;
import com.mcnsa.chat.plugin.MCNSAChat;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.plugin.utils.MessageSender;
import com.mcnsa.chat.type.ChatPlayer;

public class PlayerListener implements Listener{
	private MCNSAChat plugin;
	public PlayerListener() {
		this.plugin = MCNSAChat.plugin;
		//Register event handler
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	//Handles login events
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerLogin(PlayerJoinEvent event) {
		//Get the player
		Player player = event.getPlayer();
		String playerName = player.getName();
		
		//Add to playerManager
		PlayerManager.PlayerLogin(playerName);
		
		//Start the join notification
		String message = plugin.getConfig().getString("strings.player-join");
		message = message.replaceAll("%player%", playerName);
		
		//Check if new player.
		if (!PlayerManager.getPlayer(playerName).serversVisited.contains(MCNSAChat.serverName)) {
			//Record that the player has been on the server
			PlayerManager.getPlayer(playerName).serversVisited.add(MCNSAChat.serverName);
			//Check if the welcome is to be displayed
			if (plugin.getConfig().getBoolean("displayWelcome")) {
				//Display the welcome message
				message = plugin.getConfig().getString("strings.player-welcome");
				message = message.replaceAll("%player%", playerName);
				for (Player otherPlayer : Bukkit.getOnlinePlayers())
					if (!otherPlayer.getName().equals(event.getPlayer().getName()))
						MessageSender.send(message, player.getName());
			}
			//Debug
			MCNSAChat.console.info(playerName+" is new to the server");
		}
		//Check timeout status
		if (PlayerManager.getPlayer(playerName).modes.get("MUTE")) {
			//get current time
			long timeNow = new Date().getTime();
			if (PlayerManager.getPlayer(playerName).timeoutTill < timeNow) {
				//Player has finished their timeout
				PlayerManager.getPlayer(playerName).modes.put("MUTE", false);
			}
			else {
				//get time left
				long timeLeft = (PlayerManager.getPlayer(playerName).timeoutTill - timeNow) * 20;
				//Schedule the untimeout
				final String finalPlayerName = playerName;
				Bukkit.getScheduler().scheduleSyncDelayedTask(MCNSAChat.plugin, new Runnable() {
					public void run() {
						if (PlayerManager.getPlayer(finalPlayerName) != null)
							PlayerManager.getPlayer(finalPlayerName).modes.put("MUTE", false);
					}
				}, timeLeft);
			}
			
		}
		//Notify other players
		MessageSender.joinMessage(playerName, event);
		
		Network.playerJoined(PlayerManager.getPlayer(playerName));
		
	}
	//Handles logouts
	@EventHandler(priority = EventPriority.LOWEST)
	public void playerQuit (PlayerQuitEvent event) {
		//Save the player
		PlayerManager.PlayerLogout(event.getPlayer().getName());

		//Notify others
		MessageSender.quitMessage(event.getPlayer().getName(), event);
		
		//Notify network
		Network.playerQuit(PlayerManager.getPlayer(event.getPlayer().getName()));
	}
	//Handles chat events
	@EventHandler(priority = EventPriority.LOWEST)
	public void playerChat(AsyncPlayerChatEvent event){
		//Get chatplayer
		ChatPlayer player = PlayerManager.getPlayer(event.getPlayer().getName());
		MessageSender.channelMessage(player.channel, MCNSAChat.shortCode, player.name, event.getMessage());
		event.setCancelled(true);
	}
}
