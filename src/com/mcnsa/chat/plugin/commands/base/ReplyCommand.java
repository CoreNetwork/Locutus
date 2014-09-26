package com.mcnsa.chat.plugin.commands.base;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcnsa.chat.plugin.abstracts.AbstractChatCommand;
import com.mcnsa.chat.plugin.managers.PlayerManager;
import com.mcnsa.chat.plugin.utils.MessageSender;
import com.mcnsa.chat.type.ChatPlayer;

public class ReplyCommand extends AbstractChatCommand {

    public ReplyCommand() {
	super("reply", "Replies to a message", "none", true, "msg", "r");
	// TODO Auto-generated constructor stub
    }

    @Override
    public boolean run(CommandSender sender, String[] args) {
	Player p = (Player) sender;
	// Get the player
	ChatPlayer playerSender = PlayerManager.getPlayer(p);
	ChatPlayer target = PlayerManager.getPlayer(playerSender.lastPm);
	if (target == null) {
	    MessageSender.send("There is no one to reply to", playerSender);
	    return true;
	}
	return p.performCommand("message " + target.getName() + " " + args);
    }
}
