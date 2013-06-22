package com.mcnsa.chat.plugin.components;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;

import com.mcnsa.chat.networking.Network;
import com.mcnsa.chat.plugin.annotations.Command;
import com.mcnsa.chat.plugin.annotations.ComponentInfo;
import com.mcnsa.chat.plugin.exceptions.ChatCommandException;
import com.mcnsa.chat.plugin.managers.ChannelManager;
import com.mcnsa.chat.plugin.managers.Permissions;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.plugin.utils.Colours;
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
			permissions = {"move"})
	public static boolean channelChange(CommandSender sender, String channel) throws ChatCommandException{
		//Get the player
		ChatPlayer player = PlayerManager.getPlayer(sender.getName());
		
		//see if its a persist channel
		if (ChannelManager.getChannel(channel) != null) {
			ChatChannel chan = ChannelManager.getChannel(channel);
			
			if (!Permissions.checkReadPerm(chan.read_permission, player.name)) {
					MessageSender.send("&4You do not have the required permissions to enter this channel", player.name);
					return true;
				}
			channel = chan.name;
		}
		
		//Get players in channel
		String playersInChannel = ChannelManager.playersInChannel(channel);
		//We can say this player has the permissions. Lets welcome them
		player.changeChannel(channel);
		MessageSender.send(Colours.color("&6Welcome to the "+channel+" channel. Players here: " + playersInChannel), player.name);

		//Update player on other servers
		Network.updatePlayer(player);
		
		return true;
		
	}
	
	@Command(command = "cmute",
			aliases = {"ignore", "mute"},
			arguments = {"Player"},
			description = "Mute a player",
			permissions = {})
	public static boolean mutePlayer(CommandSender sender, String mutedPlayer) {
		//Get the player sending command
		ChatPlayer playerSending = PlayerManager.getPlayer(sender.getName());
		
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
			aliases = {"Channels", "c"},
			description = "Get a list of channels",
			permissions = {})
	public static boolean channelList(CommandSender sender) {
		//Get the player
		ChatPlayer player = PlayerManager.getPlayer(sender.getName());
		//Get the channel list
		ArrayList<String> channels = ChannelManager.getChannelList(player);
		
		StringBuffer message = new StringBuffer();
		for (String channel: channels) {
			if (message.length() < 1)
				message.append(channel);
			else
				message.append(", "+channel);
		}
		MessageSender.send("Channels: "+message.toString(), player.name);
		return true;
	}
	
	@Command(command = "listen",
			description = "Listen to a channel",
			permissions = {})
	public static boolean channelListen(CommandSender sender, String Channel) {
		Channel = Channel.substring(0, 1).toUpperCase() + Channel.substring(1);
		
		//Get the player
		ChatPlayer player = PlayerManager.getPlayer(sender.getName());
		
		//Try and get the channel
		ChatChannel targetChannel = ChannelManager.getChannel(Channel);
		if (targetChannel != null) {
			//Channel is persistent. Check perms
			if (!Permissions.checkReadPerm(targetChannel.read_permission, player.name)) {
				//Player does not have read permission
				MessageSender.send("&4You do not have permission to listen to: "+targetChannel.color+targetChannel.name, player.name);
				return true;
			}
			Channel = targetChannel.color+targetChannel.name;
		}
		//Change the channel
		if (player.channelListen(Channel)) {
			MessageSender.send("&6You are now listening to "+Channel, player.name);
		}
		else {
			MessageSender.send("&6You have stopped listening to "+Channel, player.name);
		}
		
		//Update player on other servers
		Network.updatePlayer(player);
		return true;
	}
	@Command(command = "message",
			aliases= {"msg", "whisper", "tell", "w"},
			description = "Message a player",
			permissions = {"msg"})
	public static boolean message(CommandSender sender, String player, String... Message) {
		//Get targetPlayer
		ArrayList<ChatPlayer> targetPlayers = PlayerManager.playerSearch(player);
		if (targetPlayers.isEmpty()) {
			MessageSender.send("Could not find player: "+player, sender.getName());
			return true;
		}
		ChatPlayer targetPlayer = targetPlayers.get(0);
		
		StringBuffer messageString = new StringBuffer();
		for (String message: Message) {
			messageString.append(message+" ");
		}
		
		//Send the pm back to the sender
		MessageSender.sendPM(messageString.toString(), sender.getName(), targetPlayer.name);
		
		//Try sending the pm to the target
		MessageSender.recievePM(messageString.toString(), sender.getName(), targetPlayer.name);
		
		//Send it to network
		Network.PmSend(sender.getName(), targetPlayer.name, messageString.toString());
		
		return true;
	}
	
}
