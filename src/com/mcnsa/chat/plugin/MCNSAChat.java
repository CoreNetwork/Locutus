package com.mcnsa.chat.plugin;

import org.bukkit.plugin.java.JavaPlugin;

import com.mcnsa.chat.plugin.managers.ChannelManager;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.plugin.utils.ConsoleLogging;

public class MCNSAChat extends JavaPlugin{
	public String serverName;
	public String shortCode;
	public Boolean multiServer;
	private PlayerManager playerManager;
	private ChannelManager channelManager;
	public static MCNSAChat plugin;
	public static ConsoleLogging console;
	public void onEnable() {
		plugin = this;
		console = new ConsoleLogging();
		
		console.info("Enabled");
		
		console.info("Loading config");
		this.saveDefaultConfig();
		this.serverName = this.getConfig().getString("ServerName");
		this.shortCode = this.getConfig().getString("ShortCode");
		this.multiServer = this.getConfig().getBoolean("multiServer");
		
		console.info("Config Loaded");
		console.info("Server name is: "+this.serverName);
		console.info("Server shortcode is: "+this.shortCode);

		//Notify if the multiServer is set to true or false
		if (this.multiServer) {
			console.info("Server is running in Multi Server mode.");
		}
		else {
			console.info("Server is running in single server mode.");
		}
		
		//Load up the playermanager
		this.playerManager = new PlayerManager();
		//Load up the ChannelManager
		this.channelManager = new ChannelManager();
		
		//load up the saved channels
		
		
	}
	public void onDisable() {
		
		console.info("Disabled");
	}
}
