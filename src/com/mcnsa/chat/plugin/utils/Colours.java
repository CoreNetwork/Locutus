package com.mcnsa.chat.plugin.utils;

import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;

import ru.tehkode.permissions.PermissionUser;

import com.mcnsa.chat.plugin.managers.Permissions;

public class Colours {
	public static String PlayerPrefix(String playerName) {
		PermissionUser user = Permissions.pex.getUser(playerName);
		String prefix = user.getPrefix();
		return color(prefix);
	}
	public static String processConsoleColours(String str) {
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
	public static String stripColor(String str) {
		int count = StringUtils.countMatches(str, "&");
		for (int i=0; i < count; i++) {
			str = ChatColor.stripColor(color(str));
		}
		return str;
		
	}
	public static String color(String str) {
		return ChatColor.translateAlternateColorCodes('&', str);
	}
	public static String raveColor(String rawMessage) {
		Random r = new Random();
		String newStr = "";
		String colors = "123456789abcde";
		for (int i = 0; i < rawMessage.length(); i++)
			newStr += "&" + colors.charAt(r.nextInt(colors.length())) + rawMessage.charAt(i);
		return newStr;
	}
}
