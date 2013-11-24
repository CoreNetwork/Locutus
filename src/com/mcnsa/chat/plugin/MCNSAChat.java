package com.mcnsa.chat.plugin;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.anjocaido.groupmanager.GroupManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.mcnsa.chat.file.Channels;
import com.mcnsa.chat.networking.ClientThread;
import com.mcnsa.chat.plugin.managers.ComponentManager;
import com.mcnsa.chat.plugin.annotations.DatabaseTableInfo;
import com.mcnsa.chat.plugin.exceptions.DatabaseException;
import com.mcnsa.chat.plugin.listeners.PlayerListener;
import com.mcnsa.chat.plugin.managers.ChannelManager;
import com.mcnsa.chat.plugin.managers.CommandManager;
import com.mcnsa.chat.plugin.managers.DatabaseManager;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.plugin.utils.ConsoleLogging;
import com.mcnsa.chat.plugin.utils.FileLog;
import com.mcnsa.chat.type.ChatChannel;
import com.mcnsa.chat.type.ChatPlayer;

public class MCNSAChat extends JavaPlugin{
	public static String serverName;
	public static String shortCode;
	public static Boolean multiServer;
	public static PlayerManager playerManager;
	public static ChannelManager channelManager;
	public static Boolean isSQL;
	public Channels channels;
	public static FileLog logs;
	public CommandManager commandManager;
	public ComponentManager componentManager;
	public static GroupManager groupManager;
	public static MCNSAChat plugin;
	public static ConsoleLogging console;
	public static ClientThread network;
	public void onEnable() {
		plugin = this;
		console = new ConsoleLogging();
		final PluginManager pluginManager = plugin.getServer().getPluginManager();
		final Plugin GMplugin = pluginManager.getPlugin("GroupManager");
		groupManager = (GroupManager)GMplugin;
		
				
		//Load the configs
		console.info("Loading config");
		this.saveDefaultConfig();
		MCNSAChat.serverName = this.getConfig().getString("ServerName");
		MCNSAChat.shortCode = this.getConfig().getString("ShortCode");
		MCNSAChat.multiServer = this.getConfig().getBoolean("multiServer");
		MCNSAChat.isSQL = this.getConfig().getBoolean("database-isSQL");
		boolean isTransitioning = this.getConfig().getBoolean("database-isTransitioning");
		
		if (isSQL && isTransitioning)
		{
			transition();
				
		}
		
		MCNSAChat.logs = new FileLog();
		
		//Check to see if the directory for players exists
		File playerFolder = new File("plugins/MCNSAChat/Players");
		if (!playerFolder.exists())
			playerFolder.mkdir();
		
		console.info("Config Loaded");
		console.info("Server name is: "+MCNSAChat.serverName);
		console.info("Server shortcode is: "+MCNSAChat.shortCode);

		//Notify if the multiServer is set to true or false
		if (MCNSAChat.multiServer) {
			//Config for multiserver mode
			console.info("Server is running in Multi Server mode.");
		}
		else {
			//Config for single server mode
			console.info("Server is running in single server mode.");
		}
		
		//Load up the playermanager
		MCNSAChat.playerManager = new PlayerManager();
		//Load up the ChannelManager
		MCNSAChat.channelManager = new ChannelManager();
		//Load up command manager
		this.commandManager = new CommandManager();
		// ok, start loading our components
		componentManager = new ComponentManager();
		//Load components
		componentManager.initializeComponents();
		//Load our commands
		commandManager.loadCommands(componentManager);
		
		//load up the saved channels
		this.channels = new Channels();
		this.channels.saveDefault();
		console.info("Loading channels");
		loadChannels();

		DatabaseManager dbManager = new DatabaseManager();
		dbManager.enable();
		//Support for /reload
		addOnlinePlayers();
		//Start up the player listener
		new PlayerListener();

		
		
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				if (network == null && MCNSAChat.multiServer) {
					network = new ClientThread();
					network.start();
				}
			}
		}, 0L, 600L);
	}
	private void transition() {
		try {
			DatabaseManager db = new DatabaseManager();
			db.enable();
		} catch (Exception e) {
			ConsoleLogging.severe("Could not connect to DB to transfer players");
			e.printStackTrace();
			return;
		} 
		File playerFolder = new File("plugins/MCNSAChat/Players");
		if (!playerFolder.exists())
		{
			ConsoleLogging.severe("Cannot transition system, no player folder exists");
			return;
		}
		File[] playerFiles = playerFolder.listFiles();
		ConsoleLogging.info(String.valueOf(playerFiles.length) + " players to transfer");
		long startTime = System.currentTimeMillis();
		double i = 0;
		for (File player : playerFiles)
		{
			String playerName = player.getName().substring(0, player.getName().length() - 4);
			ChatPlayer cPlayer = new ChatPlayer(playerName);
			cPlayer.loginTime = player.lastModified();
			ConsoleLogging.info(String.format("Transferring: %s \t %.2f%%", playerName, (i / playerFiles.length * 100)));
			cPlayer.savePlayer();
			i++;
		}
		ConsoleLogging.info("Finished transferring all files in " + String.valueOf((System.currentTimeMillis() - startTime)/1000) + "s");
		DatabaseManager.disconnect();
		
	}
	public void onDisable() {
		if (multiServer && network != null) {
			ConsoleLogging.info("Closing network threads");
			MCNSAChat.network.close();
			MCNSAChat.network = null;
				
		}
		ConsoleLogging.info("Closing SQL connection");
		DatabaseManager.disconnect();
		//Clear players
		PlayerManager.players = new ArrayList<ChatPlayer>();
		
		ConsoleLogging.info("Saving Channels");
		saveChannels();
		
		ConsoleLogging.info("Disabled");
	}
	@SuppressWarnings("unchecked")
	public void loadChannels() {
		
		List<Map<String,?>> channelData = (List<Map<String, ?>>) channels.get().getList("channels");
		for (Map<String, ?> channel : channelData) {
		    ChatChannel c = new ChatChannel(
		    		(String) channel.get("name"),
		    		(String) channel.get("write_permission"), 
		    		(String) channel.get("read_permission"),
		    		(String) channel.get("alias"),
		    		(String) channel.get("color")
		    		);
			List<String> modes = (List<String>) channel.get("modes");
			for (String mode : modes) {
				if (c.modes.containsKey(mode.toUpperCase())) {
					c.modes.remove(mode.toUpperCase());
					c.modes.put(mode.toUpperCase(), true);
				}
			}
			ChannelManager.channels.add(c);
			//Add alias to channelmanager
			if (c.alias !=null) {
				ChannelManager.channelAlias.put(c.alias, c.name);
			}
			c = null;
		}
	}
	public void saveChannels(){
		ArrayList<ChatChannel> channels = ChannelManager.channels;
		ArrayList<HashMap<String, ?>> savedChannels = new ArrayList<HashMap<String, ?>>();
		for (ChatChannel channel: channels) {
			HashMap<String, Object> chan = new HashMap<String, Object>();
			if (channel.modes.get("PERSIST")) {
				//Need to save the channel
				chan.put("name", channel.name);
				chan.put("write_permission", channel.write_permission);
				chan.put("read_permission", channel.read_permission);
				chan.put("color", channel.color);
				chan.put("alias", channel.alias);
				
				//Sort out the modes
				ArrayList<String> modes = new ArrayList<String>();
				for (String mode: channel.modes.keySet()){
					if (channel.modes.get(mode)) {
						modes.add(mode);
					}
				}
				chan.put("modes", modes);
				savedChannels.add(chan);
			}
		}
		this.channels.get().set("channels", savedChannels);
		this.channels.save();
	}
	
	public void addOnlinePlayers() {
		Player[] players = Bukkit.getOnlinePlayers();
		for(Player player: players) {
			if (PlayerManager.getPlayer(player.getName(), MCNSAChat.shortCode) == null)
				PlayerManager.PlayerLogin(player.getName());
		}
	}
}
