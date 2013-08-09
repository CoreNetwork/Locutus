package com.mcnsa.chat.plugin.components;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.mcnsa.chat.networking.Network;
import com.mcnsa.chat.plugin.MCNSAChat;
import com.mcnsa.chat.plugin.annotations.Command;
import com.mcnsa.chat.plugin.annotations.ComponentInfo;
import com.mcnsa.chat.plugin.managers.ChannelManager;
import com.mcnsa.chat.plugin.managers.Permissions;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.plugin.utils.Colours;
import com.mcnsa.chat.plugin.utils.MessageSender;
import com.mcnsa.chat.type.ChatChannel;
import com.mcnsa.chat.type.ChatPlayer;

@ComponentInfo(friendlyName = "Admin",
description = "Admin commands",
permsSettingsPrefix = "admin")
public class AdminCommands {
	
	@Command(
			command = "cto",
			description = "Player chat timeout",
			permissions = {"timeout"}
			)
	public static boolean ctoList(CommandSender sender) {
		//Function lists players in timeout
		
		//Start output arraylist
		Map<String, Long> output = new HashMap<String, Long>();
		//Get players in timeout
		for (ChatPlayer player: PlayerManager.players) {
			if (player.modes.get("MUTE"))
				if (!output.containsKey(player.name))
					output.put(player.name, player.timeoutTill);
		}
		
		if (output.isEmpty()) {
			MessageSender.send("&6There is no-one currently in timeout", sender.getName());
			return true;
		}
		
		//Start the output
		MessageSender.send("&6Players in timeout", sender.getName());
				
		//Loop through players in timeout
		for (Entry<String, Long> entry : output.entrySet()) {
		    String player = entry.getKey();
		    Date timeoutTime = new Date(entry.getValue());
		    SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss dd.MMM");
		    MessageSender.send("&c"+player+" Until "+df.format(timeoutTime), sender.getName());
		}
		
		return true;
	}
	
	@Command(
			command = "cto",
			description = "Player chat timeout",
			arguments = {"Player"},
			permissions = {"timeout"}
			)
	public static boolean ctoRemove(CommandSender sender, String target) {
		
		ArrayList<ChatPlayer> targetPlayers = PlayerManager.playerSearch(target);
		if (targetPlayers.isEmpty()) {
			MessageSender.send("&cCould not find player", sender.getName());
			return true;
		}
		
		ChatPlayer targetPlayer = targetPlayers.get(0);
		
		if (!targetPlayer.modes.get("MUTE")) {
			MessageSender.send("&cThat player is not in timeout", sender.getName());
			return true;
		}

		PlayerManager.unmutePlayer(targetPlayer.name);
		MessageSender.send("&6"+targetPlayer.name+" has been removed from timeout", sender.getName());
		
		return true;
	}
	
	@Command(
			command = "cto",
			description = "Player chat timeout",
			arguments = {"Player", "time", "reason"},
			permissions = {"timeout"}
			)
	public static boolean ctoadd(CommandSender sender, String target, String time, String... reason) {
		ArrayList<ChatPlayer> targetPlayers = PlayerManager.playerSearch(target);
		if (targetPlayers.isEmpty()) {
			MessageSender.send("&cCould not find player", sender.getName());
			return true;
		}
		ChatPlayer targetPlayer = targetPlayers.get(0);
		
		if (targetPlayer.modes.get("MUTE")) {
			MessageSender.send("&cThat player already in timeout", sender.getName());
			return true;
		}
		//Build reason
		StringBuffer sb = new StringBuffer();
		for (String reasonPart: reason) {
			if (sb.length() < 1)
				sb.append(reasonPart);
			else 
				sb.append(" "+reasonPart);
		}
		//Mute and notify
		PlayerManager.mutePlayer(targetPlayer.name, time, sb.toString());
		
		//Start timer
		final String finalPlayerName = targetPlayer.name;
		long timeleft = Integer.valueOf(time) * 1200;
		Bukkit.getScheduler().scheduleSyncDelayedTask(MCNSAChat.plugin, new Runnable() {
			public void run() {
					if (PlayerManager.getPlayer(finalPlayerName, MCNSAChat.shortCode) != null && PlayerManager.getPlayer(finalPlayerName, MCNSAChat.shortCode).modes.get("MUTE")){
						PlayerManager.unmutePlayer(finalPlayerName);
					}
				}
		}, timeleft);
		
		//Send to network
		Network.timeout(targetPlayer, sb.toString(), Integer.valueOf(time));
		return true;
	}
	
	@Command(
			command = "cregister",
			description = "Register a channel with the channel manager",
			permissions = {"register"},
			arguments = {"Channel"},
			playerOnly = true
			)
	public static boolean registerChannel(CommandSender sender, String channel) {
		//Function registers a channel with the channel manager
		channel = channel.substring(0, 1).toUpperCase() + channel.substring(1);
		//Check to make sure the channel isn't already registered.
		if (ChannelManager.getChannel(channel) != null) {
			MessageSender.send("&cChannel is already registered.", sender.getName());
			return true;
		}
		
		//Create the channel
		ChatChannel Channel = new ChatChannel(channel);
		//make the channel persistent
		Channel.modes.put("PERSIST", true);
		
		//Let the sender know that its created
		MessageSender.send("&6Channel "+channel+" registered", sender.getName());
		
		//Add to channel Manager
		ChannelManager.channels.add(Channel);
		//Send to network
		Network.channelUpdate(Channel);
		return true;
	}
	
	@Command(
			command = "cmode",
			description = "Add a mode to the channel your in: Rave, Boring, or Local",
			arguments = {"action","Mode"},
			permissions = {"mode"},
			playerOnly = true
			)
	public static boolean addMode(CommandSender sender, String action, String mode) {
		//Function adds modes to the channel
		//Get the channel
		ChatChannel channel = ChannelManager.getChannel(PlayerManager.getPlayer(sender.getName(), MCNSAChat.shortCode).channel);
		if (channel == null) {
			MessageSender.send("&cChannel is not registered. Please register first by /cregister <channel>", sender.getName());
			return true;
		}
		if (action.equalsIgnoreCase("add")) {
			if (mode.equalsIgnoreCase("rave")) {
				channel.modes.put("RAVE", true);
				MessageSender.send("&6Rave mode activated!", sender.getName());
			}
			else if (mode.equalsIgnoreCase("boring")) {
				channel.modes.put("BORING", true);
				MessageSender.send("&6Boring mode activated!", sender.getName());
			}
			else if (mode.equalsIgnoreCase("local")) {
				channel.modes.put("LOCAL", true);
				MessageSender.send("&6Local mode activated!", sender.getName());
			}
		}
		else if (action.equalsIgnoreCase("del")){
			if (mode.equalsIgnoreCase("rave")) {
				channel.modes.put("RAVE", false);
				MessageSender.send("&6Rave mode deactivated!", sender.getName());
			}
			else if (mode.equalsIgnoreCase("boring")) {
				channel.modes.put("BORING", false);
				MessageSender.send("&6Boring mode deactivated!", sender.getName());
			}
			else if (mode.equalsIgnoreCase("local")) {
				channel.modes.put("LOCAL", false);
				MessageSender.send("&6Local mode deactivated!", sender.getName());
			}
		}
		else {
			MessageSender.send("&cUsage: /cmode [add|del] [rave|boring|mute|local]", sender.getName());
		}
		Network.channelUpdate(channel);
		return true;
	}

	@Command(
			command = "cedit",
			arguments = {"setting", "variable"},
			description = "Set the alias and read/write permissions fo the channel your in",
			permissions = {"modify"},
			playerOnly = true
			)
	public static boolean chanmodify(CommandSender sender,String setting, String... var) {
		//Get channel
		ChatChannel channel = ChannelManager.getChannel(PlayerManager.getPlayer(sender.getName()).channel);
		if (var[0] == null)
			var [0] = "";
		if (channel == null){
			MessageSender.send("&cChannel is not registered", sender.getName());
			return true;
		}
		if (setting.equalsIgnoreCase("alias")) {
			channel.alias = var[0];
			MessageSender.send("&6Channel alias changed to: /"+var[0], sender.getName());
		}
		else if (setting.equalsIgnoreCase("readperm")) {
			channel.read_permission = var[0];
			MessageSender.send("&6Channel read permission changed to: "+var[0], sender.getName());
		}
		else if (setting.equalsIgnoreCase("writeperm")) {
			channel.read_permission = var[0];
			MessageSender.send("&6Channel write permission changed to: "+var[0], sender.getName());
		}
		else if (setting.equalsIgnoreCase("color")) {
			channel.color = "&"+var[0];
			MessageSender.send("&6Channel color changed to: &"+var[0]+"this", sender.getName());
		}
		Network.channelUpdate(channel);
		return true;
	}
	
	@Command(
			command = "cdelete",
			permissions = "register",
			description = "Deregister a channel",
			arguments = {"Channel"}
			)
	public static boolean deregister(CommandSender sender, String channel) {
		channel = channel.substring(0, 1).toUpperCase() + channel.substring(1);
		ChatChannel chan = ChannelManager.getChannel(channel);
		if (chan == null) {
			MessageSender.send("&cChannel is not registered.", sender.getName());
			return true;
		}
		
		//Remove from channel Manager
		ChannelManager.channels.remove(chan);
		
		chan.modes.put("PERSIST", false);
		
		//Update other servers
		Network.channelUpdate(chan);
		
		//Inform user
		MessageSender.send("&cChannel: "+channel+" has been removed.", sender.getName());
		
		return true;
	}

	@Command(
			command = "say",
			arguments = {"Message"},
			permissions = {"say"},
			description = "Send a message as console"
			)
	public static boolean say(CommandSender sender, String... message) {
		//Function allows mods to speak as [Console]
		StringBuffer Message = new StringBuffer();
		Message.append(MCNSAChat.plugin.getConfig().getString("consoleSender-colour")+"["+MCNSAChat.plugin.getConfig().getString("consoleSender")+"]");
		for (String part: message) {
			Message.append(" "+part);
		}
		
		Bukkit.broadcastMessage(Colours.processConsoleColours(Message.toString()));
		return true;
	}
	
	@Command(
			command = "clock",
			arguments = {"Player"},
			description = "Lock a player from moving channels",
			permissions = {"lock"}
			)
	public static boolean clock(CommandSender sender, String player) {
		//Function locks player in their channel
		//Try and get player
		ArrayList<ChatPlayer> target = PlayerManager.playerSearch(player);
		if (target.isEmpty()) {
			MessageSender.send("&4Could not find player.", sender.getName());
			return true;
		}
		
		ChatPlayer targetPlayer = target.get(0);
		//Check to see if the player is already locked
		if (!targetPlayer.modes.get("LOCKED")) {
			//Unlock player
			targetPlayer.modes.put("LOCKED", true);
			//Inform player
			MessageSender.send("&6You have been locked in your channel", targetPlayer.name);
			//inform command sender
			MessageSender.send("&6"+targetPlayer.name+" has been locked in channel: "+targetPlayer.channel, sender.getName());
		}
		else {
			//lock player
			targetPlayer.modes.put("LOCKED", false);
			//Inform player
			MessageSender.send("&6You can now change channels", targetPlayer.name);
			//inform command sender
			MessageSender.send("&6"+targetPlayer.name+" has been unlocked", sender.getName());
		}
		Network.updatePlayer(targetPlayer);
		return true;
	}
	
	@Command(
			command = "cmove",
			arguments = {"Player", "Channel"},
			permissions = {"move"},
			description = "Move a player to a different channel"
			)
	public static boolean cmove(CommandSender sender, String player, String channel) {
		//Function moves a player to a different channel
		//Try and get player
		ArrayList<ChatPlayer> target = PlayerManager.playerSearch(player);
		if (target.isEmpty()) {
			MessageSender.send("&4Could not find player.", sender.getName());
			return true;
		}
		
		ChatPlayer targetPlayer = target.get(0);
		
		//see if its a persist channel
		if (ChannelManager.getChannel(channel) != null) {
			ChatChannel chan = ChannelManager.getChannel(channel);
			
			if (!Permissions.checkReadPerm(chan.read_permission, targetPlayer.name)) {
					MessageSender.send("&4"+targetPlayer.name+" does not have the required permissions to enter "+channel, sender.getName());
					return true;
				}
			channel = chan.name;
		}
		//Get players in channel
		String playersInChannel = ChannelManager.playersInChannel(channel);
		//We can say this player has the permissions. Lets welcome them
		targetPlayer.changeChannel(channel);
		MessageSender.send(Colours.color("&6You have been moved to "+channel+". Players here: " + playersInChannel), targetPlayer.name);

		//Update player on other servers
		Network.updatePlayer(targetPlayer);
		
		//Notify the mod
		MessageSender.send("&6"+targetPlayer.name+" has been moved into "+channel, sender.getName());
		return true;
	}
	
	@Command(
			command = "creload",
			description = "Reload chat configuration",
			permissions = {"reload"}
			)
	public static boolean reload(CommandSender sender) {
		//Reloads the config file
		MCNSAChat.plugin.reloadConfig();
		MessageSender.send("&6Chat Config reloaded", sender.getName());
		return true;
	}

	@Command(
			command = "cnet",
			description = "Cross server chat controls, use off, on, or reset",
			permissions = {"csccontrol"}
			)
	public static boolean net(CommandSender sender, String action) {
		//Function allows setting of the cross server functionality
		if (action.equalsIgnoreCase("on")) {
			if (!MCNSAChat.multiServer) {
				MCNSAChat.multiServer = true;
				MessageSender.send("&6Cross server chat turned on", sender.getName());
			}
			else {
				MessageSender.send("&6Cross server chat is already on", sender.getName());
			}
			return true;
		}
		else if (action.equalsIgnoreCase("off")) {
			MCNSAChat.multiServer = false;
			//sanity check
			if (MCNSAChat.network != null)
				MCNSAChat.network.close();
			MCNSAChat.network = null;
			MessageSender.send("&6Cross server chat turned off", sender.getName());
			PlayerManager.removeNonServerPlayers();
			return true;
		}
		else if (action.equalsIgnoreCase("reset")) {
			if (MCNSAChat.network != null)
				MCNSAChat.network.close();
			MCNSAChat.network = null;
			MessageSender.send("&6Cross server chat reset", sender.getName());
			PlayerManager.removeNonServerPlayers();
			return true;
		}
		MessageSender.send("&4Invalid arguments: use on, off, or reset", sender.getName());
		return true;
	}

	@Command(
			command = "seeall",
			description = "Vewa all channels",
			permissions = {"seeall"},
			playerOnly = true
			)
	public static boolean seeall(CommandSender sender) {
		//Function sets the mode to allow to see all channels
		//get player
		ChatPlayer player = PlayerManager.getPlayer(sender.getName(),MCNSAChat.shortCode);
		
		if (player.modes.get("SEEALL")) {
			player.modes.put("SEEALL", false);
			MessageSender.send("&6You are no longer listening to all channels", player.name);
		}
		else {
			player.modes.put("SEEALL", true);
			MessageSender.send("&6You are now listening to all channels", player.name);
		}
		return true;
	}

	@Command(
			command = "chansay",
			description = "send a message to channel via console",
			arguments = {"Channel", "message"},
			permissions = {"console"}
			)
	public static boolean consolechat(CommandSender sender, String Channel, String... rawMessage) {
				
		StringBuffer message = new StringBuffer();
		for (String part: rawMessage) {
			if (message.length() < 1)
				message.append(part);
			else {
				message.append(" "+part);
			}
		}
		
		MessageSender.consoleChat(message.toString(), Channel);
		
		return true;

	}

	@Command(
			command = "forceremove",
			description = "Strips everyone's listens for a channel",
			arguments = {"Channel"}
			)
	public static boolean stripListens(CommandSender sender, String channel) {
		//Strips everyone's listens for a channel
		for (int i = 0; i < PlayerManager.players.size(); i++) {
			ChatPlayer player = PlayerManager.players.get(i);
			if (player.listening.contains(channel)) {
				player.listening.remove(channel);
				MessageSender.send("removed "+player.name+ "from listening to "+channel, sender.getName());
				Network.updatePlayer(player);
			}
		}
		
		return true;
	}
	
}
