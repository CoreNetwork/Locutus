package com.mcnsa.chat.type;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ChatChannel  implements Serializable{

	private static final long serialVersionUID = -7949153652183232773L;
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
		this.color = colour;
		this.modes.put("MUTE", false);
		this.modes.put("RAVE", false);
		this.modes.put("BORING", false);
		this.modes.put("LOCAL", false);
		this.modes.put("PERSIST", false);
		
	}
	
	public void write(DataOutputStream out) throws IOException{
		out.writeUTF(this.name);
		if (this.write_permission == null)
			out.writeUTF("null");
		else
			out.writeUTF(this.write_permission);
		
		if (this.read_permission == null)
			out.writeUTF("null");
		else
			out.writeUTF(this.read_permission);
		
		if (this.alias == null)
			out.writeUTF("null");
		else
			out.writeUTF(this.alias);
		
		if (this.color == null)
			out.writeUTF("null");
		else
			out.writeUTF(this.color);
		
		out.writeBoolean(this.modes.get("MUTE"));
		out.writeBoolean(this.modes.get("RAVE"));
		out.writeBoolean(this.modes.get("BORING"));
		out.writeBoolean(this.modes.get("LOCAL"));
		out.writeBoolean(this.modes.get("PERSIST"));
	}
	
	public static ChatChannel read(DataInputStream in) throws IOException{
		String name = in.readUTF();
		String write_perm = in.readUTF();
		String read_perm = in.readUTF();
		String alias = in.readUTF();
		String color = in.readUTF();
		HashMap<String, Boolean> modes = new HashMap<String, Boolean>();
		modes.put("MUTE", in.readBoolean());
		modes.put("RAVE", in.readBoolean());
		modes.put("BORING", in.readBoolean());
		modes.put("LOCAL", in.readBoolean());
		modes.put("PERSIST", in.readBoolean());
		
		if (write_perm.equals("null"))
			write_perm = null;
		
		if (read_perm.equals("null"))
			read_perm = null;
		if (alias.equals("null"))
			alias = null;
		if (color.equals("null"))
			color = null;
		ChatChannel c = new ChatChannel(name, write_perm, read_perm, alias, color);
		c.modes = modes;
		
		return c;
	}
}
