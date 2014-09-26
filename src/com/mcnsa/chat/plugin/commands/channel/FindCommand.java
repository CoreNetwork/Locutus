package com.mcnsa.chat.plugin.commands.channel;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.mcnsa.chat.plugin.abstracts.AbstractChatCommand;
import com.mcnsa.chat.plugin.managers.ChannelManager;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.plugin.utils.MessageSender;
import com.mcnsa.chat.type.ChatPlayer;

public class FindCommand extends AbstractChatCommand {

    public FindCommand() {
	super("find", "Find which channel a player is in", "None", false, "find", new String[0]);
    }

    @Override
    public boolean run(CommandSender sender, String[] args) {
	if (args.length == 0)
	    usage(sender);
	for (String arg : args) {
	    ChatPlayer player = PlayerManager.searchPlayer(arg);
	    if (player == null) {

		MessageSender.send("&cCould not find: " + arg, sender);
	    }
	    List<String> channels = ChannelManager.getChannelList(player);
	    StringBuilder sb = new StringBuilder();
	    if (channels.isEmpty()) {
		sb.append(player.getName() + " is not listening to any channels");
	    } else {
		sb.append(player.getName() + " is listening to channels: ");
		sb.append(channels.get(0));
	    }
	    for (int i = 1; i < channels.size(); i++) {
		sb.append(", " + channels.get(i));
	    }
	    sender.sendMessage(sb.toString());
	}
	return false;
    }

}
