package com.mcnsa.chat.plugin.commands.chat.lockdown;

import org.bukkit.command.CommandSender;

import com.mcnsa.chat.plugin.abstracts.AbstractChatCommand;

public class LockdownOnCommand extends AbstractChatCommand {

    public LockdownOnCommand() {
	super("on", "Turns server lockdown on", "none", false, "on");
    }

    @Override
    public boolean run(CommandSender sender, String[] args) {
	// TODO Auto-generated method stub
	return false;
    }

}
