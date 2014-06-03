package com.mcnsa.chat.plugin.managers;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import java.util.ArrayList;

import org.bukkit.Bukkit;

import com.mcnsa.chat.type.ChatPlayer;

public class PermissionManager {
	public static String corePermission = "mcnsachat.";
	public static Permission perms;
	public PermissionManager(){
	}
	public static boolean setupPermissions()
	{
		RegisteredServiceProvider<Permission> rsp = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
		perms = rsp.getProvider();
		return perms != null;
	}
	public static boolean checkPermission (String permission, String playerName) {
		if (permission == null || permission.equals("")) {
			return true;
		}
		if (perms.has("world", playerName, corePermission + permission)){
			return true;
		}
		return false;
	}
	public static boolean checkPermission(String permission, Player player){
		if (permission == null || permission.equals("")) {
			return true;
		}
		return perms.has(player, permission);
		
	}
	public static boolean checkPermission(String permission, ChatPlayer player){
		return checkPermission(permission, Bukkit.getPlayer(player.getUUID()));
	}
	public static boolean canUseColours(Player player) {
		return perms.has(player, corePermission+"player.cancolour");
	}
	public static boolean canUseColours(ChatPlayer player) {
		return perms.has(Bukkit.getPlayer(player.getUUID()), corePermission+"player.cancolour");
	}
	public static boolean forceListen(Player player, String channel) {
		return perms.has(player, corePermission+"forcelisten."+channel);
	}
	public static boolean forceListen(ChatPlayer player, String channel) {
		return forceListen(Bukkit.getPlayer(player.getUUID()), channel);
	}
	public static ArrayList<String> getForceListens(Player player) {
		ArrayList<String> permissions = new ArrayList<String>();
		//loop through the channels in the channel manager and check force listen perms
		for (String chan : ChannelManager.getChannelList()){
			if (PermissionManager.forceListen(player, chan))
				permissions.add(chan);
		}
		return permissions;
	}
	public static ArrayList<String> getForceListens(ChatPlayer player) {
		ArrayList<String> permissions = new ArrayList<String>();
		//loop through the channels in the channel manager and check force listen perms
		for (String chan : ChannelManager.getChannelList()){
			if (PermissionManager.forceListen(player, chan))
				permissions.add(chan);
		}
		return permissions;
	}
	
}
