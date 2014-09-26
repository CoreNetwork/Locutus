package com.mcnsa.chat.plugin.commands.base;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.mcnsa.chat.plugin.abstracts.AbstractChatCommand;
import com.mcnsa.chat.plugin.managers.ChannelManager;
import com.mcnsa.chat.plugin.utils.ConsoleLogging;
import com.mcnsa.chat.type.ChatChannel;

public class ChannelsCommand extends AbstractChatCommand {

    public ChannelsCommand() {
	super("channels", "List all channels", "none", false, "list");
    }

    @Override
    public boolean run(CommandSender sender, String[] args) {
	List<ChatChannel> channels = ChannelManager.getChatChannelList();
	ConsoleLogging.debug("Size of channels: " + channels.size());
	for (ChatChannel c : channels) {
	    sender.sendMessage(c.name);
	}
	return true;
    }

}
