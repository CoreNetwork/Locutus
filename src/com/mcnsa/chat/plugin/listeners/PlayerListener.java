package com.mcnsa.chat.plugin.listeners;

import java.sql.ResultSet;
import java.util.Date;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;

import com.mcnsa.chat.plugin.MCNSAChat;
import com.mcnsa.chat.plugin.managers.ChannelManager;
import com.mcnsa.chat.plugin.managers.DatabaseManager;
import com.mcnsa.chat.plugin.managers.PermissionManager;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.plugin.utils.Colors;
import com.mcnsa.chat.plugin.utils.MessageSender;
import com.mcnsa.chat.type.ChatChannel;
import com.mcnsa.chat.type.ChatPlayer;
//TODO what is channelalias?
//TODO Fix player join-rejoin
public class PlayerListener implements Listener {
	private MCNSAChat plugin;

	public PlayerListener() {
		this.plugin = MCNSAChat.plugin;
		// Register event handler
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	// Handles login events
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLogin(PlayerLoginEvent event) {
		// Get the player
		Player player = event.getPlayer();
		String playerName = player.getName();

		boolean newPlayer;
		try {
			ResultSet rs = DatabaseManager.accessQuery("SELECT lastLogin FROM chat_Players WHERE player=?", playerName);
			rs.getLong(1);
			
			newPlayer = false;
			
		} catch (Exception e)
		{
			newPlayer = true;
		}
		// Check if new player.
		if (newPlayer && MCNSAChat.isLockdown)
		{
			String message = MCNSAChat.plugin.getConfig().getString("strings.lockdown-kick");
			event.setKickMessage(message);
			event.setResult(Result.KICK_OTHER);
			return;
		}
		

	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();
		String playerName = player.getName();
		//First check if they're returning from a login
		if (PlayerManager.getPlayer(player) != null) {
			ChatPlayer cplayer = PlayerManager.getPlayer(player);
			Bukkit.getScheduler().cancelTask(cplayer.timeoutID);
			cplayer.timeoutID = 0;
			cplayer.isOnline = true;
			cplayer.leaveMessage = "";
			return;
		}
		
		// Add to playerManager
		PlayerManager.PlayerLogin(player.getUniqueId());
		if (PlayerManager.getPlayer(player).firstTime) {
			// Record that the player has been on the server
			PlayerManager.getPlayer(player).visitServer(MCNSAChat.serverName);
			// Check if the welcome is to be displayed
			if (plugin.getConfig().getBoolean("displayWelcome")) {
				// Display the welcome message

				event.setJoinMessage("");
				String message = plugin.getConfig().getString(
						"strings.player-welcome");
				message = message.replaceAll("%player%", playerName);
				for (int i = 0; i < PlayerManager.players.size(); i++) {
					ChatPlayer otherPlayer = PlayerManager.players.get(i);
					if (!otherPlayer.name.equalsIgnoreCase(playerName)
							&& otherPlayer.server.equals(MCNSAChat.shortCode)) {
						MessageSender.send(message, otherPlayer);
					}
				}
				MessageSender.send(ChatColor.YELLOW + playerName+ " has joined the game.", player);
			}
		}
		else
		{
			MessageSender.joinMessage(player, event);
		}
		// Check timeout status
		if (PlayerManager.getPlayer(player).modes.get("MUTE")) {
			// get current time
			long timeNow = new Date().getTime();

			if (PlayerManager.getPlayer(player, MCNSAChat.shortCode).timeoutTill < timeNow) {
				// Player has finished their timeout
				PlayerManager.getPlayer(player, MCNSAChat.shortCode).modes
						.put("MUTE", false);
			} else {
				// get time left
				long timeLeft = ((PlayerManager.getPlayer(player,
						MCNSAChat.shortCode).timeoutTill - timeNow) / 1000) * 20;

				// Schedule the untimeout
				final UUID uuid = player.getUniqueId();
				Bukkit.getScheduler().scheduleSyncDelayedTask(MCNSAChat.plugin,
						new Runnable() {
							public void run() {
								if (PlayerManager.getPlayer(uuid,
										MCNSAChat.shortCode) != null
										&& PlayerManager.getPlayer(
												uuid,
												MCNSAChat.shortCode).modes
												.get("MUTE")) {
									PlayerManager.unmutePlayer(uuid);
								}
							}
						}, timeLeft);
			}

		}
		
		//Handle S-MUTEs
		if (PlayerManager.getPlayer(player).modes.get("S-MUTE")) {
			// get current time
			long timeNow = new Date().getTime();

			if (PlayerManager.getPlayer(player, MCNSAChat.shortCode).timeoutTill < timeNow) {
				// Player has finished their timeout
				PlayerManager.getPlayer(player, MCNSAChat.shortCode).modes
						.put("S-MUTE", false);
			} else {
				// get time left
				long timeLeft = ((PlayerManager.getPlayer(player,
						MCNSAChat.shortCode).timeoutTill - timeNow) / 1000) * 20;

				// Schedule the untimeout
				final UUID uuid = player.getUniqueId();
				Bukkit.getScheduler().scheduleSyncDelayedTask(MCNSAChat.plugin,
						new Runnable() {
							public void run() {
								if (PlayerManager.getPlayer(uuid,
										MCNSAChat.shortCode) != null
										&& PlayerManager.getPlayer(
												uuid,
												MCNSAChat.shortCode).modes
												.get("S-MUTE")) {
									PlayerManager.shadowUnmutePlayer(uuid);
								}
							}
						}, timeLeft);
			}

		}
		
		//Get force listens
		for(String chan : ChannelManager.getChannelList()){
			if (PermissionManager.forceListen(player, chan)){
				if (!PlayerManager.getPlayer(player).isListening(chan)){
					PlayerManager.getPlayer(player).listenTo(chan);
				}
			}
		}

		// Set their name on the player tab list
		String playerlistName = Colors.color(Colors.PlayerPrefix(player)
				+ playerName);
		if (playerlistName.length() > 16)
			playerlistName = playerlistName.substring(0, 16);
		event.getPlayer().setPlayerListName(playerlistName);

		// Notify other players

	}
	// Handles logouts
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(final PlayerQuitEvent event) {
		// Leave the logout event for a few seconds to see if they return...
		// Save the player
		ChatPlayer cplayer = PlayerManager.getPlayer(event.getPlayer());
		cplayer.isOnline = false;
		cplayer.leaveMessage = event.getQuitMessage();
		event.setQuitMessage("");
		long timeleftTicks = MCNSAChat.plugin.getConfig().getLong("relogin-time",5)*20;
		cplayer.timeoutID = Bukkit.getScheduler().scheduleSyncDelayedTask(MCNSAChat.plugin,
				new Runnable() {
					public void run() {
						MessageSender.broadcast(event.getQuitMessage());
						PlayerManager.PlayerLogout(event.getPlayer().getUniqueId());

						// Notify others
						MessageSender.quitMessage(event.getPlayer(), event);
					}
				}, timeleftTicks);

	}

	// Handles chat events
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (event.isCancelled()) {
			return;
		}
		// Get chatplayer
		ChatPlayer player = PlayerManager.getPlayer(
				event.getPlayer(), MCNSAChat.shortCode);
		if (player.modes.get("MUTE")) {
			MessageSender.send("&c You are in timeout. Please try again later",
					player);
		}  else {
			MessageSender.channelMessage(player.channel, MCNSAChat.shortCode,
					player, event.getMessage());
		}
		event.setCancelled(true);
	}

	// Handles channel alias's and command listeners
	@EventHandler
	public static void channelalias(PlayerCommandPreprocessEvent event) {
		String[] args = event.getMessage().split(" ");
		String command = args[0].substring(1);

		if (ChannelManager.channelAlias.containsKey(command) && command != null) {
			// Check if there is any arguments
			if (args.length == 1) {
				// moving channel
				ChatPlayer player = PlayerManager.getPlayer(event.getPlayer()
						, MCNSAChat.shortCode);
				String channel = ChannelManager.channelAlias.get(command);
				// make sure its a registered channel
				if (ChannelManager.getChannel(channel) != null) {
					ChatChannel chan = ChannelManager.getChannel(channel);
					channel = chan.name;
					if (PermissionManager.checkPermission(chan.readPermission,
							player.name)) {
						// Get players in channel
						String playersInChannel = ChannelManager
								.playersInChannel(channel);
						// We can say this player has the permissions. Lets
						// welcome them
						player.changeChannel(channel);
						MessageSender.send(
								Colors.color("&6Welcome to the " + channel
										+ " channel. Players here: "
										+ playersInChannel), player);
						event.setCancelled(true);
						return;
					} else {
						MessageSender
								.send("&cYou do not have permissions for this channel",
										player);
						event.setCancelled(true);
						return;
					}
				}
			}

			// build the message
			StringBuffer message = new StringBuffer();
			for (int i = 1; i < args.length; i++) {
				if (message.length() < 1)
					message.append(args[i]);
				else
					message.append(" " + args[i]);

			}
			// get chat player
			ChatPlayer player = PlayerManager.getPlayer(event.getPlayer()
					, MCNSAChat.shortCode);
			String channel = ChannelManager.channelAlias.get(command)
					.toLowerCase();

			// See if the player is listening to channel
			if (!player.isListening(channel.toLowerCase())
					|| !player.channel.equalsIgnoreCase(channel.toLowerCase()))
				if (ChannelManager.getChannel(channel) != null
						&& PermissionManager
								.checkPermission(
										ChannelManager.getChannel(channel).readPermission,
										player.name))
					player.addListen(channel);

			if (!player.modes.get("MUTE")) {
				MessageSender.channelMessage(
						ChannelManager.channelAlias.get(command),
						MCNSAChat.shortCode, player, message.toString());
			} else {
				MessageSender.send(
						"&c You are in timeout. Please try again later",
						player);
			}

			event.setCancelled(true);
		}
	}
}
