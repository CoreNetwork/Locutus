package com.mcnsa.chat.plugin.listeners;

import com.mcnsa.chat.networking.Network;
import com.mcnsa.chat.plugin.MCNSAChat;
import com.mcnsa.chat.plugin.components.PlayerCommands;
import com.mcnsa.chat.plugin.exceptions.DatabaseException;
import com.mcnsa.chat.plugin.managers.ChannelManager;
import com.mcnsa.chat.plugin.managers.DatabaseManager;
import com.mcnsa.chat.plugin.managers.Permissions;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.plugin.utils.Colours;
import com.mcnsa.chat.plugin.utils.MessageSender;
import com.mcnsa.chat.type.ChatChannel;
import com.mcnsa.chat.type.ChatPlayer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
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
	public void playerJoin(final PlayerJoinEvent event){
		Player player = event.getPlayer();
		String playerName = player.getName();

		// Add to playerManager
        boolean firstTime = false;
        try {
            ResultSet set = DatabaseManager.accessQuery("SELECT player FROM chat_Players WHERE player_id = ?", player.getUniqueId().toString());
            firstTime = !set.isBeforeFirst();
        } catch (DatabaseException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        PlayerManager.PlayerLogin(player);
        if (firstTime) {
			// Record that the player has been on the server
			PlayerManager.getPlayer(playerName).serversVisited
					.add(MCNSAChat.serverName);
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
						MessageSender.send(message, otherPlayer.name);
					}
				}
				MessageSender.send(ChatColor.YELLOW + playerName+ " has joined the game.", playerName);
			}
		}
		else
		{
			MessageSender.joinMessage(playerName, event);
		}
		// Check timeout status
		if (PlayerManager.getPlayer(playerName).modes.get("MUTE")) {
			// get current time
			long timeNow = new Date().getTime();

			if (PlayerManager.getPlayer(playerName, MCNSAChat.shortCode).timeoutTill < timeNow) {
				// Player has finished their timeout
				PlayerManager.getPlayer(playerName, MCNSAChat.shortCode).modes
						.put("MUTE", false);
				Network.updatePlayer(PlayerManager.getPlayer(playerName,
						MCNSAChat.shortCode));
			} else {
				// get time left
				long timeLeft = ((PlayerManager.getPlayer(playerName,
						MCNSAChat.shortCode).timeoutTill - timeNow) / 1000) * 20;

				// Schedule the untimeout
				final String finalPlayerName = playerName;
				Bukkit.getScheduler().scheduleSyncDelayedTask(MCNSAChat.plugin,
						new Runnable() {
							public void run() {
								if (PlayerManager.getPlayer(finalPlayerName,
										MCNSAChat.shortCode) != null
										&& PlayerManager.getPlayer(
												finalPlayerName,
												MCNSAChat.shortCode).modes
												.get("MUTE")) {
									PlayerManager.unmutePlayer(finalPlayerName);
								}
							}
						}, timeLeft);
			}

		}
		
		//Handle S-MUTEs
		if (PlayerManager.getPlayer(playerName).modes.get("S-MUTE")) {
			// get current time
			long timeNow = new Date().getTime();

			if (PlayerManager.getPlayer(playerName, MCNSAChat.shortCode).timeoutTill < timeNow) {
				// Player has finished their timeout
				PlayerManager.getPlayer(playerName, MCNSAChat.shortCode).modes
						.put("S-MUTE", false);
				Network.updatePlayer(PlayerManager.getPlayer(playerName,
						MCNSAChat.shortCode));
			} else {
				// get time left
				long timeLeft = ((PlayerManager.getPlayer(playerName,
						MCNSAChat.shortCode).timeoutTill - timeNow) / 1000) * 20;

				// Schedule the untimeout
				final String finalPlayerName = playerName;
				Bukkit.getScheduler().scheduleSyncDelayedTask(MCNSAChat.plugin,
						new Runnable() {
							public void run() {
								if (PlayerManager.getPlayer(finalPlayerName,
										MCNSAChat.shortCode) != null
										&& PlayerManager.getPlayer(
												finalPlayerName,
												MCNSAChat.shortCode).modes
												.get("S-MUTE")) {
									PlayerManager.shadowUnmutePlayer(finalPlayerName);
								}
							}
						}, timeLeft);
			}

		}
		// Get forceListens
		for (int i = 0; i < ChannelManager.channels.size(); i++) {
			ChatChannel chan = ChannelManager.channels.get(i);
			if (Permissions.forcelisten(playerName, chan.name)) {
				if (!PlayerManager.getPlayer(playerName, MCNSAChat.shortCode).listening
						.contains(chan.name.toLowerCase())) {
					PlayerManager.getPlayer(playerName, MCNSAChat.shortCode).listening
							.add(chan.name.toLowerCase());
				}
			}
		}

		// Set their name on the player tab list
		String playerlistName = Colours.color(Colours.PlayerPrefix(playerName) + playerName);
		if (playerlistName.length() > 16)
			playerlistName = playerlistName.substring(0, 16);

        final String finalListName = playerlistName;

        //Setting packet needs to be delayed a bit, otherwise just joining player won't get it
        Bukkit.getScheduler().runTask(MCNSAChat.plugin, new Runnable()
        {
            @Override
            public void run()
            {
                //Bukkit won't update name with colors if name is the same as previous, so we need to add dummy name just to change it
                //event.getPlayer().setPlayerListName("dummy");
                //event.getPlayer().setPlayerListName(finalListName);
				PlayerCommands.crankreload(null);
			}
        });

        PlayerManager.updateTabNames(player);

		// Notify other players
		Network.playerJoined(PlayerManager.getPlayer(playerName));
	}
	// Handles logouts
	@EventHandler(priority = EventPriority.MONITOR)
	public void playerQuit(PlayerQuitEvent event) {

		// Save the player
		PlayerManager.PlayerLogout(event.getPlayer().getName());

		// Notify others
		MessageSender.quitMessage(event.getPlayer().getName(), event);

	}

	// Handles chat events
	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerChat(AsyncPlayerChatEvent event) {
		if (event.isCancelled()) {
			return;
		}
		// Get chatplayer
		ChatPlayer player = PlayerManager.getPlayer(
				event.getPlayer().getName(), MCNSAChat.shortCode);
		if (player.modes.get("MUTE")) {
			MessageSender.send("&c You are in timeout. Please try again later",
					player.name);
		} else if (player.modes.get("S-MUTE")) {
			MessageSender.shadowChannelMessage(player.channel,
					MCNSAChat.shortCode, player.name, event.getMessage());
		} else {
			MessageSender.channelMessage(player.channel, MCNSAChat.shortCode,
					player.name, event.getMessage());
		}
		event.setCancelled(true);
	}

	// Handles channel alias's and command listeners
	@EventHandler
	public void channelalias(PlayerCommandPreprocessEvent event) {
		String[] args = event.getMessage().split(" ");
		String command = args[0].substring(1);

		if (ChannelManager.channelAlias.containsKey(command) && command != null) {
			// Check if there is any arguments
			if (args.length == 1) {
				// moving channel
				ChatPlayer player = PlayerManager.getPlayer(event.getPlayer()
						.getName(), MCNSAChat.shortCode);
				String channel = ChannelManager.channelAlias.get(command);
				// make sure its a registered channel
				if (ChannelManager.getChannel(channel) != null) {
					ChatChannel chan = ChannelManager.getChannel(channel);
					channel = chan.name;
					if (Permissions.checkReadPerm(chan.read_permission,
							player.name)) {
						// Get players in channel
						String playersInChannel = ChannelManager
								.playersInChannel(channel);
						// We can say this player has the permissions. Lets
						// welcome them
						player.changeChannel(channel);
						Network.updatePlayer(player);
						MessageSender.send(
								Colours.color("&6Welcome to the " + channel
										+ " channel. Players here: "
										+ playersInChannel), player.name);
						event.setCancelled(true);
						return;
					} else {
						MessageSender
								.send("&cYou do not have permissions for this channel",
										player.name);
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
					.getName(), MCNSAChat.shortCode);
			String channel = ChannelManager.channelAlias.get(command)
					.toLowerCase();

			// See if the player is listening to channel
			if (!player.listening.contains(channel.toLowerCase())
					|| !player.channel.equalsIgnoreCase(channel.toLowerCase()))
				if (ChannelManager.getChannel(channel) != null
						&& Permissions
								.checkReadPerm(
										ChannelManager.getChannel(channel).read_permission,
										player.name))
					player.addListen(channel);

			if (!player.modes.get("MUTE")) {
				MessageSender.channelMessage(
						ChannelManager.channelAlias.get(command),
						MCNSAChat.shortCode, player.name, message.toString());
			} else {
				MessageSender.send(
						"&c You are in timeout. Please try again later",
						player.name);
			}

			event.setCancelled(true);
		}
	}
}
