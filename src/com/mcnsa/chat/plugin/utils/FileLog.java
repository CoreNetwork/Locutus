package com.mcnsa.chat.plugin.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.mcnsa.chat.plugin.MCNSAChat;

public class FileLog {
	private static BufferedWriter chatLog;
	private static BufferedWriter errorLog;
	
	public FileLog() {
		try {
			chatLog = new BufferedWriter(new FileWriter(new File("plugins/MCNSAChat/chatLog.txt"), true));
			errorLog = new BufferedWriter(new FileWriter(new File("plugins/MCNSAChat/errorLog.txt"), true));
		}
		catch (Exception e) {
			MCNSAChat.console.severe("Error creating log file: "+e.getMessage());
		}
		
		//Check if the files already exist
		Boolean chatLogExists = new File("plugins/MCNSAChat/chatLog.txt").exists();
		Boolean errorLogExists = new File("plugins/MCNSAChat/errorLog.txt").exists();
		
		if (!chatLogExists) {
			try {
				chatLog.write("time,server,player,channel,message");
				chatLog.newLine();
				chatLog.flush();
			} catch (IOException e) {
				MCNSAChat.console.severe("Could not write to chatLog: "+e.getMessage());
			}
		}
		if (!errorLogExists) {
			try {
				errorLog.write("time,error");
				errorLog.newLine();
				errorLog.flush();
			} catch (IOException e) {
				MCNSAChat.console.severe("Could not write to errorLog: "+e.getMessage());
			}
		}
	}
	
	public static void writeChat(String server, String Player, String channel, String message) {
		try {
			String time = new SimpleDateFormat("yy-MM-dd HH:mm:ss").format(new Date());
			message = time + "|<"+server+channel+">("+Player+")"+message;
			chatLog.write(message);
			chatLog.newLine();
			chatLog.flush();
		} catch (IOException e) {
			MCNSAChat.console.severe("Could not write to chatLog: "+e.getMessage());
		}
	}
	public static void writeError(String message) {
		try {
			String time = new SimpleDateFormat("yy-MM-dd HH:mm:ss").format(new Date());
			message = time + "|"+message;
			errorLog.write(message);
			errorLog.newLine();
			errorLog.flush();
		} catch (IOException e) {
			MCNSAChat.console.severe("Could not write to errorLog: "+e.getMessage());
		}
	}
	public static void closeFiles() {
		try {
			chatLog.close();
			errorLog.close();
		} catch (IOException e) {
			MCNSAChat.console.severe("Could not close log files: "+e.getMessage());
		}
	}
}
