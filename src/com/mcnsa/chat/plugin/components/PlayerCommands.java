package com.mcnsa.chat.plugin.components;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.chat.plugin.MCNSAChat;
import com.mcnsa.chat.plugin.annotations.Command;
import com.mcnsa.chat.plugin.annotations.ComponentInfo;
import com.mcnsa.chat.plugin.exceptions.ChatCommandException;
import com.mcnsa.chat.plugin.exceptions.DatabaseException;
import com.mcnsa.chat.plugin.managers.ChannelManager;
import com.mcnsa.chat.plugin.managers.DatabaseManager;
import com.mcnsa.chat.plugin.managers.PermissionManager;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.plugin.utils.Colors;
import com.mcnsa.chat.plugin.utils.ConsoleLogging;
import com.mcnsa.chat.plugin.utils.MessageSender;
import com.mcnsa.chat.type.ChatChannel;
import com.mcnsa.chat.type.ChatPlayer;
//TODO Change command structure including names
//TODO Change func names to fit standard
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
		Player p = (Player) sender;
		//Get the player
		ChatPlayer player = PlayerManager.getPlayer(p.getUniqueId(), MCNSAChat.shortCode);
		//Check to see if the Player is locked in channel
		if (player.modes.get("LOCKED")) {
			MessageSender.send("&cYou are locked in this channel", player);
			return true;
		}
		
		//see if its a persist channel
		if (ChannelManager.getChannel(channel) != null) {
			ChatChannel chan = ChannelManager.getChannel(channel);
			
			if (!PermissionManager.checkPermission(chan.readPermission, player.name)) {
					MessageSender.send("&4You do not have the required permissions to enter this channel", player);
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
		MessageSender.send(Colors.color("&6Welcome to the "+channel+" channel. Players here: " + playersInChannel), player);
	
		return true;
		
	}
	
	@Command(command = "mute",
			aliases = {"ignore", "mute", "cmute"},
			arguments = {"Player"},
			description = "Mute a player",
			permissions = {"mute"},
			playerOnly = true)
	public static boolean mutePlayer(CommandSender sender, String mutedPlayerName) {
		Player p = (Player) sender;
		//Get the player sending command
		ChatPlayer playerSending = PlayerManager.getPlayer(p.getUniqueId(), MCNSAChat.shortCode);
		
		//Try and find the player they are trying to mute
		ChatPlayer mutedPlayer = PlayerManager.searchPlayer(mutedPlayerName);
		//See if the player they are trying to mute is already muted
		if (playerSending.hasMuted(mutedPlayer)) {
			//Let the player know
			MessageSender.send("&6"+mutedPlayer+" is already muted", playerSending);
		}
		else {
			playerSending.mutePlayer(mutedPlayer);
			//Let the player know
			MessageSender.send("&6"+mutedPlayer+" has been muted", playerSending);
		}

		
		return true;
	}
	
	@Command(command = "unmute",
			arguments = {"Player"},
			description = "Unmute a player",
			permissions = {"mute"},
			playerOnly = true)
	public static boolean unmutePlayer(CommandSender sender, String mutedPlayerName) {
		Player p = (Player) sender;
		//Get the player sending command
		ChatPlayer playerSending = PlayerManager.getPlayer(p.getUniqueId(), MCNSAChat.shortCode);
		
		//Try and find the player they are trying to mute
		ChatPlayer mutedPlayer = PlayerManager.searchPlayer(mutedPlayerName);
		//See if the player they are trying to mute is already muted
		if (playerSending.hasMuted(mutedPlayer)) {
			//Player is already muted, Un mute them
			playerSending.hasMuted(mutedPlayer);
			
			//Let the player know
			MessageSender.send("&6"+mutedPlayer+" has been unmuted", playerSending);
		}
		else {
			//Let the player know
			MessageSender.send("&6"+mutedPlayer+" is not muted", playerSending);
		}

		
		return true;
	}
	
	@Command(command = "channels",
			aliases = {"Channels"},
			description = "Get a list of channels",
			permissions = {"list"},
			playerOnly = false)
	public static boolean channelList(CommandSender sender) {
		//Get the player
		ChatPlayer player = PlayerManager.getPlayer(sender, MCNSAChat.shortCode);
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
			MessageSender.send("Channels: "+message.toString(), player);
		}
		else
		{
			MessageSender.sendToConsole("Channels: "+message.toString());
		}
		return true;
	}
	
	@Command(command = "listen",
			description = "Listen to a channel",
			arguments = {"channel"},
			permissions = {},
			playerOnly = true)
	public static boolean channelListen(CommandSender sender, String Channel) {
		Player p = (Player) sender;
		Channel = Channel.substring(0, 1).toUpperCase() + Channel.substring(1);
		String channel = Channel;
		//Get the player
		ChatPlayer player = PlayerManager.getPlayer(p.getUniqueId(), MCNSAChat.shortCode);
		
		//Try and get the channel
		ChatChannel targetChannel = ChannelManager.getChannel(Channel);
		if (targetChannel != null) {
			//Channel is persistent. Check perms
			if (!PermissionManager.checkPermission(targetChannel.readPermission, player.name)) {
				//Player does not have read permission
				MessageSender.send("&4You do not have permission to listen to: "+targetChannel.color+targetChannel.name, player);
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
			MessageSender.send("&6You are now listening to "+channel, player);
		}
		else if (result == 1) {
			MessageSender.send("&6You have stopped listening to "+channel, player);
		}
		else  if (result == 2){
			MessageSender.send("&cYou cannot listen to this channel "+channel, player);
		}
		
		
		return true;
	}
	@Command(command = "message",
			aliases= {"msg", "whisper", "tell", "w"},
			arguments = {"Player", "Message"},
			description = "Message a player",
			permissions = {"msg"})
	public static boolean message(CommandSender sender, String player, String... messageArray) {
		
		StringBuffer messageString = new StringBuffer();
		for (String message: messageArray) {
			messageString.append(message+" ");
		}
		String message = messageString.toString();
		if (sender instanceof Player){
			ChatPlayer playerSender = PlayerManager.getPlayer(sender);
			if (playerSender.modes.get("MUTE"))
			{
				MessageSender.send("&c You are in timeout. Please try again later", playerSender);
				return true;
			}
			if (player.equalsIgnoreCase("console")){
				if(!playerSender.modes.get("S-MUTE"))
					MessageSender.sendToConsole(message);
				MessageSender.sendPMConsole(messageString.toString(), sender);
				return true;
			}
			ChatPlayer target = PlayerManager.searchPlayer(player);
			if (target == null){
				MessageSender.send("Could not find player: "+player, sender);
				return true;
			}
			if (target.equals(playerSender)){
				MessageSender.recievePM(messageString.toString(), sender, target);
				MessageSender.sendPM(messageString.toString(), sender, target);
				return true;
			}

			MessageSender.sendPM(messageString.toString(), sender, target);
			if (!(playerSender.modes.get("S-MUTE") || target.modes.get("S-MUTE"))){
				MessageSender.recievePM(messageString.toString(), sender, target);
			}
			return true;
			
		} else {
			if (player.equalsIgnoreCase("console")){
				MessageSender.sendPMConsoleConsole(message);
				MessageSender.receivePMConsoleConsole(message);
				return true;
			}
			ChatPlayer target = PlayerManager.searchPlayer(player);
			if (target == null){
				MessageSender.sendToConsole("Could not find player: "+player);
				return true;
			}
			MessageSender.sendPMConsole(messageString.toString(), target);
			if (!target.modes.get("S-MUTE")){
				MessageSender.receivePMConsole(messageString.toString(), target);
			}
			return true;
			
		}
		
	}
	@Command(command = "r",
			arguments = {"Message"},
			aliases = {"reply"},
			permissions = {"msg"}, 
			description = "Reply to the last PM send or recieved",
			playerOnly = true)
	public static boolean reply(CommandSender sender, String... message) {
		Player p = (Player) sender;
		//Get the player
		ChatPlayer playerSender = PlayerManager.getPlayer(p);
		ChatPlayer target = PlayerManager.getPlayer(playerSender.lastPm);
		if (target == null){
			MessageSender.send("There is no one to reply to", playerSender);
			return true;
		}
		return message(sender, target.name, message);
	}
	
	@Command(
			command = "me",
			aliases = {"action"},
			permissions = {"me"},
			description = "Format your message like irc /me",
			playerOnly = true
			)
	public static boolean cmdMe(CommandSender sender, String... rawMessage) {
		Player p = (Player) sender;
		//Build the chat message
		ChatPlayer player = PlayerManager.getPlayer(p);
		StringBuffer messageString = new StringBuffer();
		for (String message: rawMessage) {
			messageString.append(message+" ");
		}
		if (player.modes.get("MUTE")) { 
			MessageSender.send("&c You are in timeout. Please try again later", player);
			}
		else
		{
			MessageSender.actionMessage(player, messageString.toString(), MCNSAChat.shortCode, PlayerManager.getPlayer(p, MCNSAChat.shortCode).channel);
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
		ChatPlayer target;
		//Try and see if we can find the target player
		if ((target = PlayerManager.searchPlayer(player)) != null) {
			MessageSender.send(target.name+" is in channel: "+target.channel, sender);
		}
		else {
			MessageSender.send("&cCould not find: "+player, sender);
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
			formattedPlayers.add(Colors.PlayerPrefix(player)+player.getName());
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
		MessageSender.send("&6Players online ("+formattedPlayers.size()+"/"+Bukkit.getMaxPlayers()+"): "+players.toString() , sender);
		return true;
	}
	
	@Command(
			command = "crankreload",
			arguments = {},
			description = "Reloads players names",
			permissions = {"crankreload"}
			)
		public static boolean crankreload(CommandSender sender) {
		//TODO
		//Needed? has changed
		PermissionManager.perms.getGroups();
		for (ChatPlayer player : PlayerManager.players)
		{
			String playerlistName = Colors.color(Colors.PlayerPrefix(player)
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
		
			if (PlayerManager.searchPlayer(playerName) != null){
				String message = MCNSAChat.plugin.getConfig().getString(
				"strings.seen-online");
				message = 	message.replace("%player%", playerName);
				MessageSender.send(message, sender);
			} else {
				try {
					ResultSet rs = DatabaseManager.accessQuery("SELECT lastLogin FROM chat_Players WHERE UPPER(player)=UPPER(?)", playerName);
					long last = rs.getLong(1);
					long currentTime = System.currentTimeMillis();
					long time = currentTime-last;
					String message = "&%player% has been last seen ";
					message = 	message.replace("%player%", playerName);
					
					String seconds = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(time) % 60);
					String minutes = String.valueOf(TimeUnit.MILLISECONDS.toMinutes(time) % 60);
					String hours = String.valueOf(TimeUnit.MILLISECONDS.toHours(time) % 24);
					String days = String.valueOf(TimeUnit.MILLISECONDS.toDays(time) % 30);
					String months = String.valueOf((int)(TimeUnit.MILLISECONDS.toDays(time) / 30));
					String years = String.valueOf((int)TimeUnit.MILLISECONDS.toDays(time) / 365);

					ConsoleLogging.info(seconds);
					ConsoleLogging.info(minutes);
					ConsoleLogging.info(hours);
					ConsoleLogging.info(days);
					ConsoleLogging.info(months);
					ConsoleLogging.info(years);
					ConsoleLogging.info(String.valueOf(seconds.isEmpty()));
					message += (!years.equals("0") ? years+" years " : "");
					message += (!months.equals("0") ? months+" months " : "");
					message += (!days.equals("0") ? days+"d " : "");
					message += (!hours.equals("0") ? hours+"h " : "");
					message += (!minutes.equals("0") ? minutes+"m " : "");
					message += (!seconds.equals("0") ? seconds+"s " : "");
					message += "ago";
					MessageSender.send(message, sender);
				} catch (DatabaseException e) {
					String message = MCNSAChat.plugin.getConfig().getString(
					"strings.seen-never");
					message = 	message.replace("%player%", playerName);
					MessageSender.send(message, sender);
				} catch (SQLException e) {
					String message = MCNSAChat.plugin.getConfig().getString(
					"strings.seen-never");
					message = 	message.replace("%player%", playerName);
					MessageSender.send(message, sender);
					
				}
			}
				
			return true;
		
	}
}
