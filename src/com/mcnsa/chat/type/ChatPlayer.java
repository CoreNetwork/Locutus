package com.mcnsa.chat.type;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import com.mcnsa.chat.file.Players;
import com.mcnsa.chat.plugin.MCNSAChat;

public class ChatPlayer {
	public String name;
	public String server;
	public String channel;
	public String lastPm;
	public Map<String, Boolean> modes;
	public ArrayList<String> listening;
	public Players playerFile;
	private MCNSAChat plugin;
	
	@SuppressWarnings("unchecked")
	public ChatPlayer(String username){
		this.plugin = MCNSAChat.plugin;
		
		//Player name
		this.name = username;
		//Player server
		this.server = this.plugin.shortCode;
		
		//check to see if there is actually a player file there
		File playerFile = new File(this.plugin.getDataFolder()+"Players/", username+".yml");
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
			this.channel = this.playerFile.get().getString("channel");
			this.listening = (ArrayList<String>) this.playerFile.get().getList("listening");
			this.lastPm = this.playerFile.get().getString("lastPm");
			this.modes.put("SEEALL", this.playerFile.get().getBoolean("modes.SEELALL"));
			this.modes.put("MUTE", this.playerFile.get().getBoolean("modes.MUTE"));
			this.modes.put("POOF", this.playerFile.get().getBoolean("modes.POOF"));
		}
		
	}
	public void savePlayer() {
		this.playerFile.get().set("channel", this.channel);
		this.playerFile.get().set("listening", this.listening);
		this.playerFile.get().set("lastPm", this.lastPm);
		this.playerFile.get().set("modes.SEEALL", this.modes.get("SEEALL"));
		this.playerFile.get().set("modes.MUTE", this.modes.get("MUTE"));
		this.playerFile.get().set("modes.POOF", this.modes.get("POOF"));
		this.playerFile.save();
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
