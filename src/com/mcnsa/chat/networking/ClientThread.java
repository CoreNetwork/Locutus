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
			out.writeObject(packet);
			
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
				//Log in error log
				FileLog.writeError("Network: "+e.getMessage());
			}
			else if (e.getMessage().contains("Connection reset")) {
				MCNSAChat.console.info("Chatserver connection closed: Connection dropped");
				//No need to set the server to single server mode
				//Log in error log
				FileLog.writeError("Network: "+e.getMessage());
				//Reset connection
				MCNSAChat.network = null;
			}
			else {
				//Unable to connect
				MCNSAChat.console.warning("Chatserver Connection Closed:"+e.getMessage());
				//reset the connection
				MCNSAChat.network = null;
				//Log in error log
				FileLog.writeError("Network: "+e.getMessage());
			}
			
		}
		catch (ClassNotFoundException e) {
			//Missing class, Ususally means incompatable with the chatserver
			MCNSAChat.console.warning("Class not found exception. Could not find class for packet. Closing thread");
			//Set server to single server mode
			MCNSAChat.network = null;
			//Log in error log
			FileLog.writeError("Network: "+e.getMessage());
		}
	}
	
	public Boolean loop(ObjectInputStream in, ObjectOutputStream out) throws ClassNotFoundException, IOException {
		//Get the object
		Object recieved = in.readObject();
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
			if (Bukkit.getPlayer(packet.target) != null && Bukkit.getPlayer(packet.sender) == null)
				MessageSender.recievePM(packet.message, packet.sender, packet.target);
		}
		else if (recieved instanceof PlayerJoinedPacket) {
			PlayerJoinedPacket packet = (PlayerJoinedPacket) recieved;
			if (!packet.server.equals(MCNSAChat.serverName)) {
				//player joining other server
				MessageSender.joinMessage(packet.player.name, packet.server);
			}
		}
		else if (recieved instanceof PlayerQuitPacket) {
			PlayerQuitPacket packet = (PlayerQuitPacket) recieved;
			if (!packet.server.equals(MCNSAChat.serverName)) {
				//player quitting other server
				MessageSender.quitMessage(packet.player.name, packet.server);
			}
		}
		else if (recieved instanceof ChannelListPacket) {
			ChannelListPacket packet = (ChannelListPacket) recieved;
			//Log to console
			MCNSAChat.console.info("Recieved channel list from network");
			//Replace the channel list with recieved channels
			ChannelManager.channels = packet.channels;
		}
		else if (recieved instanceof ChannelUpdatePacket) {
			ChannelUpdatePacket packet = (ChannelUpdatePacket) recieved;
			ChatChannel chan = ChannelManager.getChannel(packet.channel.name);
			if (chan != null)
				ChannelManager.channels.remove(chan);
			ChannelManager.channels.add(packet.channel);
		}
		else if (recieved instanceof ChatPacket) {
			ChatPacket packet = (ChatPacket) recieved;
			if (!packet.serverCode.equals(MCNSAChat.shortCode))
				MessageSender.channelMessage(packet.channel, packet.serverCode, packet.player, packet.message);
		}
		return true;
	}
	public void write(Object packet) {
		try {
			out.writeObject(packet);
		} catch (IOException e) {
			FileLog.writeError("Error writing packet: "+packet.toString()+" : "+e.getMessage());
			//Reset the connection
			MCNSAChat.network = null;
		}
	}
}
