package com.mcnsa.chat.type;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.mcnsa.chat.file.Players;
import com.mcnsa.chat.networking.Network;
import com.mcnsa.chat.plugin.MCNSAChat;
import com.mcnsa.chat.plugin.managers.ChannelManager;
import com.mcnsa.chat.plugin.managers.Permissions;
import com.mcnsa.chat.plugin.utils.Colours;

public class ChatPlayer implements Serializable{

	private static final long serialVersionUID = -4493289681267602037L;
	public String name;
	public String server;
	public String channel;
	public String lastPm;
	public Map<String, Boolean> modes = new HashMap<String, Boolean>();
	public ArrayList<String> listening;
	public ArrayList<String> muted = new ArrayList<String>();
	public ArrayList<String> serversVisited = new ArrayList<String>();
	transient Players playersFile;
	public long timeoutTill = 0;
	public String formatted;
	
	@SuppressWarnings("unchecked")
	public ChatPlayer(String username){
		
		//Player name
		this.name = username;
		//Player server
		this.server = MCNSAChat.shortCode;
		
		//check to see if there is actually a player file there
		File playerFile = new File("plugins/MCNSAChat/Players/", username+".yml");
		playersFile = new Players(this.name);
		if (!playerFile.exists()) {
			//Player is new to the server. Set the defaults
			this.channel = MCNSAChat.plugin.getConfig().getString("defaultChannel");
			this.listening = (ArrayList<String>) MCNSAChat.plugin.getConfig().getList("defaultListen");
			this.modes.put("SEEALL", false);
			this.modes.put("MUTE", false);
			this.modes.put("POOF", false);
			this.modes.put("LOCKED", false);
			this.lastPm = null;
		}
		else {
			
			//Get the details from the player's config file 
			if (this.playersFile.get().contains(MCNSAChat.serverName+"-Channel")) {
				this.channel = this.playersFile.get().getString(MCNSAChat.serverName+"-Channel");
			}
			else {
				this.channel = MCNSAChat.plugin.getConfig().getString("defaultChannel");
			}
			//Listening stuff
			this.listening = (ArrayList<String>) this.playersFile.get().getList("listening");
			for (String defaultListen: (ArrayList<String>) MCNSAChat.plugin.getConfig().getList("defaultListen")) {
				if (!this.listening.contains(defaultListen.toLowerCase())) {
					this.listening.add(defaultListen.toLowerCase());
				}
			}
			ArrayList<String> newListen = new ArrayList<String>();
			for (int i = 0; i < this.listening.size(); i++) {
				ChatChannel chan = ChannelManager.getChannel(this.listening.get(i));
				if (chan != null) {
					if (Permissions.checkReadPerm(chan.read_permission, this.name) && !newListen.contains(channel.toLowerCase())) {
						newListen.add(channel.toLowerCase());
					}
				}
			}
			this.listening = newListen;
			this.lastPm = this.playersFile.get().getString("lastPm");
			this.modes.put("SEEALL", this.playersFile.get().getBoolean("modes.SEELALL"));
			this.modes.put("MUTE", this.playersFile.get().getBoolean("modes.MUTE"));
			this.modes.put("POOF", this.playersFile.get().getBoolean("modes.POOF"));
			this.modes.put("LOCKED", this.playersFile.get().getBoolean("modes.LOCKED"));
			this.muted = (ArrayList<String>) this.playersFile.get().getList("muted");
			this.serversVisited = (ArrayList<String>) this.playersFile.get().getList("serversVisited");
			this.timeoutTill = this.playersFile.get().getLong("timeoutTill");
		}
		this.formatted = Colours.PlayerPrefix(name)+this.name;
		playersFile.save();
	}
	public void savePlayer() {
		if (this.playersFile == null) {
			this.playersFile = new Players(this.name);
		}
		this.playersFile.get().set(MCNSAChat.serverName+"-Channel", this.channel);
		this.playersFile.get().set("channel", this.channel);
		this.playersFile.get().set("listening", this.listening);
		this.playersFile.get().set("lastPm", this.lastPm);
		this.playersFile.get().set("modes.SEEALL", this.modes.get("SEEALL"));
		this.playersFile.get().set("modes.MUTE", this.modes.get("MUTE"));
		this.playersFile.get().set("modes.POOF", this.modes.get("POOF"));
		this.playersFile.get().set("muted", this.muted);
		this.playersFile.get().set("serversVisited", this.serversVisited);
		this.playersFile.get().set("timeoutTill", this.timeoutTill);
		this.playersFile.save();
	}
	public void changeChannel(String newChannel) {
		this.channel = newChannel.substring(0, 1).toUpperCase() + newChannel.substring(1);
	}
	public int channelListen(String channel){
		if (listening.contains(channel.toLowerCase())){
			listening.remove(channel.toLowerCase());
			Network.updatePlayer(this);
			return 1;
		}
		else if (ChannelManager.getChannel(channel) != null && !Permissions.checkReadPerm(ChannelManager.getChannel(channel).read_permission, name)){
			return 2;
		}
		else {
			listening.add(channel.toLowerCase());
			Network.updatePlayer(this);
			return 3;
		}
	}
	public void addListen(String channel) {
		if (!listening.contains(channel.toLowerCase())) {
			listening.add(channel.toLowerCase());
		}
		Network.updatePlayer(this);
	}
}
