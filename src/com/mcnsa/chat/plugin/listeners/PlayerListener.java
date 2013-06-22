package com.mcnsa.chat.plugin.listeners;

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
		
		//Notify other players
		MessageSender.joinMessage(playerName, MCNSAChat.serverName, event);
		
		Network.playerJoined(PlayerManager.getPlayer(playerName));
		
	}
	//Handles logouts
	@EventHandler(priority = EventPriority.LOWEST)
	public void playerQuit (PlayerQuitEvent event) {
		//Save the player
		PlayerManager.PlayerLogout(event.getPlayer().getName());

		//Notify others
		MessageSender.quitMessage(event.getPlayer().getName(), MCNSAChat.serverName, event);
	}
	//Handles chat events
	@EventHandler(priority = EventPriority.LOWEST)
	public void playerChat(AsyncPlayerChatEvent event){
		MessageSender.sendChannel(event.getPlayer().getName(), event.getMessage());
		event.setCancelled(true);
	}
}
