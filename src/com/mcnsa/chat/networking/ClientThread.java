package com.mcnsa.chat.networking;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import org.bukkit.Bukkit;

import com.mcnsa.chat.networking.packets.*;
import com.mcnsa.chat.plugin.MCNSAChat;
import com.mcnsa.chat.plugin.managers.ChannelManager;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.plugin.utils.FileLog;
import com.mcnsa.chat.plugin.utils.MessageSender;
import com.mcnsa.chat.type.ChatChannel;

public class ClientThread extends Thread{
	
	private String chatserver;
	private Socket socket;
	private int port;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	public ClientThread(MCNSAChat plugin) {
		this.chatserver = MCNSAChat.chatserver;
		this.port = MCNSAChat.chatserverPort;
	}

	@Override
	public void run() {
		//This is where we handle networking Such as connecting and transfering of data	
		try {
			this.socket = new Socket(this.chatserver, this.port);
			this.out = new ObjectOutputStream(this.socket.getOutputStream());
			this.in = new ObjectInputStream(this.socket.getInputStream());
			
			MCNSAChat.console.info("Connected to chatserver");
			ServerJoinedPacket packet = new ServerJoinedPacket();
			out.writeUnshared(packet);
			
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
		catch (ClassNotFoundException e) {
			//Missing class, Ususally means incompatable with the chatserver
			MCNSAChat.console.warning("Class not found exception. Could not find class for packet. Closing thread");
			//Set server to single server mode
			MCNSAChat.network = null;
			//Remove any unneeded players
			PlayerManager.removeNonServerPlayers();
			//Log in error log
			FileLog.writeError("Network: "+e.getMessage());
		}
	}
	
	public Boolean loop(ObjectInputStream in, ObjectOutputStream out) throws ClassNotFoundException, IOException {
		//Get the object
		Object recieved = in.readUnshared();
		if (recieved instanceof ServerAuthedPacket) {
			//Password was accepted by the server
			MCNSAChat.console.info("Chatserver authed");
		}
		else if (recieved instanceof ServerFailAuthPacket){
			//Failed Authentication. Set to single server mode
			MCNSAChat.multiServer = false;
			MCNSAChat.console.warning("Failed Chatserver authentication. Please check password");
			//Log in error log
			FileLog.writeError("Network: Chatserver refused authentication");
		}
		else if (recieved instanceof NetworkBroadcastPacket) {
			//Broadcast message from the chatserver
			NetworkBroadcastPacket packet = (NetworkBroadcastPacket) recieved;
			//Log to console
			MCNSAChat.console.info("NetworkBroadcast: "+packet.message);
			//Send to everyone
			
			String message = MCNSAChat.plugin.getConfig().getString("strings.networkMessage");
			message = message.replaceAll("%message%", packet.message);
			MessageSender.broadcast(message);
			
		}
		else if (recieved instanceof PmPacket) {
			PmPacket packet = (PmPacket) recieved;
			
			//Check if target is on this server
			if (Bukkit.getPlayer(packet.target) != null)
				MessageSender.recievePM(packet.message, packet.sender.name, packet.target);
		}
		else if (recieved instanceof PlayerJoinedPacket) {
			PlayerJoinedPacket packet = (PlayerJoinedPacket) recieved;
			
			//player joining other server
			MessageSender.joinMessage(packet.player, packet.server);
			//Add to playermanager
			PlayerManager.updatePlayer(packet.player);
			//Log to console
			MCNSAChat.console.networkLogging(packet.player.name+" Joined "+packet.server);

		}
		else if (recieved instanceof PlayerQuitPacket) {
			PlayerQuitPacket packet = (PlayerQuitPacket) recieved;
			//player quitting other server
			MessageSender.quitMessage(packet.player, packet.server);
			//save and remove
			packet.player.savePlayer();
			PlayerManager.players.remove(packet.player);
			MCNSAChat.console.networkLogging(packet.player.name+" Left "+packet.server);
		}
		else if (recieved instanceof ChannelListPacket) {
			ChannelListPacket packet = (ChannelListPacket) recieved;
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
		}
		else if (recieved instanceof ChannelUpdatePacket) {
			ChannelUpdatePacket packet = (ChannelUpdatePacket) recieved;
			ChatChannel chan = ChannelManager.getChannel(packet.channel.name);
			if (chan != null)
				ChannelManager.channels.remove(chan);
			ChannelManager.channels.add(packet.channel);
		}
		else if (recieved instanceof PlayerChatPacket) {
			PlayerChatPacket packet = (PlayerChatPacket) recieved;
			if (!packet.serverShortCode.equals(MCNSAChat.shortCode))
				if (packet.action.equals("CHAT"))
					MessageSender.channelMessage(packet.Channel, packet.serverShortCode, packet.player.name, packet.message);
				if (packet.action.equals("ACTION"))
					MessageSender.actionMessage(packet.player, packet.message);
		}
		else if (recieved instanceof PlayerUpdatePacket) {
			PlayerUpdatePacket packet = (PlayerUpdatePacket) recieved;
			//Update player
			PlayerManager.updatePlayer(packet.player);
			MCNSAChat.console.networkLogging(packet.player.name+" Updated from "+packet.player.server);
			MCNSAChat.console.info(packet.player.channel);
			
		}
		else if (recieved instanceof PlayerListPacket) {
			PlayerListPacket packet = (PlayerListPacket) recieved;
			//Update player
			PlayerManager.players = packet.players;
			MCNSAChat.console.networkLogging(" Updated playerlist from "+packet.server);
			
		}
		else if (recieved instanceof TimeoutPacket) {
			TimeoutPacket packet = (TimeoutPacket) recieved;
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
		recieved = null;
		return true;
	}
	public void write(Object packet) {
		try {
			out.writeUnshared(packet);
			out.reset();
		} catch (IOException e) {
			FileLog.writeError("Error writing packet: "+packet.toString()+" : "+e.getMessage());
			MCNSAChat.network = null;
			//Log in error log
			FileLog.writeError("Network: "+e.getMessage());
			//Remove other server players
			PlayerManager.removeNonServerPlayers();
		}
	}

	public void close() {
		try {
			this.socket.close();
		} catch (IOException e) {
			
		}
		
	}
}
