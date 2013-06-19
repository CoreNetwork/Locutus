package com.mcnsa.chat.plugin.components;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;

import com.mcnsa.chat.plugin.MCNSAChat;
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
		ChatPlayer player = PlayerManager.getPlayer(sender.getName(), MCNSAChat.plugin.shortCode);
		
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
		//TODO: Networking: UPDATE player
		return true;
		
	}
	
	@Command(command = "cmute",
			aliases = {"ignore", "mute"},
			arguments = {"Player"},
			description = "Mute a player",
			permissions = {})
	public static boolean mutePlayer(CommandSender sender, String mutedPlayer) {
		//Get the player sending command
		ChatPlayer playerSending = PlayerManager.getPlayer(sender.getName(), MCNSAChat.plugin.shortCode);
		
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
		//TODO: Networking: UPDATE Player
		return true;
	}
	
	@Command(command = "clist",
			aliases = {"Channels", "c"},
			description = "Get a list of channels",
			permissions = {})
	public static boolean channelList(CommandSender sender) {
		//Get the player
		ChatPlayer player = PlayerManager.getPlayer(sender.getName(), MCNSAChat.plugin.shortCode);
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
}
