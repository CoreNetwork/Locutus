package com.mcnsa.chat.plugin.managers;

import net.milkbowl.vault.permission.Permission;

import java.util.ArrayList;

import org.bukkit.Bukkit;

import org.bukkit.plugin.RegisteredServiceProvider;

public class Permissions {
	public static String corePermission = "mcnsachat.";
	public static Permission perms;
	public Permissions(){
	}
	public static boolean setupPermissions()
	{
		RegisteredServiceProvider<Permission> rsp = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
		perms = rsp.getProvider();
		return perms != null;
	}
	public static boolean checkReadPerm(String permission, String playerName) {
		if (permission == null || permission.equals("")) {
			return true;
		}
		String checkPermission = corePermission+"read."+permission;
		if (perms.has("world", playerName, checkPermission))
			return true;
		return false;
	}
	public static boolean checkWritePerm(String permission, String playerName) {
		if (permission == null || permission.equals("")) {
			return true;
		}
		String checkPermission = corePermission+"write."+permission;
		if (perms.has("world", playerName, checkPermission))
			return true;
		return false;
	}
	public static boolean checkPermission (String permission, String playerName) {
		//MCNSAChat.console.info("Checking for permission: "+corePermission+permission + " for: " + playerName);
		if (perms.has("world", playerName, corePermission + permission)){
			return true;
		}
		return false;
	}
	public static Boolean useColours(String playerName) {
		if (perms.has("world", playerName, corePermission+"player.cancolour"))
			return true;
		
		return false;
	}
	public static Boolean forcelisten(String playerName, String channel) {
		if (perms.has("world", playerName, corePermission+"forcelisten."+channel))
			return true;
		
		return false;
	}
	public static ArrayList<String> getForceListens(String playerName) {
		ArrayList<String> permissions = new ArrayList<String>();
		//loop through the channels in the channel manager and check force listen perms
		for (int i = 0; i < ChannelManager.channels.size(); i++) {
			if (perms.has("world", playerName, corePermission+"forcelisten."+ChannelManager.channels.get(i).name))
				permissions.add(ChannelManager.channels.get(i).name);
		}
		return permissions;
	}
	
}
