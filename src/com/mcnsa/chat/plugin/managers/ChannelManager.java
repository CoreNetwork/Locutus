package com.mcnsa.chat.plugin.managers;

import java.util.ArrayList;
import java.util.HashMap;

import com.mcnsa.chat.plugin.utils.Colors;
import com.mcnsa.chat.type.ChatChannel;
import com.mcnsa.chat.type.ChatPlayer;

public class ChannelManager {
    private static ArrayList<ChatChannel> channels = new ArrayList<ChatChannel>();
    private static ArrayList<String> tempChannels = new ArrayList<String>();
    public static HashMap<String, String> channelAlias = new HashMap<String, String>();

    public ChannelManager() {
	channels = new ArrayList<ChatChannel>();
	tempChannels = new ArrayList<String>();
	channelAlias = new HashMap<String, String>();
    }

    public static void removeChannel(String chan) {
	channels.remove(getChannel(chan));
    }

    public static void removeTempChannel(String chan) {
	tempChannels.remove(chan);
    }

    public static void addTempChannel(String chan) {
	tempChannels.add(chan);
    }

    public static ChatChannel getChannel(String chan) {
	for (int i = 0; i < channels.size(); i++) {
	    ChatChannel channel = channels.get(i);
	    if (channel.name.equalsIgnoreCase(chan)) {
		return channel;
	    }
	}
	return null;
    }

    public static String getTempChannel(String chan) {
	for (int i = 0; i < tempChannels.size(); i++) {
	    String channel = tempChannels.get(i);
	    if (channel.equalsIgnoreCase(chan)) {
		return channel;
	    }
	}
	return null;
    }

    public static ArrayList<ChatPlayer> getPlayersListening(String channel) {
	ArrayList<ChatPlayer> players = new ArrayList<ChatPlayer>();
	for (ChatPlayer player : PlayerManager.players) {
	    if (player.channel.equalsIgnoreCase(channel) || player.isListening(channel.toLowerCase()) || player.modes.get("SEEALL")) {
		players.add(player);
	    }
	}

	if (players.isEmpty())
	    return null;

	return players;

    }

    public static ArrayList<ChatPlayer> getPlayersInChannel(String channel) {
	ArrayList<ChatPlayer> players = new ArrayList<ChatPlayer>();
	for (int i = 0; i < players.size(); i++) {
	    ChatPlayer player = PlayerManager.players.get(i);
	    if (player.channel.equalsIgnoreCase(channel) || player.isListening(channel.toLowerCase())) {
		players.add(player);
	    }
	}

	return players;
    }

    public static ArrayList<String> getChannelList() {
	ArrayList<String> persistChannelList = getPersistChannelList();
	ArrayList<String> playerChannelList = getPlayerChannelList();
	ArrayList<String> channelList = new ArrayList<String>();
	// Get the channels in the channel list
	for (int i = 0; i < persistChannelList.size(); i++) {
	    String chan = persistChannelList.get(i);
	    if (!channelList.contains(chan))
		channelList.add(chan);
	}

	// Now get the channels the players are in
	for (int i = 0; i < playerChannelList.size(); i++) {
	    String playerChan = playerChannelList.get(i);
	    if (!channelList.contains(playerChan))
		channelList.add(playerChan);
	}
	return channelList;
    }

    public static ArrayList<String> getChannelList(ChatPlayer player) {
	ArrayList<String> playerChannelList = getPlayerChannelList();
	ArrayList<String> channelList = new ArrayList<String>();
	// Get the channels in the channel list
	for (int i = 0; i < channels.size(); i++) {
	    ChatChannel chan = channels.get(i);
	    if (PermissionManager.checkPermission(chan.readPermission, player) && PermissionManager.checkPermission(chan.writePermission, player)) {
		if (!channelList.contains(chan.name))
		    channelList.add(chan.name);
	    }
	}
	// Now get the channels the players are in
	for (int i = 0; i < playerChannelList.size(); i++) {
	    String playerChan = playerChannelList.get(i);
	    if (!channelList.contains(playerChan))
		channelList.add(playerChan);
	}
	return channelList;
    }

    public static ArrayList<String> getPersistChannelList() {
	ArrayList<String> channelList = new ArrayList<String>();
	// Get the channels in the channel list
	for (int i = 0; i < channels.size(); i++) {
	    ChatChannel chan = channels.get(i);
	    if (!channelList.contains(chan.name))
		channelList.add(chan.name);
	}
	return channelList;
    }

    public static ArrayList<String> getPlayerChannelList() {
	ArrayList<String> channelList = new ArrayList<String>();

	for (int i = 0; i < PlayerManager.players.size(); i++) {
	    ChatPlayer player = PlayerManager.players.get(i);
	    if (!channelList.contains(player.channel))
		channelList.add(player.channel);
	}
	return channelList;
    }

    public static String playersInChannel(String channel) {
	StringBuffer playerList = new StringBuffer();
	if (getPlayersListening(channel) != null) {
	    for (ChatPlayer player : getPlayersListening(channel)) {
		if (!playerList.toString().contains(player.getName()) || !player.modes.get("SEEALL") && player.channel.equalsIgnoreCase(channel)) {
		    if (playerList.length() < 1)
			playerList.append(Colors.PlayerPrefix(player) + player.getName());
		    else
			playerList.append(", " + Colors.PlayerPrefix(player) + player.getName());
		}
	    }
	}
	if (playerList.length() == 0)
	    return "No-one";
	return playerList.toString();
    }

    public static ArrayList<ChatChannel> getChatChannelList() {
	ArrayList<ChatChannel> chanClone = new ArrayList<ChatChannel>();
	for (ChatChannel chan : channels)
	    chanClone.add(chan);
	return chanClone;
    }

    public static int getChannelListSize() {
	return channels.size();
    }

    public static void addChannel(ChatChannel chatChannel) {
	channels.add(chatChannel);

    }

    public static void setChannels(ArrayList<ChatChannel> channels2) {
	channels = channels2;

    }

    public static void removeChannel(ChatChannel chan) {
	removeChannel(chan.name);

    }
}
