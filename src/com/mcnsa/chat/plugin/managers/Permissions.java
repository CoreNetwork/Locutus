package com.mcnsa.chat.plugin.managers;

import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import com.mcnsa.chat.plugin.MCNSAChat;

public class Permissions {
	public static String corePermission = "MCNSAChat.";
	public static PermissionManager pex = PermissionsEx.getPermissionManager();
	private MCNSAChat plugin;
	
	public Permissions(){
		this.plugin = MCNSAChat.plugin;
	}
	public static boolean checkPlayerPerm(String permission, String playerName) {
		PermissionUser user = pex.getUser(playerName);
		if (user.has(corePermission+"player."+permission)) 
			return true;
		return false;
	}
	public static boolean checkAdminPerm(String permission, String playerName) {
		PermissionUser user = pex.getUser(playerName);
		if (user.has(corePermission+"admin."+permission)) 
			return true;
		return false;
	}
	public static boolean checkReadPerm(String permission, String playerName) {
		PermissionUser user = pex.getUser(playerName);
		if (user.has(corePermission+"read."+permission)) 
			return true;
		return false;
	}
	public static boolean checkWritePerm(String permission, String playerName) {
		PermissionUser user = pex.getUser(playerName);
		if (user.has(corePermission+"write."+permission)) 
			return true;
		return false;
	}
	public static String getPrefix(String playerName){
		PermissionUser user = pex.getUser(playerName);
		return user.getPrefix();
	}
}
