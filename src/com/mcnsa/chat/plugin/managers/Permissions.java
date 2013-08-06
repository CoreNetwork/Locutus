package com.mcnsa.chat.plugin.managers;

import java.util.ArrayList;
import com.mcnsa.chat.plugin.MCNSAChat;

public class Permissions {
	public static String corePermission = "mcnsachat.";
	public Permissions(){
	}
	public static boolean checkReadPerm(String permission, String playerName) {
		if (permission == null || permission.equals("")) {
			return true;
		}
		String checkPermission = corePermission+"read."+permission;
		//MCNSAChat.console.info("Checking for permission: "+checkPermission);
		if (MCNSAChat.groupManager.getWorldsHolder().getWorldPermissions("world").permission(playerName, checkPermission)) 
			return true;
		return false;
	}
	public static boolean checkWritePerm(String permission, String playerName) {
		if (permission == null || permission.equals("")) {
			return true;
		}
		String checkPermission = corePermission+"write."+permission;
		//MCNSAChat.console.info("Checking for permission: "+checkPermission);
		if (MCNSAChat.groupManager.getWorldsHolder().getWorldPermissions("world").permission(playerName, checkPermission)) 
			return true;
		return false;
	}
	public static boolean checkPermission (String permission, String playerName) {
		//MCNSAChat.console.info("Checking for permission: "+checkPermission);
		if (MCNSAChat.groupManager.getWorldsHolder().getWorldPermissions("world").permission(playerName, corePermission+permission)) {
			return true;
		}
		return false;
	}
	public static Boolean useColours(String playerName) {
		if (MCNSAChat.groupManager.getWorldsHolder().getWorldPermissions("world").permission(playerName, corePermission+"player.cancolour"))
			return true;
		
		return false;
	}
	public static Boolean forcelisten(String playerName, String channel) {
		if (MCNSAChat.groupManager.getWorldsHolder().getWorldPermissions("world").permission(playerName, corePermission+"forcelisten."+channel))
			return true;
		
		return false;
	}
	public static ArrayList<String> getForceListens(String playerName) {
		ArrayList<String> permissions = new ArrayList<String>();
		//loop through the channels in the channel manager and check force listen perms
		for (int i = 0; i < ChannelManager.channels.size(); i++) {
			if (MCNSAChat.groupManager.getWorldsHolder().getWorldPermissions("world").permission(playerName, corePermission+"forcelisten."+ChannelManager.channels.get(i).name))
				permissions.add(ChannelManager.channels.get(i).name);
		}
		return permissions;
	}
	public static String getSuffix(String player) {
		String suffix = MCNSAChat.groupManager.getWorldsHolder().getWorldPermissions("world").getUserSuffix(player);
		return suffix;
	}

}
