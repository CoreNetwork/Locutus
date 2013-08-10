package com.mcnsa.chat.networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import org.bukkit.Bukkit;

import com.mcnsa.chat.networking.packets.BasePacket;
import com.mcnsa.chat.networking.packets.ChannelListingPacket;
import com.mcnsa.chat.networking.packets.ChannelUpdatePacket;
import com.mcnsa.chat.networking.packets.NetworkBroadcastPacket;
import com.mcnsa.chat.networking.packets.PlayerChatPacket;
import com.mcnsa.chat.networking.packets.PlayerJoinedPacket;
import com.mcnsa.chat.networking.packets.PlayerListPacket;
import com.mcnsa.chat.networking.packets.PlayerQuitPacket;
import com.mcnsa.chat.networking.packets.PlayerTimeoutPacket;
import com.mcnsa.chat.networking.packets.PlayerUpdatePacket;
import com.mcnsa.chat.networking.packets.PmPacket;
import com.mcnsa.chat.networking.packets.ServerAuthPacket;
import com.mcnsa.chat.networking.packets.ServerJoinedPacket;
import com.mcnsa.chat.plugin.MCNSAChat;
import com.mcnsa.chat.plugin.managers.ChannelManager;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.plugin.utils.FileLog;
import com.mcnsa.chat.plugin.utils.MessageSender;
import com.mcnsa.chat.type.ChatChannel;

public class ClientThread extends Thread{
	
	private Socket socket;
	private DataOutputStream out;
	private DataInputStream in;
	public ClientThread() {

	}

	@Override
	public void run() {
		//This is where we handle networking Such as connecting and transfering of data	
		try {
			this.socket = new Socket(MCNSAChat.plugin.getConfig().getString("chatServer"), MCNSAChat.plugin.getConfig().getInt("chatServerPort"));
			this.out = new DataOutputStream(this.socket.getOutputStream());
			this.in = new DataInputStream(this.socket.getInputStream());
			
			MCNSAChat.console.info("Connected to chatserver");
			ServerJoinedPacket packet = new ServerJoinedPacket(MCNSAChat.serverName, MCNSAChat.shortCode, MCNSAChat.plugin.getConfig().getString("chatServerPassword"), PlayerManager.players);
			packet.write(out);
			
			while (loop(in, out))
				;
		
		}
		catch (UnknownHostException e) {
			//Unable to find server
			MCNSAChat.console.warning("Unable to connect to chatserver: Unknown host");
			//Set server to single server mode
			MCNSAChat.multiServer = false;
		}
		catch (IOException e) {
			//Check for connection refused
			if (e.getMessage() == null) {
				MCNSAChat.network = null;
			}
			else if (e.getMessage().contains("Connection refused: connect")) {
				MCNSAChat.console.warning("Chatserver connection refused. Check chatserver address and reload");
				//Set server to single server mode
				MCNSAChat.multiServer = false;
				MCNSAChat.network = null;
				//Log in error log
				FileLog.writeError("Network: "+e.getMessage());
			}
			else if (e.getMessage().contains("Connection reset")) {
				MCNSAChat.console.info("Chatserver connection closed: Connection dropped");
				//No need to set the server to single server mode
				//Log in error log
				FileLog.writeError("Network: "+e.getMessage());
				//Remove other server players
				PlayerManager.removeNonServerPlayers();
				//Reset connection
				MCNSAChat.network = null;
			}
			else {
				//Unable to connect
				MCNSAChat.console.warning("Chatserver Connection Closed:"+e.getMessage());
				//reset the connection
				MCNSAChat.network = null;
				//Remove any unneeded players
				PlayerManager.removeNonServerPlayers();
				//Log in error log
				FileLog.writeError("Network: "+e.getMessage());
			}
			
		}
	}
	
	public Boolean loop(DataInputStream in, DataOutputStream out) throws IOException {
		//Get the packet id
		short id = in.readShort();

		if (id == ServerAuthPacket.id) {
			//Get packet
			ServerAuthPacket packet = new ServerAuthPacket();
			packet.read(in);
			
			if (packet.status.equalsIgnoreCase("pass")) {
				MCNSAChat.console.info("Chatserver authed");
				return true;
			}
			else
				MCNSAChat.console.warning("ChatServer authentication Failed. Please check passcode");
				MCNSAChat.multiServer = false;
		}
		else if (id == NetworkBroadcastPacket.id) {
			//Broadcast message from the chatserver
			NetworkBroadcastPacket packet = new NetworkBroadcastPacket();
			packet.read(in);
			//Log to console
			MCNSAChat.console.info("NetworkBroadcast: "+packet.message);
			//Send to everyone
			
			String message = MCNSAChat.plugin.getConfig().getString("strings.networkMessage");
			message = message.replaceAll("%message%", packet.message);
			MessageSender.broadcast(message);
			return true;
		}
		else if (id == PmPacket.id) {
			PmPacket packet = new PmPacket();
			packet.read(in);
			
			//Check if target is on this server
			if (Bukkit.getPlayer(packet.reciever) != null)
				MessageSender.recievePM(packet.message, packet.sender, packet.reciever);
			
			return true;
		}
		else if (id == PlayerJoinedPacket.id) {
			PlayerJoinedPacket packet = new PlayerJoinedPacket();
			packet.read(in);
			
			//player joining other server
			MessageSender.joinMessage(packet.player, packet.server);
			//Add to playermanager
			PlayerManager.updatePlayer(packet.player);
			//Log to console
			MCNSAChat.console.networkLogging(packet.player.name+" Joined "+packet.server);
			
			return true;
		}
		else if (id == PlayerQuitPacket.id) {
			PlayerQuitPacket packet = new PlayerQuitPacket();
			packet.read(in);
			//player quitting other server
			MessageSender.quitMessage(packet.player, packet.server);
			//save and remove
			packet.player.savePlayer();
			PlayerManager.players.remove(packet.player);
			MCNSAChat.console.networkLogging(packet.player.name+" Left "+packet.server);
			
			return true;
		}
		else if (id == ChannelListingPacket.id) {
			ChannelListingPacket packet = new ChannelListingPacket();
			packet.read(in);
			//Log to console
			MCNSAChat.console.info("Recieved channel list from network");
			//Replace the channel list with recieved channels
			ChannelManager.channels = packet.channels;
			
			//Check for aliases
			for (int i = 0; i < ChannelManager.channels.size(); i++) {
				ChatChannel chan = ChannelManager.channels.get(i);
				//Check to see if the channel has an alias set
				if (chan.alias != null & !ChannelManager.channelAlias.containsKey(chan.alias)) {
					ChannelManager.channelAlias.put(chan.alias, chan.name);
				}
			}
			
			return true;
		}
		else if (id == ChannelUpdatePacket.id) {
			ChannelUpdatePacket packet = new ChannelUpdatePacket();
			packet.read(in);
			
			ChatChannel chan = ChannelManager.getChannel(packet.channel.name);
			if (chan != null)
				ChannelManager.channels.remove(chan);
			ChannelManager.channels.add(packet.channel);
			
			return true;
		}
		else if (id == PlayerChatPacket.id) {
			PlayerChatPacket packet = new PlayerChatPacket();
			packet.read(in);
			if (!packet.server.equals(MCNSAChat.shortCode)) {
				if (packet.type.equals("CHAT"))
					MessageSender.channelMessage(packet.channel, packet.server, packet.player, packet.message);
				if (packet.type.equals("ACTION"))
					MessageSender.actionMessage(packet.player, packet.message, packet.channel, packet.server);
			}
				
			return true;
		}
		else if (id == PlayerUpdatePacket.id) {
			PlayerUpdatePacket packet = new PlayerUpdatePacket();
			packet.read(in);
			//Update player
			PlayerManager.updatePlayer(packet.player);
			MCNSAChat.console.networkLogging(packet.player.name+" Updated from "+packet.player.server);
			
			return true;
		}
		else if (id == PlayerListPacket.id) {
			PlayerListPacket packet = new PlayerListPacket();
			packet.read(in);
			
			//Update player
			PlayerManager.players = packet.players;
			MCNSAChat.console.networkLogging(" Updated playerlist from network");
			
			return true;
		}
		else if (id == PlayerTimeoutPacket.id) {
			PlayerTimeoutPacket packet = new PlayerTimeoutPacket();
			packet.read(in);
			
			if (packet.time == 0 && packet.reason == null) {
				MessageSender.send("&6You have been removed from Timeout", packet.player.name);
			}
			else {
				//inform players
				MessageSender.timeoutPlayer(packet.player.name, String.valueOf(packet.time), packet.reason);
	
				//Set timer just incase other servers go down
				final String finalPlayerName = packet.player.name;
				long timeleft = packet.time * 1210;
				Bukkit.getScheduler().scheduleSyncDelayedTask(MCNSAChat.plugin, new Runnable() {
					public void run() {
							if (PlayerManager.getPlayer(finalPlayerName, MCNSAChat.shortCode) != null && PlayerManager.getPlayer(finalPlayerName).modes.get("MUTE")){
								PlayerManager.unmutePlayer(finalPlayerName);
							}
						}
				}, timeleft);
			}
			return true;
		}
		return false;
	}
	public void write(BasePacket packet) {
		try {
			MCNSAChat.console.info("Writing packet");
			packet.write(out);
		} catch (IOException e) {
			FileLog.writeError("Error writing packet: "+e.getMessage());
			MCNSAChat.network = null;
			//Log in error log
			FileLog.writeError("Network: "+e.getMessage());
			//Remove other server players
			PlayerManager.removeNonServerPlayers();
			//Close socket
			close();
		}
	}

	public void close() {
		try {
			this.socket.close();
		} catch (IOException e) {
			
		}
		
	}
}
