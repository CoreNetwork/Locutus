package com.mcnsa.chat.type;

import java.util.HashMap;
import java.util.Map;

public class ChatChannel {
	public String name;
	public String write_permission;
	public String read_permission;
	public Map<String, Boolean> modes = new HashMap<String, Boolean>();;
	public String alias;
	public String color;
	
	public ChatChannel(String cname){
		this.name = cname;
		this.write_permission = null;
		this.read_permission = null;
		this.alias = null;
		this.modes.put("MUTE", false);
		this.modes.put("RAVE", false);
		this.modes.put("BORING", false);
		this.modes.put("LOCAL", false);
		this.modes.put("PERSIST", false);
		this.color = "&f";
	}
	public ChatChannel(String cname, String wperm, String rperm, String alias, String colour){
		this.name = cname;
		this.write_permission = wperm;
		this.read_permission = rperm;
		this.alias = alias;
		this.modes.put("MUTE", false);
		this.modes.put("RAVE", false);
		this.modes.put("BORING", false);
		this.modes.put("LOCAL", false);
		this.modes.put("PERSIST", false);
		this.color = colour;
	}
}
