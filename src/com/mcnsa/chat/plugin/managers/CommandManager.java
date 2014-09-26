package com.mcnsa.chat.plugin.managers;

import java.util.ArrayList;
import java.util.List;

import com.mcnsa.chat.plugin.abstracts.AbstractChatCommand;
import com.mcnsa.chat.plugin.commands.base.ChannelCommand;
import com.mcnsa.chat.plugin.commands.base.ChannelsCommand;
import com.mcnsa.chat.plugin.commands.base.MeCommand;
import com.mcnsa.chat.plugin.commands.base.MessageCommand;
import com.mcnsa.chat.plugin.commands.base.MuteCommand;
import com.mcnsa.chat.plugin.commands.base.ReplyCommand;
import com.mcnsa.chat.plugin.commands.base.SeenCommand;
import com.mcnsa.chat.plugin.commands.base.SilenceCommand;
import com.mcnsa.chat.plugin.commands.base.TimeoutCommand;
import com.mcnsa.chat.plugin.commands.base.UnmuteCommand;
import com.mcnsa.chat.plugin.commands.base.UnsilenceCommand;
import com.mcnsa.chat.plugin.commands.base.UntimeoutCommand;
import com.mcnsa.chat.plugin.commands.chat.ChatCommand;
import com.mcnsa.chat.plugin.utils.ConsoleLogging;

public class CommandManager {
    public static List<AbstractChatCommand> commands;

    public static void loadCommands() {
	commands = new ArrayList<AbstractChatCommand>();
	commands.add(new SilenceCommand());
	commands.add(new UnsilenceCommand());
	commands.add(new ChatCommand());
	commands.add(new ChannelCommand());
	commands.add(new ChannelsCommand());
	commands.add(new MessageCommand());
	commands.add(new ReplyCommand());
	commands.add(new SeenCommand());
	commands.add(new MeCommand());
	commands.add(new TimeoutCommand());
	commands.add(new UntimeoutCommand());
	commands.add(new MuteCommand());
	commands.add(new UnmuteCommand());
	ConsoleLogging.info("Loading commands...");
	for (AbstractChatCommand c : commands) {
	    ConsoleLogging.info("Loaded command: " + c.name);
	}
    }
}
