package com.mcnsa.chat.plugin.managers;

import com.mcnsa.chat.plugin.MCNSAChat;

import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class Permissions {
	public static String corePermission = "MCNSAChat.";
	public static PermissionManager pex = PermissionsEx.getPermissionManager();
	public Permissions(){
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
		if (permission.length() < 1 && user.has(corePermission+"read")) {
			return true;
		}
		if (user.has(corePermission+"read."+permission)) 
			return true;
		return false;
	}
	public static boolean checkWritePerm(String permission, String playerName) {
		PermissionUser user = pex.getUser(playerName);
		if (permission.length() < 1 && user.has(corePermission+"write")) {
			return true;
		}
		if (user.has(corePermission+"write."+permission)) {
			MCNSAChat.console.info("Checking permission "+corePermission+"write."+permission);
			return true;
		}
		return false;
	}
	public static boolean checkPermission (String permission, String playerName) {
		PermissionUser user = pex.getUser(playerName);
		if (user.has(corePermission+permission)) 
			return true;
		return false;
	}
	public static String getPrefix(String playerName){
		PermissionUser user = pex.getUser(playerName);
		return user.getPrefix();
	}
	public static Boolean useColours(String playerName) {
		PermissionUser user = pex.getUser(playerName);
		if (user.has(corePermission+"player.usecolour")) 
			return true;
		return false;
	}
}
