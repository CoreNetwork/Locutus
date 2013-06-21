package com.mcnsa.chat.networking;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import com.mcnsa.chat.networking.packets.ServerJoined;
import com.mcnsa.chat.plugin.MCNSAChat;

public class ClientThread implements Runnable{
	
	private String password;
	private String chatserver;
	private Socket socket;
	private int port;
	private ObjectOutputStream out;
	private ObjectInputStream in;

	public ClientThread() {
		this.chatserver = MCNSAChat.plugin.getConfig().getString("chatServer");
		this.password = MCNSAChat.plugin.getConfig().getString("chatServerPassword");
		this.port = MCNSAChat.plugin.getConfig().getInt("chatServerPort");
	}

	@Override
	public void run() {
		//This is where we handle networking Such as connecting and transfering of data	
		try {
			this.socket = new Socket(this.chatserver, this.port);
			this.out = new ObjectOutputStream(this.socket.getOutputStream());
			this.in = new ObjectInputStream(this.socket.getInputStream());
		}
		catch (UnknownHostException e) {
			//Unable to find server
			MCNSAChat.console.warning("Unable to connect to chatserver: Unknown host");
		}
		catch (IOException e) {
			//Unable to connect
			MCNSAChat.console.warning("Unable to connect to chatserver");
		}
		
		MCNSAChat.console.info("Connected to chatserver");
		try {
			out.writeObject(new ServerJoined());
		}
		catch(IOException e) {
			
		}
	}
}
