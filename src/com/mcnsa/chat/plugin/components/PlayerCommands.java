package com.mcnsa.chat.plugin.components;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.chat.networking.Network;
import com.mcnsa.chat.plugin.MCNSAChat;
import com.mcnsa.chat.plugin.annotations.Command;
import com.mcnsa.chat.plugin.annotations.ComponentInfo;
import com.mcnsa.chat.plugin.annotations.DatabaseTableInfo;
import com.mcnsa.chat.plugin.exceptions.ChatCommandException;
import com.mcnsa.chat.plugin.exceptions.DatabaseException;
import com.mcnsa.chat.plugin.managers.ChannelManager;
import com.mcnsa.chat.plugin.managers.DatabaseManager;
import com.mcnsa.chat.plugin.managers.Permissions;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.plugin.utils.Colours;
import com.mcnsa.chat.plugin.utils.ConsoleLogging;
import com.mcnsa.chat.plugin.utils.MessageSender;
import com.mcnsa.chat.type.ChatChannel;
import com.mcnsa.chat.type.ChatPlayer;

@ComponentInfo(friendlyName = "Player",
description = "Player commands",
permsSettingsPrefix = "player")
public class PlayerCommands {
	@Command(command = "c",
			arguments = {"Channel"},
			description = "Move to a channel",
			permissions = {"move"},
			playerOnly = true)
	public static boolean channelChange(CommandSender sender, String channel) throws ChatCommandException{
		
		channel = Colours.stripColor(channel);
		//Get the player
		ChatPlayer player = PlayerManager.getPlayer(sender.getName(), MCNSAChat.shortCode);
		//Check to see if the Player is locked in channel
		if (player.modes.get("LOCKED")) {
			MessageSender.send("&cYou are locked in this channel", player.name);
			return true;
		}
		
		//see if its a persist channel
		if (ChannelManager.getChannel(channel) != null) {
			ChatChannel chan = ChannelManager.getChannel(channel);
			
			if (!Permissions.checkReadPerm(chan.read_permission, player.name)) {
					MessageSender.send("&4You do not have the required permissions to enter this channel", player.name);
					return true;
				}
			channel = chan.name;
		}
		
		String tempTargetChannel = ChannelManager.getTempChannel(channel);
		if (tempTargetChannel != null)
		{
			channel = tempTargetChannel;
		}
		else
		{
			ChannelManager.addTempChannel(channel);
		}
		//Get players in channel
		String playersInChannel = ChannelManager.playersInChannel(channel);
		//We can say this player has the permissions. Lets welcome them
		player.changeChannel(channel);
		Network.updatePlayer(player);
		MessageSender.send(Colours.color("&6Welcome to the "+channel+" channel. Players here: " + playersInChannel), player.name);
	
		return true;
		
	}
	
	@Command(command = "cmute",
			aliases = {"ignore", "mute", "cmute"},
			arguments = {"Player"},
			description = "Mute a player",
			permissions = {"mute"},
			playerOnly = true)
	public static boolean mutePlayer(CommandSender sender, String mutedPlayer) {
		//Get the player sending command
		ChatPlayer playerSending = PlayerManager.getPlayer(sender.getName(), MCNSAChat.shortCode);
		
		//Try and find the player they are trying to mute
		ArrayList<ChatPlayer> playerResults = PlayerManager.playerSearch(mutedPlayer);
		if (!playerResults.isEmpty()) {
			//Get the first result
			mutedPlayer = playerResults.get(0).name;
		}
		//See if the player they are trying to mute is already muted
		if (playerSending.muted.contains(mutedPlayer)) {
			//Player is already muted, Un mute them
			playerSending.muted.remove(mutedPlayer);
			
			//Let the player know
			MessageSender.send("&6"+mutedPlayer+" has been unmuted", playerSending.name);
		}
		else {
			playerSending.muted.add(mutedPlayer);
			//Let the player know
			MessageSender.send("&6"+mutedPlayer+" has been muted", playerSending.name);
		}

		//Update player on other servers
		Network.updatePlayer(playerSending);
		
		return true;
	}
	
	@Command(command = "clist",
			aliases = {"Channels"},
			description = "Get a list of channels",
			permissions = {"list"},
			playerOnly = false)
	public static boolean channelList(CommandSender sender) {
		//Get the player
		ChatPlayer player = PlayerManager.getPlayer(sender.getName(), MCNSAChat.shortCode);
		ArrayList<String> channels;
		if (sender instanceof Player)
		{
			channels = ChannelManager.getChannelList(player);
		}
		else
		{
			channels = ChannelManager.getChannelList();
		}
		//Get the channel list
		
		StringBuffer message = new StringBuffer();
		for (String channel: channels) {
			if (message.length() < 1)
				if (ChannelManager.getChannel(channel) != null)
					message.append(ChannelManager.getChannel(channel).color + channel + "&f");
				else 
					message.append(channel);
			else
				if (ChannelManager.getChannel(channel) != null)
					message.append(", "+ChannelManager.getChannel(channel).color + channel + "&f");
				else 
					message.append(", "+channel);
		}
		if (sender instanceof Player)
		{
			MessageSender.send("Channels: "+message.toString(), player.name);
		}
		else
		{
			MessageSender.send("Channels: "+message.toString(), "console");
		}
		return true;
	}
	
	@Command(command = "listen",
			description = "Listen to a channel",
			arguments = {"channel"},
			permissions = {},
			playerOnly = true)
	public static boolean channelListen(CommandSender sender, String Channel) {
		Channel = Channel.substring(0, 1).toUpperCase() + Channel.substring(1);
		String channel = Channel;
		//Get the player
		ChatPlayer player = PlayerManager.getPlayer(sender.getName(), MCNSAChat.shortCode);
		
		//Try and get the channel
		ChatChannel targetChannel = ChannelManager.getChannel(Channel);
		if (targetChannel != null) {
			//Channel is persistent. Check perms
			if (!Permissions.checkReadPerm(targetChannel.read_permission, player.name)) {
				//Player does not have read permission
				MessageSender.send("&4You do not have permission to listen to: "+targetChannel.color+targetChannel.name, player.name);
				return true;
			}
			channel = targetChannel.color+targetChannel.name;
		}
		String tempTargetChannel = ChannelManager.getTempChannel(Channel);
		if (tempTargetChannel != null)
		{
			channel = tempTargetChannel;
		}
		else
		{
			ChannelManager.addTempChannel(channel);
		}
		//Change the channel
		int result = player.channelListen(channel);
		if (result == 3) {
			MessageSender.send("&6You are now listening to "+channel, player.name);
			Network.updatePlayer(player);
		}
		else if (result == 1) {
			MessageSender.send("&6You have stopped listening to "+channel, player.name);
			Network.updatePlayer(player);
		}
		else  if (result == 2){
			MessageSender.send("&cYou cannot listen to this channel "+channel, player.name);
			Network.updatePlayer(player);
		}
		
		//Update player on other servers
		Network.updatePlayer(player);
		
		return true;
	}
	@Command(command = "message",
			aliases= {"msg", "whisper", "tell", "w"},
			arguments = {"Player", "Message"},
			description = "Message a player",
			permissions = {"msg"})
	public static boolean message(CommandSender sender, String player, String... Message) {
		
		StringBuffer messageString = new StringBuffer();
		for (String message: Message) {
			messageString.append(message+" ");
		}
		String formattedString = messageString.toString();
		if (!Permissions.useColours(sender.getName()))
			messageString = Colours.stripColor(formattedString);
		//sending to console support
		if (player.equalsIgnoreCase("console")) {
			//Try and get player
			ChatPlayer playerSender = PlayerManager.getPlayer(sender.getName());
			if (playerSender.modes.get("MUTE"))
			{
				MessageSender.send("&c You are in timeout. Please try again later", playerSender.name);
				return true;
			} else if (playerSender.modes.get("S-MUTE"))
			{
				MessageSender.sendPM(formattedString, sender.getName(), player.toUpperCase());
				
				return true;
			}
			MessageSender.sendPM(formattedString, sender.getName(), player.toUpperCase());
			MessageSender.recievePM(formattedString, sender.getName(), player.toUpperCase());
			
			return true;
			
			
		}
		else {
			if (sender instanceof Player)
			{
				ChatPlayer playerSender = PlayerManager.getPlayer(sender.getName());
				if (playerSender.modes.get("MUTE"))
				{
					MessageSender.send("&c You are in timeout. Please try again later", playerSender.name);
					return true;
				}
				else if(playerSender.modes.get("S-MUTE"))
				{
					ArrayList<ChatPlayer> targetPlayers = PlayerManager.playerSearch(player);
					if (targetPlayers.isEmpty()) {
						MessageSender.send("Could not find player: "+player, sender.getName());
						return true;
					}

					ChatPlayer targetPlayer = targetPlayers.get(0);
					//Send the pm back to the sender
					
					MessageSender.sendPM(formattedString, sender.getName(), targetPlayer.name);
					if (player.equalsIgnoreCase(playerSender.name))
					{
						MessageSender.recievePM(formattedString, sender.getName(), player.toUpperCase());
					}
					return true;
				}
				//Get targetPlayer
				ArrayList<ChatPlayer> targetPlayers = PlayerManager.playerSearch(player);
				if (targetPlayers.isEmpty()) {
					MessageSender.send("Could not find player: "+player, sender.getName());
					return true;
				}
				ChatPlayer targetPlayer = targetPlayers.get(0);
				
				//Send the pm back to the sender
				MessageSender.sendPM(formattedString, sender.getName(), targetPlayer.name);
				ChatPlayer target = PlayerManager.getPlayer(targetPlayer.name);
				if(!target.modes.get("S-MUTE"))
					//Try sending the pm to the target
					MessageSender.recievePM(formattedString, sender.getName(), targetPlayer.name);
				
				//Send it to network
				Network.PmSend(PlayerManager.getPlayer(sender.getName(), MCNSAChat.shortCode), targetPlayer.name, formattedString);
			}
			else
			{
				ArrayList<ChatPlayer> targetPlayers = PlayerManager.playerSearch(player);
				if (targetPlayers.isEmpty()) {
					MessageSender.send("Could not find player: "+player, sender.getName());
					return true;
				}
				ChatPlayer targetPlayer = targetPlayers.get(0);
				
				//Send the pm back to the sender
				MessageSender.sendPM(formattedString, sender.getName(), targetPlayer.name);
				
				//Try sending the pm to the target
				MessageSender.recievePM(formattedString, sender.getName(), targetPlayer.name);
				
				//Send it to network
				Network.PmSend(PlayerManager.getPlayer(sender.getName(), MCNSAChat.shortCode), targetPlayer.name, formattedString);
			}
		}
		return true;
	}
	@Command(command = "r",
			arguments = {"Message"},
			aliases = {"reply"},
			permissions = {"msg"}, 
			description = "Reply to the last PM send or recieved",
			playerOnly = true)
	public static boolean reply(CommandSender sender, String... Message) {
		//Build the chat message
		StringBuffer messageString = new StringBuffer();
		for (String message: Message) {
			messageString.append(message+" ");
		}
		
		String formattedString = messageString.toString();
		//Get the player
		ChatPlayer playerSender = PlayerManager.getPlayer(sender.getName());
		
		if (!Permissions.useColours(playerSender.name))
			messageString = Colours.stripColor(formattedString);
		
		if (playerSender.modes.get("MUTE"))
		{
			MessageSender.send("&c You are in timeout. Please try again later", playerSender.name);
			return true;
		} else if(playerSender.modes.get("S-MUTE"))
		{
			if (playerSender.lastPm == null || playerSender.lastPm.length() < 1){
				MessageSender.send("There is no one to reply to", playerSender.name);
				return true;
			}
			if (playerSender.lastPm.equalsIgnoreCase("console")) {
				MessageSender.sendPM(formattedString, sender.getName(), playerSender.lastPm);
			}
			else
			{
				ChatPlayer targetPlayer = PlayerManager.getPlayer(playerSender.lastPm);
				
				//See if target player is online
				if (targetPlayer == null) {
					MessageSender.send("That player is offline", playerSender.name);
					return true;
				}
							
				
				//Send the pm back to the sender
				MessageSender.sendPM(formattedString, sender.getName(), targetPlayer.name);
				if (targetPlayer.name == sender.getName())
				{
					MessageSender.recievePM(formattedString, sender.getName(), targetPlayer.name);
				}
				return true;
			}
			
		}
		//check to see if the lastpm is actually filled in
		if (playerSender.lastPm == null || playerSender.lastPm.length() < 1){
			MessageSender.send("There is no one to reply to", playerSender.name);
			return true;
		}
		//sending to console support
		if (playerSender.lastPm.equalsIgnoreCase("console")) {
			//Send the pm back to the sender
			MessageSender.sendPM(formattedString, sender.getName(), playerSender.lastPm);
			
			//Try sending the pm to the target
			MessageSender.recievePM(formattedString, sender.getName(), playerSender.lastPm);
			return true;
		}
		else {
			//Get who they are replying to
			ChatPlayer targetPlayer = PlayerManager.getPlayer(playerSender.lastPm);
			
			//See if target player is online
			if (targetPlayer == null) {
				MessageSender.send("That player is offline", playerSender.name);
				return true;
			}
						
			//Send the pm back to the sender
			MessageSender.sendPM(formattedString, sender.getName(), targetPlayer.name);
			
			ChatPlayer target = PlayerManager.getPlayer(targetPlayer.name);
			if(!target.modes.get("S-MUTE"))
				//Try sending the pm to the target
				MessageSender.recievePM(formattedString, sender.getName(), targetPlayer.name);
			
			//Send it to network
			Network.PmSend(PlayerManager.getPlayer(sender.getName(), MCNSAChat.shortCode), targetPlayer.name, formattedString);
		}
		return true;
	}
	
	@Command(
			command = "me",
			aliases = {"action"},
			permissions = {"me"},
			description = "Format your message like irc /me",
			playerOnly = true
			)
	public static boolean cmdMe(CommandSender sender, String... rawMessage) {
		//Build the chat message
		ChatPlayer player = PlayerManager.getPlayer(sender.getName());
		StringBuffer messageString = new StringBuffer();
		for (String message: rawMessage) {
			messageString.append(message+" ");
		}
		String formattedString = messageString.toString();
		if (!Permissions.useColours(player.name))
			messageString = Colours.stripColor(formattedString);
		if (player.modes.get("MUTE")) { 
			MessageSender.send("&c You are in timeout. Please try again later", player.name);
			}
		else if(player.modes.get("S-MUTE")){
			MessageSender.shadowActionMessage(sender.getName(), formattedString, MCNSAChat.shortCode, PlayerManager.getPlayer(sender.getName(), MCNSAChat.shortCode).channel);
		}
		else
		{
			MessageSender.actionMessage(sender.getName(), formattedString, MCNSAChat.shortCode, PlayerManager.getPlayer(sender.getName(), MCNSAChat.shortCode).channel);
		}
		
		//Send it to relevent players
		//MessageSender.actionMessage(sender.getName(), messageString.toString(), MCNSAChat.shortCode, PlayerManager.getPlayer(sender.getName(), MCNSAChat.shortCode).channel);

		return true;
	}
	@Command(
			command= "csearch",
			description = "Find what channel a player is in"
			)
	public static boolean csearch(CommandSender sender, String player) {
		//Try and see if we can find the target player
		if (PlayerManager.playerSearch(player).size() > 0) {
			ChatPlayer target = PlayerManager.playerSearch(player).get(0);
			MessageSender.send(target.name+" is in channel: "+target.channel, sender.getName());
		}
		else {
			MessageSender.send("&cCould not find: "+player, sender.getName());
		}
		return true;
	}
	private static List<String> quickSort(List<String> toSort)
	{
		if (toSort.size() <= 1){
			return toSort;
		}
		String pivot = toSort.get(MCNSAChat.random.nextInt(toSort.size()));
		toSort.remove(pivot);
		List<String> lesser = new ArrayList<String>();
		List<String> greater = new ArrayList<String>();
		int pivotIndex = MCNSAChat.ranking.indexOf(pivot.substring(0, 2));
		if (pivotIndex == -1)
		{
			//Invalid Ranking
			ConsoleLogging.warning("Ranking not found for player: " + pivot);
			return toSort;
		}
		for (String player : toSort)
		{
			int index = MCNSAChat.ranking.indexOf(player.substring(0, 2));
			if (index == -1)
			{
				//Invalid Ranking
				ConsoleLogging.warning("Ranking not found for player: " + player);
				return toSort;
			}
			if (index <= pivotIndex)
			{
				lesser.add(player);
			}
			else
			{
				greater.add(player);
			}
		}
		List<String> result = quickSort(lesser);
		result.add(pivot);
		result.addAll(quickSort(greater));
		return result;

		//Sort by rank (&7 (default), &2, &3, &e, &6, &c, &d, &8, &b (mod))
	}
	
	@Command(
			command = "list",
			description = "Display list of online players",
			permissions = {"list"},
			aliases = {"online", "who"}
			)
	public static boolean list(CommandSender sender){
		
		List<String> formattedPlayers = new ArrayList<String>();
		//Get the players online
		for (Player player: Bukkit.getOnlinePlayers()) {
			formattedPlayers.add(Colours.PlayerPrefix(player.getName())+player.getName());
		}
		
		//Sort by rank (&7 (default), &2, &3, &e, &6, &c, &d, &8, &b (mod))
		formattedPlayers = quickSort(formattedPlayers);
		
		
		StringBuffer players = new StringBuffer();
		for (String player: formattedPlayers) {
			if (players.length() < 1)
				players.append(player);
			else
				players.append(", "+player);
		}
		//Now display
		MessageSender.send("&6Players online ("+formattedPlayers.size()+"/"+Bukkit.getMaxPlayers()+"): "+players.toString() , sender.getName());
		return true;
	}
	
	@Command(
			command = "crankreload",
			arguments = {},
			description = "Reloads players names",
			permissions = {"crankreload"}
			)
		public static boolean crankreload(CommandSender sender) {

		Permissions.perms.getGroups();
		for (ChatPlayer player : PlayerManager.players)
		{
			String playerlistName = Colours.color(Colours.PlayerPrefix(player.name)
					+ player.name);
			if (playerlistName.length() > 16)
				playerlistName = playerlistName.substring(0, 16);
			Bukkit.getPlayer(player.name).setPlayerListName(playerlistName);
		}
		return true;
	}
	
	@Command(
			command = "seen",
			arguments = {"Player name"},
			description = "Displays the last time the person logged on",
			permissions = {"seen"}
			)
		public static boolean seen(CommandSender sender, String playerName) {
			playerName = Colours.stripColor(playerName);
			if (PlayerManager.getPlayer(playerName) != null){
				String message = MCNSAChat.plugin.getConfig().getString(
				"strings.seen-online");
				message = 	message.replace("%player%", playerName);
				MessageSender.send(message, sender.getName());
			} else {
				try {
					ResultSet rs = DatabaseManager.accessQuery("SELECT lastLogin FROM chat_Players WHERE UPPER(player)=UPPER(?)", playerName);
					long last = rs.getLong(1);
					long currentTime = System.currentTimeMillis();
					long time = currentTime-last;
					String message = "&6%player% has been last seen ";
					message = 	message.replace("%player%", playerName);
					
					String seconds = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(time) % 60);
					String minutes = String.valueOf(TimeUnit.MILLISECONDS.toMinutes(time) % 60);
					String hours = String.valueOf(TimeUnit.MILLISECONDS.toHours(time) % 24);
					String days = String.valueOf(TimeUnit.MILLISECONDS.toDays(time) % 30);
					String months = String.valueOf((int)(TimeUnit.MILLISECONDS.toDays(time) / 30));
					String years = String.valueOf((int)TimeUnit.MILLISECONDS.toDays(time) / 365);

					message += (!years.equals("0") ? years+" years " : "");
					message += (!months.equals("0") ? months+" months " : "");
					message += (!days.equals("0") ? days+"d " : "");
					message += (!hours.equals("0") ? hours+"h " : "");
					message += (!minutes.equals("0") ? minutes+"m " : "");
					message += (!seconds.equals("0") ? seconds+"s " : "");
					message += "ago";
					MessageSender.send(message, sender.getName());
				} catch (DatabaseException e) {
					String message = MCNSAChat.plugin.getConfig().getString(
					"strings.seen-never");
					message = 	message.replace("%player%", playerName);
					MessageSender.send(message, sender.getName());
				} catch (SQLException e) {
					String message = MCNSAChat.plugin.getConfig().getString(
					"strings.seen-never");
					message = 	message.replace("%player%", playerName);
					MessageSender.send(message, sender.getName());
					
				}
			}
				
			return true;
		
	}
}
