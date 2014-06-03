package com.mcnsa.chat.plugin.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.mcnsa.chat.plugin.MCNSAChat;

public class ConsoleLogging {
	
	public static void debug(String message) {
		Bukkit.getConsoleSender().sendMessage("["+ChatColor.GOLD+"MCNSAChat"+ChatColor.WHITE+"] " + message);
	}
	
	public static void error (String message) {
		FileLog.writeError(message);
	}
	public static void info (String message) {
		Bukkit.getConsoleSender().sendMessage(Colors.processConsoleColours("&f[&aMCNSAChat&f]&f[INFO] "+message));
	}
	public static void warning (String message) {
		Bukkit.getConsoleSender().sendMessage(Colors.processConsoleColours("&f[&aMCNSAChat&f]&f[&6WARNING&f] "+message));
	}
	public static void severe (String message) {
		Bukkit.getConsoleSender().sendMessage(Colors.processConsoleColours("&f[&aMCNSAChat&f]&f[&4SEVERE&f] "+message));
	}
	
	//TODO Networking
	public static void networkLogging(String message) {
		if (MCNSAChat.plugin.getConfig().getBoolean("consoleLogServerInfo")) {
			Bukkit.getConsoleSender().sendMessage(Colors.processConsoleColours("&f[&aMCNSAChat&f]&f[NET] "+message));
		}
	}
	
	
	public static void sendPM(String target, String rawMessage) {
		//This is for displaying the pms send from console, to the console
		Bukkit.getConsoleSender().sendMessage(Colors.processConsoleColours("&f[&aMCNSAChat&f]&f[&aPM Sent to "+target+"&f] "+rawMessage));		
	}
	
	public static void recievePM(String sender, String rawMessage) {
		//This is for displaying the pms send from console, to the console
		Bukkit.getConsoleSender().sendMessage(Colors.processConsoleColours("&f[&aMCNSAChat&f]&f[&aPM recieved from "+sender+"&f] "+rawMessage));		
	}
}
