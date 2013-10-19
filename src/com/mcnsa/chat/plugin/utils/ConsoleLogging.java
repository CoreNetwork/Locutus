package com.mcnsa.chat.plugin.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.mcnsa.chat.plugin.MCNSAChat;

public class ConsoleLogging {
	
	public static void info (String message) {
		Bukkit.getConsoleSender().sendMessage(Colours.processConsoleColours("&f[&aMCNSAChat&f]&f[INFO] "+message));
	}
	public static void warning (String message) {
		Bukkit.getConsoleSender().sendMessage(Colours.processConsoleColours("&f[&aMCNSAChat&f]&f[&6WARNING&f] "+message));
	}
	public static void severe (String message) {
		Bukkit.getConsoleSender().sendMessage(Colours.processConsoleColours("&f[&aMCNSAChat&f]&f[&4SEVERE&f] "+message));
	}
	public void networkLogging(String message) {
		if (MCNSAChat.plugin.getConfig().getBoolean("consoleLogServerInfo")) {
			Bukkit.getConsoleSender().sendMessage(Colours.processConsoleColours("&f[&aMCNSAChat&f]&f[NET] "+message));
		}
	}
	public static String processColours(String str) {
		str = str.replaceAll("&0", ChatColor.BLACK.toString());
		str = str.replaceAll("&1", ChatColor.DARK_BLUE.toString());
		str = str.replaceAll("&2", ChatColor.DARK_GREEN.toString());
		str = str.replaceAll("&3", ChatColor.DARK_AQUA.toString());
		str = str.replaceAll("&4", ChatColor.DARK_RED.toString());
		str = str.replaceAll("&5", ChatColor.DARK_PURPLE.toString());
		str = str.replaceAll("&6", ChatColor.GOLD.toString());
		str = str.replaceAll("&7", ChatColor.GRAY.toString());
		str = str.replaceAll("&8", ChatColor.DARK_GRAY.toString());
		str = str.replaceAll("&9", ChatColor.BLUE.toString());
		str = str.replaceAll("&a", ChatColor.GREEN.toString());
		str = str.replaceAll("&b", ChatColor.AQUA.toString());
		str = str.replaceAll("&c", ChatColor.RED.toString());
		str = str.replaceAll("&d", ChatColor.LIGHT_PURPLE.toString());
		str = str.replaceAll("&e", ChatColor.YELLOW.toString());
		str = str.replaceAll("&f", ChatColor.WHITE.toString());
		str = str.replaceAll("&k", ChatColor.MAGIC.toString());
		str = str.replaceAll("&l", ChatColor.BOLD.toString());
		str = str.replaceAll("&m", ChatColor.STRIKETHROUGH.toString());
		str = str.replaceAll("&n", ChatColor.UNDERLINE.toString());
		str = str.replaceAll("&o", ChatColor.ITALIC.toString());
		str = str.replaceAll("&r", ChatColor.RESET.toString());
		return str;
	}
	public void pm_send(String target, String rawMessage) {
		//This is for displaying the pms send from console, to the console
		Bukkit.getConsoleSender().sendMessage(Colours.processConsoleColours("&f[&aMCNSAChat&f]&f[&aPM Sent to "+target+"&f] "+rawMessage));		
	}
	public void pm_recieved(String sender, String rawMessage) {
		//This is for displaying the pms send from console, to the console
		Bukkit.getConsoleSender().sendMessage(Colours.processConsoleColours("&f[&aMCNSAChat&f]&f[&aPM recieved from "+sender+"&f] "+rawMessage));		
	}
}
