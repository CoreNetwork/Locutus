package com.mcnsa.chat.plugin.commands.chat.lockdown;

import org.bukkit.command.CommandSender;

import com.mcnsa.chat.plugin.abstracts.AbstractChatCommand;

public class LockdownOffCommand extends AbstractChatCommand {

    public LockdownOffCommand() {
	super("off", "Turns server lockdown off", "none", false, "off");
    }

    @Override
    public boolean run(CommandSender sender, String[] args) {
	// TODO Auto-generated method stub
	return false;
    }

}
