package com.mcnsa.chat.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import net.milkbowl.vault.chat.Chat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.mcnsa.chat.file.Channels;
import com.mcnsa.chat.plugin.abstracts.AbstractChatCommand;
import com.mcnsa.chat.plugin.listeners.PlayerListener;
import com.mcnsa.chat.plugin.managers.ChannelManager;
import com.mcnsa.chat.plugin.managers.CommandManager;
import com.mcnsa.chat.plugin.managers.DatabaseManager;
import com.mcnsa.chat.plugin.managers.PermissionManager;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.plugin.utils.ConsoleLogging;
import com.mcnsa.chat.plugin.utils.FileLog;
import com.mcnsa.chat.type.ChatChannel;
import com.mcnsa.chat.type.ChatPlayer;

public class MCNSAChat extends JavaPlugin {
    public static final String userNameMatch = "\\/u\\/(\\w|-)+";
    public static final String subredditNameMatch = "\\/r\\/(\\w|-)+";
    public static final UUID consoleUUID = UUID.fromString("fb7afb6d-e9ea-4739-9893-616134617808");
    public static String serverName;
    public static String shortCode;
    public static Boolean multiServer;
    public PlayerManager playerManager;
    public ChannelManager channelManager;
    public Channels channels;
    public static FileLog logs;
    public static Chat chat;
    public static MCNSAChat plugin;
    public static boolean isLockdown;
    public static int lockdownTimerID;
    public static String lockdownReason;
    public static long lockdownUnlockTime;
    public static String bannedWordsSilent;
    public static String bannedWordsNotify;
    public static List<String> ranking;
    public static Random random;

    public void onEnable() {
	plugin = this;

	// Setup Vault Permissions hook
	if (!PermissionManager.setupPermissions())
	    ConsoleLogging.severe("Could not load permissions, is Vault installed?");

	// Setup Vault Chat hook
	if (!setupVaultChat())
	    ConsoleLogging.severe("Could not load permissions, is Vault installed?");

	// Load the configs
	ConsoleLogging.info("Loading config");

	// TODO Do we need to save config first?
	this.saveDefaultConfig();
	// TODO Swap to function
	MCNSAChat.serverName = this.getConfig().getString("ServerName");
	MCNSAChat.shortCode = this.getConfig().getString("ShortCode");
	MCNSAChat.multiServer = this.getConfig().getBoolean("multiServer");
	MCNSAChat.isLockdown = this.getConfig().getBoolean("Lockdown");
	MCNSAChat.lockdownReason = this.getConfig().getString("lockdown-reason");
	MCNSAChat.lockdownUnlockTime = this.getConfig().getLong("lockdown-unlock-time");
	bannedWordsNotify = this.getConfig().getString("banned-notify", "");
	bannedWordsSilent = this.getConfig().getString("banned-silent", "");
	ranking = Arrays.asList(((String) getConfig().get("ranking-string", "&7 &2 &3 &e &6 &c &d &8 &b")).split(" "));
	if (new Date().getTime() > MCNSAChat.lockdownUnlockTime) {
	    MCNSAChat.isLockdown = false;
	}

	// TODO swap to static?
	MCNSAChat.logs = new FileLog();

	// Check to see if the directory for players exists
	File playerFolder = new File("plugins/MCNSAChat/Players");
	if (!playerFolder.exists())
	    playerFolder.mkdir();

	// Finished config loading
	ConsoleLogging.info("Config Loaded");
	ConsoleLogging.info("Server name is: " + MCNSAChat.serverName);
	ConsoleLogging.info("Server shortcode is: " + MCNSAChat.shortCode);

	// Notify if the multiServer is set to true or false
	if (MCNSAChat.multiServer) {
	    ConsoleLogging.info("Server is running in Multi Server mode.");
	} else {
	    ConsoleLogging.info("Server is running in Single Server mode.");
	}

	// Load up the playermanager
	playerManager = new PlayerManager();
	// Load up the ChannelManager
	channelManager = new ChannelManager();

	// load up the saved channels
	this.channels = new Channels();
	this.channels.saveDefault();
	ConsoleLogging.info("Loading channels");
	loadChannels();

	// Load up the Database Manager
	DatabaseManager dbManager = new DatabaseManager();
	dbManager.enable();
	// Support for /reload
	addOnlinePlayers();
	// Start up the player listener

	// TODO necessary?
	new PlayerListener();

	// Set up a Plugin-wide random generator
	random = new Random();
	CommandManager.loadCommands();

    }

    private boolean setupVaultChat() {

	RegisteredServiceProvider<Chat> rsp = Bukkit.getServer().getServicesManager().getRegistration(Chat.class);
	chat = rsp.getProvider();
	return chat != null;
    }

    public void onDisable() {
	// TODO why reload?
	this.reloadConfig();

	// Iterate through and save all player data
	for (ChatPlayer player : PlayerManager.players) {
	    player.savePlayer();
	}

	// Safely close SQL Connection
	ConsoleLogging.info("Closing SQL connection");
	DatabaseManager.disconnect();

	// TODO why not set to null?
	// Clear players
	PlayerManager.players = new ArrayList<ChatPlayer>();

	// TODO this messes with reloading after changing config values, needs
	// fixing
	ConsoleLogging.info("Saving Channels");
	saveChannels();
	this.getConfig().set("lockdown", isLockdown);
	this.getConfig().set("lockdown-reason", lockdownReason);
	this.getConfig().set("lockdown-unlock-time", lockdownUnlockTime);

	this.saveConfig();
	ConsoleLogging.info("Disabled");
    }

    @SuppressWarnings("unchecked")
    public void loadChannels() {

	List<Map<String, ?>> channelData = (List<Map<String, ?>>) channels.get().getList("channels");
	for (Map<String, ?> channel : channelData) {
	    ChatChannel c = new ChatChannel((String) channel.get("name"), (String) channel.get("write_permission"), (String) channel.get("read_permission"), (String) channel.get("alias"),
		    (ChatColor.valueOf((String) channel.get("color"))));
	    List<String> modes = (List<String>) channel.get("modes");
	    for (String mode : modes) {
		if (c.modes.containsKey(mode.toUpperCase())) {
		    c.modes.remove(mode.toUpperCase());
		    c.modes.put(mode.toUpperCase(), true);
		}
	    }
	    ChannelManager.addChannel(c);
	    // Add alias to channelmanager
	    if (c.alias != null) {
		ChannelManager.channelAlias.put(c.alias, c.name);
	    }
	    c = null;
	}
    }

    public void saveChannels() {
	ArrayList<ChatChannel> channels = ChannelManager.getChatChannelList();
	ArrayList<HashMap<String, ?>> savedChannels = new ArrayList<HashMap<String, ?>>();
	for (ChatChannel channel : channels) {
	    HashMap<String, Object> chan = new HashMap<String, Object>();
	    if (channel.modes.get("PERSIST")) {
		// Need to save the channel
		chan.put("name", channel.name);
		chan.put("write_permission", channel.writePermission);
		chan.put("read_permission", channel.readPermission);
		chan.put("color", channel.color);
		chan.put("alias", channel.alias);

		// Sort out the modes
		ArrayList<String> modes = new ArrayList<String>();
		for (String mode : channel.modes.keySet()) {
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
	for (Player player : players) {
	    if (PlayerManager.getPlayer(player.getUniqueId(), MCNSAChat.shortCode) == null)
		PlayerManager.PlayerLogin(player.getUniqueId());
	}
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {
	ConsoleLogging.info("Size: " + CommandManager.commands.size());
	for (AbstractChatCommand c : CommandManager.commands) {
	    ConsoleLogging.info("c: " + c.name + " and label: " + label);
	    if (c.name.equalsIgnoreCase(label) || (c.aliases != null && c.aliases.contains(label))) {
		ConsoleLogging.info("Found matching command....");
		c.execute(cs, args);
	    }
	}
	return false;

    }

}
