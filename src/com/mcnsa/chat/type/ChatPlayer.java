package com.mcnsa.chat.type;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.mcnsa.chat.plugin.MCNSAChat;
import com.mcnsa.chat.plugin.exceptions.DatabaseException;
import com.mcnsa.chat.plugin.managers.ChannelManager;
import com.mcnsa.chat.plugin.managers.DatabaseManager;
import com.mcnsa.chat.plugin.managers.PermissionManager;

public class ChatPlayer implements Serializable {

    // TODO Convert class to private variables with getters and setters
    // Remove unncessary fields
    private static final long serialVersionUID = -4493289681267602037L;
    private String name = "";
    public String server = "";
    public String channel = "";
    public UUID lastPm = null;
    public boolean firstTime = true;
    public boolean isOnline = true;
    public int timeoutID = 0;
    public String leaveMessage = "";
    public Map<String, Boolean> modes = new HashMap<String, Boolean>();
    private ArrayList<String> listening;
    private ArrayList<UUID> muted = new ArrayList<UUID>();
    private ArrayList<String> serversVisited = new ArrayList<String>();
    public long timeoutTill = 0;
    public long loginTime = 0;
    public String formatted;
    public UUID id;

    @Deprecated
    public ChatPlayer(String username) {
	// Player name
	this.name = username;
	// Player server
	this.server = MCNSAChat.shortCode;

	this.loginTime = new Date().getTime();

	try {
	    // Check to see if they actually exist
	    ResultSet results = DatabaseManager.accessQuery("SELECT COUNT(*) FROM chat_players WHERE player = ?", this.getUUID().toString());
	    int size = results.getInt(1);
	    results = DatabaseManager.accessQuery("SELECT * FROM chat_players WHERE player = ?", this.getUUID().toString());// ArrayList<HashMap<String,
															    // Object>>
															    // results
															    // =
															    // DatabaseManager.accessQuery("SELECT * FROM chat_players WHERE player = ?",
															    // this.name);
	    if (size == 0) {
		this.channel = MCNSAChat.plugin.getConfig().getString("defaultChannel");
		this.listening = (ArrayList<String>) MCNSAChat.plugin.getConfig().getList("defaultListen");
		this.modes = new HashMap<String, Boolean>();
		this.firstTime = true;
		this.modes.put("SEEALL", false);
		this.modes.put("MUTE", false);
		this.modes.put("POOF", false);
		this.modes.put("LOCKED", false);
		this.modes.put("S-MUTE", false);
		this.lastPm = null;
	    } else {

		// Load simple
		// lastpm, timeouttill, channel
		this.channel = results.getString("channel");

		// TODO DB
		this.lastPm = UUID.fromString(results.getString("lastpm"));
		this.timeoutTill = results.getLong("timeoutTill");
		this.firstTime = false;
		// Load listening channels
		this.listening = new ArrayList<String>();
		results = DatabaseManager.accessQuery("SELECT * FROM chat_playerchannels WHERE playerName = ?", this.getUUID().toString());
		while (results.next()) {
		    ResultSet newResult = DatabaseManager.accessQuery("SELECT COUNT(*) FROM chat_channels WHERE id = ?", results.getInt("channelID"));
		    size = newResult.getInt(1);
		    newResult = DatabaseManager.accessQuery("SELECT * FROM chat_channels WHERE id = ?", results.getInt("channelID"));
		    if (size > 0) {
			this.listening.add(newResult.getString("name"));
		    }
		}

		// Load modes

		this.modes = new HashMap<String, Boolean>();
		this.modes.put("SEEALL", false);
		this.modes.put("MUTE", false);
		this.modes.put("S-MUTE", false);
		this.modes.put("POOF", false);
		this.modes.put("LOCKED", false);
		results = DatabaseManager.accessQuery("SELECT * FROM chat_modes WHERE playerName = ?", this.name);
		while (results.next()) {
		    // if (this.modes.containsKey((String)r.get("modeName")))
		    // this.modes.remove((String)r.get("modeName"));
		    this.modes.put((results.getString("modeName")).toUpperCase(), results.getBoolean("modeStatus"));

		}

		// Load muted

		this.muted = new ArrayList<UUID>();
		results = DatabaseManager.accessQuery("SELECT * FROM chat_mutedplayers WHERE muteePlayer = ?", this.name);
		while (results.next()) {
		    this.muted.add(UUID.fromString(results.getString("mutedPlayer")));
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

    public ChatPlayer(String name2, String server2, String channel2, UUID lastPm2, HashMap<String, Boolean> modes2, ArrayList<String> listening2, ArrayList<String> serversVisited2) {
	this.name = name2;
	this.server = server2;
	this.channel = channel2;
	this.lastPm = lastPm2;
	this.modes = modes2;
	this.listening = listening2;
	this.serversVisited = serversVisited2;
	this.muted = new ArrayList<UUID>();
    }

    public ChatPlayer(UUID uuid) {
	this.id = uuid;
	// Player name
	this.name = Bukkit.getPlayer(uuid).getDisplayName();
	// Player server
	this.server = MCNSAChat.shortCode;

	this.loginTime = new Date().getTime();

	try {
	    // Check to see if they actually exist
	    ResultSet results = DatabaseManager.accessQuery("SELECT COUNT(*) FROM chat_players WHERE player = ?", this.name);
	    int size = results.getInt(1);
	    results = DatabaseManager.accessQuery("SELECT * FROM chat_players WHERE player = ?", this.name);// ArrayList<HashMap<String,
													    // Object>>
													    // results
													    // =
													    // DatabaseManager.accessQuery("SELECT * FROM chat_players WHERE player = ?",
													    // this.name);
	    if (size == 0) {
		this.channel = MCNSAChat.plugin.getConfig().getString("defaultChannel");
		this.listening = (ArrayList<String>) MCNSAChat.plugin.getConfig().getList("defaultListen");
		this.modes = new HashMap<String, Boolean>();
		this.firstTime = true;
		this.modes.put("SEEALL", false);
		this.modes.put("MUTE", false);
		this.modes.put("POOF", false);
		this.modes.put("LOCKED", false);
		this.modes.put("S-MUTE", false);
		this.muted = new ArrayList<UUID>();
		this.lastPm = null;
	    } else {

		// Load simple
		// lastpm, timeouttill, channel
		this.channel = results.getString("channel");
		this.lastPm = UUID.fromString(results.getString("lastpm"));
		this.timeoutTill = results.getLong("timeoutTill");
		this.firstTime = false;
		// Load listening channels
		this.listening = new ArrayList<String>();
		results = DatabaseManager.accessQuery("SELECT * FROM chat_playerchannels WHERE playerName = ?", this.name);
		while (results.next()) {
		    ResultSet newResult = DatabaseManager.accessQuery("SELECT COUNT(*) FROM chat_channels WHERE id = ?", results.getInt("channelID"));
		    size = newResult.getInt(1);
		    newResult = DatabaseManager.accessQuery("SELECT * FROM chat_channels WHERE id = ?", results.getInt("channelID"));
		    if (size > 0) {
			this.listening.add(newResult.getString("name"));
		    }
		}

		// Load modes

		this.modes = new HashMap<String, Boolean>();
		this.modes.put("SEEALL", false);
		this.modes.put("MUTE", false);
		this.modes.put("S-MUTE", false);
		this.modes.put("POOF", false);
		this.modes.put("LOCKED", false);
		results = DatabaseManager.accessQuery("SELECT * FROM chat_modes WHERE playerName = ?", this.name);
		while (results.next()) {
		    // if (this.modes.containsKey((String)r.get("modeName")))
		    // this.modes.remove((String)r.get("modeName"));
		    this.modes.put((results.getString("modeName")).toUpperCase(), results.getBoolean("modeStatus"));

		}

		this.lastPm = UUID.fromString(results.getString("lastPM"));
		// Load muted

		this.muted = new ArrayList<UUID>();
		results = DatabaseManager.accessQuery("SELECT * FROM chat_mutedplayers WHERE muteePlayer = ?", this.name);
		while (results.next()) {
		    this.muted.add(UUID.fromString(results.getString("mutedPlayer")));
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

    public void savePlayer() {
	try {
	    // Insert the simple values first
	    DatabaseManager.updateQuery("DELETE FROM chat_Players where player = ?", this.getUUID().toString());
	    DatabaseManager.updateQuery("INSERT INTO chat_players (player, channel, lastpm, timeouttill, lastLogin) VALUES (?,?,?,?,?)", this.getUUID().toString(), this.channel,
		    (this.lastPm == null ? "" : this.lastPm.toString()), this.timeoutTill, this.loginTime);

	    // Clear all chat_Playerchannels for that player
	    DatabaseManager.updateQuery("DELETE FROM chat_Playerchannels where playerName = ?", this.getUUID().toString());

	    // Then add the new ones
	    for (String channel : this.listening) {
		ResultSet results = DatabaseManager.accessQuery("SELECT COUNT(*) FROM chat_channels WHERE name = ?", channel);
		int size = results.getInt(1);
		results = DatabaseManager.accessQuery("SELECT * FROM chat_channels WHERE name = ?", channel);
		if (size == 0) {
		    DatabaseManager.updateQuery("INSERT INTO chat_channels VALUES (NULL, ?)", channel);
		}
		results = DatabaseManager.accessQuery("SELECT * FROM chat_channels WHERE name = ?", channel);
		int id = results.getInt("id");
		DatabaseManager.updateQuery("INSERT INTO chat_Playerchannels VALUES(?,?)", this.getUUID().toString(), id);
	    }

	    // Now modes
	    // Clear all first
	    DatabaseManager.updateQuery("DELETE FROM chat_Modes where playerName = ?", this.getUUID().toString());

	    // Iterate through and add all
	    for (Entry<String, Boolean> s : this.modes.entrySet()) {
		if (s.getKey() == null)
		    continue;
		DatabaseManager.updateQuery("INSERT INTO chat_Modes VALUES(?,?,?)", this.getUUID().toString(), s.getKey(), s.getValue());
	    }

	    // Now mutes

	    // Clear all
	    DatabaseManager.updateQuery("DELETE FROM chat_MutedPlayers where muteePlayer = ?", this.getUUID().toString());

	    // And add new
	    for (UUID id : muted) {
		DatabaseManager.updateQuery("INSERT INTO chat_MutedPlayers VALUES(?,?)", this.getUUID().toString(), id.toString());
	    }

	} catch (DatabaseException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (SQLException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    public void changeChannel(String newChannel) {
	this.channel = newChannel.substring(0, 1).toUpperCase() + newChannel.substring(1);
    }

    public int channelListen(String channel) {
	if (listening.contains(channel.toLowerCase())) {
	    listening.remove(channel.toLowerCase());
	    return 1;
	} else if (ChannelManager.getChannel(channel) != null && !PermissionManager.checkPermission(ChannelManager.getChannel(channel).readPermission, this)) {
	    return 2;
	} else {
	    listening.add(channel.toLowerCase());
	    return 3;
	}
    }

    public void addListen(String channel) {
	if (!listening.contains(channel.toLowerCase())) {
	    listening.add(channel.toLowerCase());
	}
    }

    public void write(DataOutputStream out) throws IOException {
	out.writeUTF(this.name);
	out.writeUTF(this.server);
	out.writeUTF(this.channel);
	out.writeUTF(this.formatted);
	if (this.lastPm == null)
	    out.writeUTF("null");
	else
	    out.writeUTF(this.lastPm.toString());
	if (this.modes.get("SEEALL")) {
	    out.writeInt(1);
	} else {
	    out.writeInt(0);
	}
	if (this.modes.get("MUTE")) {
	    out.writeInt(1);
	} else {
	    out.writeInt(0);
	}
	if (this.modes.get("POOF")) {
	    out.writeInt(1);
	} else {
	    out.writeInt(0);
	}
	if (this.modes.get("LOCKED")) {
	    out.writeInt(1);
	} else {
	    out.writeInt(0);
	}
	if (this.modes.get("S-MUTE")) {
	    out.writeInt(1);
	} else {
	    out.writeInt(0);
	}
	out.writeInt(this.listening.size());
	for (String listen : this.listening) {
	    out.writeUTF(listen);
	}
	out.writeInt(this.serversVisited.size());
	for (String visited : this.serversVisited) {
	    out.writeUTF(visited);
	}
    }

    public static ChatPlayer read(DataInputStream in) throws IOException {
	String name = in.readUTF();
	String server = in.readUTF();
	String channel = in.readUTF();
	String formatted = in.readUTF();
	UUID lastPm = UUID.fromString(in.readUTF());

	HashMap<String, Boolean> modes = new HashMap<String, Boolean>();
	if (in.readInt() == 1) {
	    modes.put("SEEALL", true);
	} else {
	    modes.put("SEEALL", false);
	}
	if (in.readInt() == 1) {
	    modes.put("MUTE", true);
	} else {
	    modes.put("MUTE", false);
	}
	if (in.readInt() == 1) {
	    modes.put("POOF", true);
	} else {
	    modes.put("POOF", false);
	}
	if (in.readInt() == 1) {
	    modes.put("LOCKED", true);
	} else {
	    modes.put("LOCKED", false);
	}
	if (in.readInt() == 1) {
	    modes.put("S-MUTE", true);
	} else {
	    modes.put("S-MUTE", false);
	}

	ArrayList<String> listening = new ArrayList<String>();
	ArrayList<String> serversVisited = new ArrayList<String>();

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

    public boolean isListening(String channel) {
	return listening.contains(channel.toLowerCase());
    }

    public void listenTo(String channel) {
	listening.add(channel.toLowerCase());
    }

    public boolean hasMuted(UUID player) {
	return muted.contains(player);
    }

    public boolean hasMuted(ChatPlayer player) {
	return muted.contains(player.getUUID());
    }

    public boolean hasMuted(Player player) {
	return muted.contains(player.getUniqueId());
    }

    public void mutePlayer(UUID id) {
	muted.add(id);
    }

    public void mutePlayer(Player p) {
	muted.add(p.getUniqueId());
    }

    public void mutePlayer(ChatPlayer cp) {
	muted.add(cp.getUUID());
    }

    public boolean hasVisited(String server) {
	return serversVisited.contains(server);
    }

    public void visitServer(String server) {
	serversVisited.add(server);
    }

    public void stopListening(String channel2) {
	listening.remove(channel2);

    }

    public ArrayList<String> getChannels() {
	return listening;
    }

    public UUID getUUID() {
	return this.id;
    }

    public String getName() {
	return this.name;
    }

    public void unmutePlayer(ChatPlayer mutedPlayer) {
	muted.remove(mutedPlayer.getUUID());
    }

    public void unmutePlayer(Player mutedPlayer) {
	muted.remove(mutedPlayer.getUniqueId());

    }

    public void unmutePlayer(UUID mutedPlayer) {
	muted.remove(mutedPlayer);

    }

}
