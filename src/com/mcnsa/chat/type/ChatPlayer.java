package com.mcnsa.chat.type;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.mcnsa.chat.file.Players;
import com.mcnsa.chat.plugin.MCNSAChat;

public class ChatPlayer {
	public String name;
	public String server;
	public String channel;
	public String lastPm;
	public Map<String, Boolean> modes = new HashMap<String, Boolean>();
	public ArrayList<String> listening;
	public ArrayList<String> muted = new ArrayList<String>();
	public ArrayList<String> serversVisited = new ArrayList<String>();
	public Players playersFile;
	private MCNSAChat plugin;
	
	@SuppressWarnings("unchecked")
	public ChatPlayer(String username){
		this.plugin = MCNSAChat.plugin;
		
		//Player name
		this.name = username;
		//Player server
		this.server = this.plugin.shortCode;
		
		//check to see if there is actually a player file there
		File playerFile = new File("plugins/MCNSAChat/Players/", username+".yml");
		playersFile = new Players(this.name);
		if (!playerFile.exists()) {
			//Player is new to the server. Set the defaults
			this.channel = this.plugin.getConfig().getString("defaultChannel");
			this.listening = (ArrayList<String>) this.plugin.getConfig().getList("defaultListen");
			this.modes.put("SEEALL", false);
			this.modes.put("MUTE", false);
			this.modes.put("POOF", false);
			this.lastPm = null;
		}
		else {
			
			//Get the details from the player's config file 
			if (this.playersFile.get().contains(MCNSAChat.plugin.serverName+"-Channel")) {
				this.channel = this.playersFile.get().getString(MCNSAChat.plugin.serverName+"-Channel");
			}
			else {
				this.channel = this.plugin.getConfig().getString("defaultChannel");
			}
			this.listening = (ArrayList<String>) this.playersFile.get().getList("listening");
			for (String defaultListen: (ArrayList<String>) this.plugin.getConfig().getList("defaultListen")) {
				if (!this.listening.contains(defaultListen))
					this.listening.add(defaultListen);
			}
			this.lastPm = this.playersFile.get().getString("lastPm");
			this.modes.put("SEEALL", this.playersFile.get().getBoolean("modes.SEELALL"));
			this.modes.put("MUTE", this.playersFile.get().getBoolean("modes.MUTE"));
			this.modes.put("POOF", this.playersFile.get().getBoolean("modes.POOF"));
			this.muted = (ArrayList<String>) this.playersFile.get().getList("muted");
			this.serversVisited = (ArrayList<String>) this.playersFile.get().getList("serversVisited");
		}
		playersFile.save();
	}
	public void savePlayer() {
		this.playersFile.get().set(MCNSAChat.plugin.serverName+"-Channel", this.channel);
		this.playersFile.get().set("channel", this.channel);
		this.playersFile.get().set("listening", this.listening);
		this.playersFile.get().set("lastPm", this.lastPm);
		this.playersFile.get().set("modes.SEEALL", this.modes.get("SEEALL"));
		this.playersFile.get().set("modes.MUTE", this.modes.get("MUTE"));
		this.playersFile.get().set("modes.POOF", this.modes.get("POOF"));
		this.playersFile.get().set("muted", this.muted);
		this.playersFile.get().set("serversVisited", this.serversVisited);
		this.playersFile.save();
	}
	public void changeChannel(String channel) {
		this.channel = channel;
	}
	public void channelListen(String channel){
		if (this.listening.contains(channel)){
			this.listening.remove(channel);
		}
		else {
			this.listening.add(channel);
		}
	}
}
