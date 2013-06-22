package com.mcnsa.chat.networking;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import com.mcnsa.chat.networking.packets.NetworkBroadcastPacket;
import com.mcnsa.chat.networking.packets.ServerAuthedPacket;
import com.mcnsa.chat.networking.packets.ServerFailAuthPacket;
import com.mcnsa.chat.networking.packets.ServerJoinedPacket;
import com.mcnsa.chat.plugin.MCNSAChat;
import com.mcnsa.chat.plugin.utils.FileLog;
import com.mcnsa.chat.plugin.utils.MessageSender;

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
			MessageSender.broadcast("&cMCNSA NetMessage: "+message);
			
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
