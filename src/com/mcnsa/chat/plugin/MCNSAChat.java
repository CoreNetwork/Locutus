package com.mcnsa.chat.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.plugin.java.JavaPlugin;

import com.mcnsa.chat.file.Channels;
import com.mcnsa.chat.plugin.listeners.PlayerListener;
import com.mcnsa.chat.plugin.managers.ChannelManager;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.plugin.utils.ConsoleLogging;
import com.mcnsa.chat.type.ChatChannel;

public class MCNSAChat extends JavaPlugin{
	public String serverName;
	public String shortCode;
	public Boolean multiServer;
	@SuppressWarnings("unused")
	public PlayerManager playerManager;
	@SuppressWarnings("unused")
	public ChannelManager channelManager;
	public Channels channels;
	public static MCNSAChat plugin;
	public static ConsoleLogging console;
	public void onEnable() {
		plugin = this;
		console = new ConsoleLogging();
		
		console.info("Enabled");
		
		//Check to see if the directory for players exists
		File playerFolder = new File("plugins/MCNSAChat/Players");
		
		playerFolder.mkdir();
				
		//Load the configs
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
		this.channels = new Channels();
		this.channels.saveDefault();
		console.info("Loading channels");
		loadChannels();
		
		//Start up the player listener
		new PlayerListener();
	}
	public void onDisable() {
		
		console.info("Saving Channels");
		saveChannels();
		
		console.info("Disabled");
	}
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
	public static void Channelsinfo(){
		for (int i = 0; i < ChannelManager.channels.size(); i++) {
			ChatChannel c = ChannelManager.channels.get(i);
			console.info(c.name);
			console.info(c.alias);
			console.info(c.color);
			console.info(c.read_permission);
			console.info(c.write_permission);
			console.info(c.modes.toString());
		}
	}
}
