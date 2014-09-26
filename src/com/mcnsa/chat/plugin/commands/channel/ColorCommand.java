package com.mcnsa.chat.plugin.commands.channel;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.chat.plugin.abstracts.AbstractChatCommand;
import com.mcnsa.chat.plugin.managers.ChannelManager;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.plugin.utils.MessageSender;
import com.mcnsa.chat.type.ChatChannel;

public class ColorCommand extends AbstractChatCommand {

    private HashMap<String, ChatColor> colors;

    public ColorCommand() {
	super("color", "Changes the color of a channel", "None", false, "color", new String[0]);
	colors = new HashMap<String, ChatColor>();

	// Black
	colors.put("black", ChatColor.BLACK);
	colors.put("&0", ChatColor.BLACK);
	colors.put("§0", ChatColor.BLACK);

	// Dark blue
	colors.put("darkblue", ChatColor.DARK_BLUE);
	colors.put("dark_blue", ChatColor.DARK_BLUE);
	colors.put("dark-blue", ChatColor.DARK_BLUE);
	colors.put("&1", ChatColor.DARK_BLUE);
	colors.put("§1", ChatColor.DARK_BLUE);

	// Dark Green
	colors.put("darkgreen", ChatColor.DARK_GREEN);
	colors.put("dark_green", ChatColor.DARK_GREEN);
	colors.put("dark-green", ChatColor.DARK_GREEN);
	colors.put("&2", ChatColor.DARK_GREEN);
	colors.put("§2", ChatColor.DARK_GREEN);

	// Dark Aqua
	colors.put("darkaqua", ChatColor.DARK_AQUA);
	colors.put("dark_aqua", ChatColor.DARK_AQUA);
	colors.put("dark-aqua", ChatColor.DARK_AQUA);
	colors.put("&3", ChatColor.DARK_AQUA);
	colors.put("§3", ChatColor.DARK_AQUA);

	// Dark Red
	colors.put("darkred", ChatColor.DARK_RED);
	colors.put("dark_red", ChatColor.DARK_RED);
	colors.put("dark-red", ChatColor.DARK_RED);
	colors.put("&4", ChatColor.DARK_RED);
	colors.put("§4", ChatColor.DARK_RED);

	// Dark Purple
	colors.put("darkpurple", ChatColor.DARK_PURPLE);
	colors.put("dark_purple", ChatColor.DARK_PURPLE);
	colors.put("dark-purple", ChatColor.DARK_PURPLE);
	colors.put("&5", ChatColor.DARK_PURPLE);
	colors.put("§5", ChatColor.DARK_PURPLE);

	// Gold
	colors.put("gold", ChatColor.GOLD);
	colors.put("&6", ChatColor.GOLD);
	colors.put("§6", ChatColor.GOLD);

	// Gray
	colors.put("grey", ChatColor.GRAY);
	colors.put("gray", ChatColor.GRAY);
	colors.put("&7", ChatColor.GRAY);
	colors.put("§7", ChatColor.GRAY);

	// Dark Gray
	colors.put("darkgray", ChatColor.DARK_GRAY);
	colors.put("dark_gray", ChatColor.DARK_GRAY);
	colors.put("dark-gray", ChatColor.DARK_GRAY);
	colors.put("darkgrey", ChatColor.DARK_GRAY);
	colors.put("dark_grey", ChatColor.DARK_GRAY);
	colors.put("dark-grey", ChatColor.DARK_GRAY);
	colors.put("&8", ChatColor.DARK_GRAY);
	colors.put("§8", ChatColor.DARK_GRAY);

	// Blue
	colors.put("blue", ChatColor.BLUE);
	colors.put("&9", ChatColor.BLUE);
	colors.put("§9", ChatColor.BLUE);

	// Green
	colors.put("green", ChatColor.GREEN);
	colors.put("&a", ChatColor.GREEN);
	colors.put("§a", ChatColor.GREEN);

	// Aqua
	colors.put("aqua", ChatColor.AQUA);
	colors.put("&b", ChatColor.AQUA);
	colors.put("§b", ChatColor.AQUA);

	// Red
	colors.put("red", ChatColor.RED);
	colors.put("&c", ChatColor.RED);
	colors.put("§c", ChatColor.RED);

	// Light purple
	colors.put("lightpurple", ChatColor.LIGHT_PURPLE);
	colors.put("light_purple", ChatColor.LIGHT_PURPLE);
	colors.put("light-purple", ChatColor.LIGHT_PURPLE);
	colors.put("&d", ChatColor.LIGHT_PURPLE);
	colors.put("§d", ChatColor.LIGHT_PURPLE);

	// Yellow
	colors.put("yellow", ChatColor.YELLOW);
	colors.put("&e", ChatColor.YELLOW);
	colors.put("§e", ChatColor.YELLOW);

	// White
	colors.put("white", ChatColor.WHITE);
	colors.put("&f", ChatColor.WHITE);
	colors.put("§f", ChatColor.WHITE);

	// Bold
	colors.put("bold", ChatColor.BOLD);
	colors.put("&l", ChatColor.BOLD);
	colors.put("§l", ChatColor.BOLD);

	// Italic
	colors.put("gold", ChatColor.ITALIC);
	colors.put("&o", ChatColor.ITALIC);
	colors.put("§o", ChatColor.ITALIC);

	// Underline
	colors.put("underline", ChatColor.UNDERLINE);
	colors.put("&n", ChatColor.UNDERLINE);
	colors.put("§n", ChatColor.UNDERLINE);

    }

    private ChatColor getColor(String colorName) {

	return colors.get(colorName.toLowerCase());
    }

    @Override
    public boolean run(CommandSender sender, String[] args) {
	if (args.length > 2 || args.length == 0)
	    return false;
	if (args.length == 1) {
	    if (sender instanceof Player) {
		ChatChannel channel = ChannelManager.getChannel(PlayerManager.getPlayer(sender).channel);
		channel.color = getColor(args[0]);
		MessageSender.send("&6Channel color changed to: /" + args[0], sender);

	    } else {
		return false;
	    }
	} else {
	    ChatChannel channel = ChannelManager.getChannel(args[0]);
	    channel.color = getColor(args[1]);
	    MessageSender.send("&6Channel color changed to: /" + args[1], sender);
	}

	return false;
    }

}
