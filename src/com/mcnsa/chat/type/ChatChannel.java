package com.mcnsa.chat.type;

import java.util.Map;

public class ChatChannel {
	public String name;
	public String write_permission;
	public String read_permission;
	public Map<String, Boolean> modes;
	public String alias;
	
	public ChatChannel(String name){
		this.name = name;
		this.write_permission = null;
		this.read_permission = null;
		this.alias = null;
		this.modes.put("MUTE", false);
		this.modes.put("RAVE", false);
		this.modes.put("BORING", false);
		this.modes.put("LOCAL", false);
	}
	public ChatChannel(String name, String write, String read, String alias, Map<String, Boolean> modes){
		this.name = name;
		this.write_permission = write;
		this.read_permission = read;
		this.modes = modes;
		this.alias = alias;
	}
}
