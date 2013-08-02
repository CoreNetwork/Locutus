package com.mcnsa.chat.networking.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PmPacket implements BasePacket {
	public static int id = 10;
	public String sender;
	public String reciever;
	public String message;
	
	public PmPacket(){
		
	}
	
	public PmPacket(String sender, String reciever, String message) {
		this.sender = sender;
		this.reciever = reciever;
		this.message = message;
	}
	
	public void write(DataOutputStream out) throws IOException {
		out.writeInt(id);
		out.writeUTF(this.sender);
		out.writeUTF(this.reciever);
		out.writeUTF(this.message);
		out.flush();
	}
	
	public void read(DataInputStream in) throws IOException {
		this.sender = in.readUTF();
		this.reciever = in.readUTF();
		this.message = in.readUTF();
	}
}
