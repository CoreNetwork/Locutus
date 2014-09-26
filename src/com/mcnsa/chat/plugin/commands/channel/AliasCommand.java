package com.mcnsa.chat.plugin.commands.channel;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.chat.plugin.abstracts.AbstractChatCommand;
import com.mcnsa.chat.plugin.managers.ChannelManager;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.plugin.utils.ConsoleLogging;
import com.mcnsa.chat.plugin.utils.MessageSender;
import com.mcnsa.chat.type.ChatChannel;

public class AliasCommand extends AbstractChatCommand {

    public AliasCommand() {
	super("alias", "Change the alias of a channel", "None", false, "modify", new String[0]);
    }

    @Override
    public boolean run(CommandSender sender, String[] args) {
	if (args.length > 2 || args.length == 0) {
	    usage(sender);
	    return false;
	}
	if (args.length == 1) {
	    if (sender instanceof Player) {
		ChatChannel channel = ChannelManager.getChannel(PlayerManager.getPlayer(sender).channel);
		ConsoleLogging.debug("Length: " + args.length);
		ConsoleLogging.debug(args[0]);
		channel.alias = args[0];
		MessageSender.send("&6Channel alias changed to: /" + args[0], sender);

	    } else {
		return false;
	    }
	} else {
	    ChatChannel channel = ChannelManager.getChannel(args[0]);
	    channel.alias = args[1];
	    MessageSender.send("&6Channel alias changed to: /" + args[1], sender);
	}

	return false;
    }

}
