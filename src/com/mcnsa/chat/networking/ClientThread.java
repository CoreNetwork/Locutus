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
			MCNSAChat.multiServer = false;
		}
		catch (IOException e) {
			//Check for connection refused
			if (e.getMessage().contains("Connection refused: connect")) {
				MCNSAChat.console.warning("Chatserver connection refused. Check chatserver address and reload");
				MCNSAChat.multiServer = false;
			}
			else if (e.getMessage().contains("Connection reset")) {
				MCNSAChat.console.info("Chatserver connection closed: Connection dropped");
			}
			else {
				//Unable to connect
				MCNSAChat.console.warning("Chatserver Connection Closed:"+e.getMessage());
			}
			MCNSAChat.network = null;
		}
		catch (ClassNotFoundException e) {
			//Missing class, Ususally means incompatable with the chatserver
			MCNSAChat.console.warning("Class not found exception. Could not find class for packet. Closing thread");
			MCNSAChat.network = null;
		}
	}
	
	public Boolean loop(ObjectInputStream in, ObjectOutputStream out) throws ClassNotFoundException, IOException {
		Object recieved = in.readObject();
		if (recieved instanceof ServerAuthedPacket) {
			MCNSAChat.console.info("Chatserver authed");
		}
		else if (recieved instanceof ServerFailAuthPacket){
			//Failed Authentication
			MCNSAChat.multiServer = false;
			MCNSAChat.console.warning("Failed Chatserver authentication. Please check password");
		}
		else if (recieved instanceof NetworkBroadcastPacket) {
			NetworkBroadcastPacket packet = (NetworkBroadcastPacket) recieved;
			MCNSAChat.console.info("NetworkBroadcast: "+packet.message);
		}
		return true;
	}
}
