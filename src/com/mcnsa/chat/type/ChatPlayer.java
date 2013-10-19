package com.mcnsa.chat.type;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mcnsa.chat.file.Players;
import com.mcnsa.chat.networking.Network;
import com.mcnsa.chat.plugin.MCNSAChat;
import com.mcnsa.chat.plugin.exceptions.DatabaseException;
import com.mcnsa.chat.plugin.managers.ChannelManager;
import com.mcnsa.chat.plugin.managers.DatabaseManager;
import com.mcnsa.chat.plugin.managers.Permissions;
import com.mcnsa.chat.plugin.utils.Colours;
import com.mcnsa.chat.plugin.utils.ConsoleLogging;

public class ChatPlayer implements Serializable{

	private static final long serialVersionUID = -4493289681267602037L;
	public String name = "";
	public String server = "";
	public String channel = "";
	public String lastPm = "";
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
		
		
		
		if (MCNSAChat.isSQL)
		{
			try {
				//Check to see if they actually exist
				ResultSet results = DatabaseManager.accessQuery("SELECT COUNT(*) FROM chat_players WHERE player = ?", this.name);
				int size = results.getInt(1);
				results = DatabaseManager.accessQuery("SELECT * FROM chat_players WHERE player = ?", this.name);//ArrayList<HashMap<String, Object>> results = DatabaseManager.accessQuery("SELECT * FROM chat_players WHERE player = ?", this.name);
				if (size == 0)
				{
					this.channel = MCNSAChat.plugin.getConfig().getString("defaultChannel");
					this.listening = (ArrayList<String>) MCNSAChat.plugin.getConfig().getList("defaultListen");
					this.modes = new HashMap<String, Boolean>();
					this.modes.put("SEEALL", false);
					this.modes.put("MUTE", false);
					this.modes.put("POOF", false);
					this.modes.put("LOCKED", false);
					this.lastPm = null;
				}
				else
				{

					//Load simple
					//lastpm, timeouttill, channel
					this.channel = results.getString("channel");
					this.lastPm = results.getString("lastpm");
					this.timeoutTill =  results.getLong("timeoutTill");
					
					//Load listening channels
					this.listening = new ArrayList<String>();
					results = DatabaseManager.accessQuery("SELECT * FROM chat_playerchannels WHERE playerName = ?", this.name);
					while(results.next())
					{
						ResultSet newResult = DatabaseManager.accessQuery("SELECT COUNT(*) FROM chat_channels WHERE id = ?", results.getInt("channelID"));
						size = newResult.getInt(1);
						newResult = DatabaseManager.accessQuery("SELECT * FROM chat_channels WHERE id = ?", results.getInt("channelID"));
						if (size > 0)
						{
							this.listening.add( newResult.getString("name"));
						}
					}
					
					//Load modes

					this.modes = new HashMap<String, Boolean>();
					this.modes.put("SEEALL", false);
					this.modes.put("MUTE", false);
					this.modes.put("POOF", false);
					this.modes.put("LOCKED", false);
					results = DatabaseManager.accessQuery("SELECT * FROM chat_modes WHERE playerName = ?", this.name);
					while(results.next())
					{
							//if (this.modes.containsKey((String)r.get("modeName")))
							//	this.modes.remove((String)r.get("modeName"));
							this.modes.put((results.getString("modeName")).toUpperCase(), results.getBoolean("modeStatus"));
							
					}
					
					
					//Load muted

					this.muted = new ArrayList<String>();
					results = DatabaseManager.accessQuery("SELECT * FROM chat_mutedplayers WHERE muteePlayer = ?", this.name);
					while(results.next())
					{
						this.muted.add( results.getString("mutedPlayer"));
					}
				}

			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
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
			} else {
				
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
			playersFile.save();
		}
		this.formatted = Colours.PlayerPrefix(name)+this.name;
	}
	public ChatPlayer(String name2, String server2, String channel2, String lastPm2, HashMap<String, Boolean> modes2, ArrayList<String> listening2, ArrayList<String> serversVisited2) {
		this.name = name2;
		this.server = server2;
		this.channel = channel2;
		this.lastPm = lastPm2;
		this.modes = modes2;
		this.listening = listening2;
		this.serversVisited = serversVisited2;
	}
	public void savePlayer() {
		if (this.playersFile == null) {
			this.playersFile = new Players(this.name);
		}
		if (MCNSAChat.isSQL)
		{
			try {
				//Insert the simple values first
				if (this.lastPm == null)
					this.lastPm = "";
				DatabaseManager.updateQuery("DELETE FROM chat_Players where player = ?", this.name);
				DatabaseManager.updateQuery("INSERT INTO chat_players (player, channel, lastpm, timeouttill) VALUES (?,?,?,?)", this.name, this.channel, this.lastPm, this.timeoutTill);
				
				//Clear all chat_Playerchannels for that player
				DatabaseManager.updateQuery("DELETE FROM chat_Playerchannels where playerName = ?", this.name);
				
				//Then add the new ones
				for(String channel : this.listening)
				{
					ResultSet results = DatabaseManager.accessQuery("SELECT COUNT(*) FROM chat_channels WHERE name = ?", channel);
					int size = results.getInt(1);
					results = DatabaseManager.accessQuery("SELECT * FROM chat_channels WHERE name = ?", channel);
					if (size == 0)
					{
						DatabaseManager.updateQuery("INSERT INTO chat_channels VALUES (NULL, ?)", channel);
					}
					results = DatabaseManager.accessQuery("SELECT * FROM chat_channels WHERE name = ?", channel);
					int id =  results.getInt("id");
					DatabaseManager.updateQuery("INSERT INTO chat_Playerchannels VALUES(?,?)", this.name, id);
				}
				
				//Now modes
				//Clear all first
				DatabaseManager.updateQuery("DELETE FROM chat_Modes where playerName = ?", this.name);
				
				//Iterate through and add all
				for(Entry<String, Boolean> s :this.modes.entrySet())
				{
					if (s.getKey() == null)
						continue;
					DatabaseManager.updateQuery("INSERT INTO chat_Modes VALUES(?,?,?)", this.name, s.getKey(), s.getValue());
				}
				
				//Now mutes
				
				//Clear all
				DatabaseManager.updateQuery("DELETE FROM chat_MutedPlayers where muteePlayer = ?", this.name);
				
				//And add new
				for(String s : muted)
				{
					DatabaseManager.updateQuery("INSERT INTO chat_MutedPlayers VALUES(?,?)", this.name, s);
				}

			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			this.playersFile.get().set(MCNSAChat.serverName+"-Channel", this.channel);
			this.playersFile.get().set("channel", this.channel);
			this.playersFile.get().set("lastPm", this.lastPm);
			
			this.playersFile.get().set("listening", this.listening);
			
			this.playersFile.get().set("modes.SEEALL", this.modes.get("SEEALL"));
			this.playersFile.get().set("modes.MUTE", this.modes.get("MUTE"));
			this.playersFile.get().set("modes.POOF", this.modes.get("POOF"));
			this.playersFile.get().set("modes.LOCKED", this.modes.get("LOCKED"));
			
			this.playersFile.get().set("muted", this.muted);
			this.playersFile.get().set("serversVisited", this.serversVisited);
			this.playersFile.get().set("timeoutTill", this.timeoutTill);
			this.playersFile.save();
		}
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
	
	public void write(DataOutputStream out) throws IOException{
		out.writeUTF(this.name);
		out.writeUTF(this.server);
		out.writeUTF(this.channel);
		out.writeUTF(this.formatted);
		if (this.lastPm == null)
			out.writeUTF("null");
		else
			out.writeUTF(this.lastPm);
		if (this.modes.get("SEEALL")) {out.writeInt(1); } else { out.writeInt(0); }
		if (this.modes.get("MUTE")) {out.writeInt(1); } else { out.writeInt(0); }
		if (this.modes.get("POOF")) {out.writeInt(1); } else { out.writeInt(0); }
		if (this.modes.get("LOCKED")) {out.writeInt(1); } else { out.writeInt(0); }
		out.writeInt(this.listening.size());
		for (String listen: this.listening) {
			out.writeUTF(listen);
			}
		out.writeInt(this.serversVisited.size());
		for (String visited: this.serversVisited){
			out.writeUTF(visited);
		}
	}
	
	public static ChatPlayer read(DataInputStream in) throws IOException{
		String name = in.readUTF();
		String server = in.readUTF();
		String channel = in.readUTF();
		String formatted = in.readUTF();
		String lastPm = in.readUTF();
		
		HashMap<String, Boolean> modes = new HashMap<String, Boolean>();
		if (in.readInt() == 1) { modes.put("SEEALL", true); } else { modes.put("SEEALL", false);}
		if (in.readInt() == 1) { modes.put("MUTE", true); } else { modes.put("MUTE", false);}
		if (in.readInt() == 1) { modes.put("POOF", true); } else { modes.put("POOF", false);}
		if (in.readInt() == 1) { modes.put("LOCKED", true); } else { modes.put("LOCKED", false);}
		
		ArrayList <String> listening = new ArrayList<String>();
		ArrayList <String> serversVisited = new ArrayList<String>();
		
		int size = in.readInt();
		for (int i = 0; i < size; i++) {
			listening.add(in.readUTF());
		}
		
		int size2 = in.readInt();
		for (int i = 0; i < size2; i++) {
			serversVisited.add(in.readUTF());
		}
		if (lastPm.equals("null"))
			lastPm = null;
		ChatPlayer cp = new ChatPlayer(name, server, channel, lastPm, modes, listening, serversVisited);
		cp.formatted = formatted;
		return cp;
	}
}
