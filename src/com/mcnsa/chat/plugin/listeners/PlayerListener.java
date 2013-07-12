package com.mcnsa.chat.plugin.listeners;

import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.mcnsa.chat.networking.Network;
import com.mcnsa.chat.plugin.MCNSAChat;
import com.mcnsa.chat.plugin.managers.ChannelManager;
import com.mcnsa.chat.plugin.managers.Permissions;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.plugin.utils.Colours;
import com.mcnsa.chat.plugin.utils.MessageSender;
import com.mcnsa.chat.type.ChatChannel;
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
				
		//Check if new player.
		if (!PlayerManager.getPlayer(playerName).serversVisited.contains(MCNSAChat.serverName)) {
			//Record that the player has been on the server
			PlayerManager.getPlayer(playerName).serversVisited.add(MCNSAChat.serverName);
			//Check if the welcome is to be displayed
			if (plugin.getConfig().getBoolean("displayWelcome")) {
				//Display the welcome message
				String message = plugin.getConfig().getString("strings.player-welcome");
				message = message.replaceAll("%player%", playerName);
				for (Player otherPlayer : Bukkit.getOnlinePlayers())
					if (!otherPlayer.getName().equals(event.getPlayer().getName()))
						MessageSender.send(message, player.getName());
			}
		}
		//Check timeout status
		if (PlayerManager.getPlayer(playerName).modes.get("MUTE")) {
			//get current time
			long timeNow = new Date().getTime();

			if (PlayerManager.getPlayer(playerName, MCNSAChat.shortCode).timeoutTill < timeNow) {
				//Player has finished their timeout
				PlayerManager.getPlayer(playerName, MCNSAChat.shortCode).modes.put("MUTE", false);
				Network.updatePlayer(PlayerManager.getPlayer(playerName, MCNSAChat.shortCode));
			}
			else {
				//get time left
				long timeLeft = ((PlayerManager.getPlayer(playerName, MCNSAChat.shortCode).timeoutTill - timeNow)/1000) * 20;
				
				//Schedule the untimeout
				final String finalPlayerName = playerName;
				Bukkit.getScheduler().scheduleSyncDelayedTask(MCNSAChat.plugin, new Runnable() {
					public void run() {
							if (PlayerManager.getPlayer(finalPlayerName, MCNSAChat.shortCode) != null && PlayerManager.getPlayer(finalPlayerName, MCNSAChat.shortCode).modes.get("MUTE")){
								PlayerManager.unmutePlayer(finalPlayerName);
							}
						}
				}, timeLeft);
			}
			
		}
				
		//Get forceListens
		for (int i = 0; i < ChannelManager.channels.size(); i++) {
			ChatChannel chan = ChannelManager.channels.get(i);
			if (Permissions.forcelisten(playerName, chan.name)) {
				if (!PlayerManager.getPlayer(playerName, MCNSAChat.shortCode).listening.contains(chan.name)) {
					PlayerManager.getPlayer(playerName, MCNSAChat.shortCode).listening.add(chan.name);
					MCNSAChat.console.info("Adding forcelisten: "+chan.name);
				}
			}
		}
		
		//Set their name on the player tab list
		String playerlistName = Colours.color(Colours.PlayerPrefix(playerName)+playerName);
		if (playerlistName.length() > 16)
			playerlistName = playerlistName.substring(0, 16);
		event.getPlayer().setPlayerListName(playerlistName);
		
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

	}
	//Handles chat events
	@EventHandler(priority = EventPriority.LOWEST)
	public void playerChat(AsyncPlayerChatEvent event){
		//Get chatplayer
		ChatPlayer player = PlayerManager.getPlayer(event.getPlayer().getName(), MCNSAChat.shortCode);
		if (!player.modes.get("MUTE")) { 
			MessageSender.channelMessage(player.channel, MCNSAChat.shortCode, player.name, event.getMessage());
		}
		else {
			MessageSender.send("&c You are in timeout. Please try again later", player.name);
		}
		event.setCancelled(true);
	}
	
	//Handles channel alias's and command listeners
	@EventHandler
	public void channelalias(PlayerCommandPreprocessEvent event) {
		if (event.isCancelled())
			return;
		String[] args = event.getMessage().split(" ");
		String command = args[0].substring(1);
		if (command == null)
			return;
		
		if (ChannelManager.channelAlias.containsKey(command) && command !=null) {
			//Check if there is any arguments
			if (args.length == 1) {
				//moving channel
				ChatPlayer player = PlayerManager.getPlayer(event.getPlayer().getName(), MCNSAChat.shortCode);
				player.changeChannel(ChannelManager.channelAlias.get(command));
				Network.updatePlayer(player);
				event.setCancelled(true);
				return;
			}
			
			//build the message
			StringBuffer message = new StringBuffer();
			for (int i = 1; i < args.length; i++) {
				if (message.length() < 1)
					message.append(args[i]);
				else
					message.append(" "+args[i]);
					
			}
			
			//Get chatplayer
			ChatPlayer player = PlayerManager.getPlayer(event.getPlayer().getName(), MCNSAChat.shortCode);
			if (!player.modes.get("MUTE")) { 
				MessageSender.channelMessage(ChannelManager.channelAlias.get(command), MCNSAChat.shortCode, player.name, message.toString());
			}
			else {
				MessageSender.send("&c You are in timeout. Please try again later", player.name);
			}
			
			
			event.setCancelled(true);
		}
	}
}
