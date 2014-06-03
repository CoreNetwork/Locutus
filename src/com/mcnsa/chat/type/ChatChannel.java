package com.mcnsa.chat.type;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ChatChannel  implements Serializable{
	//TODO Whole class is only for networking?
	private static final long serialVersionUID = -7949153652183232773L;
	public String name;
	public String writePermission;
	public String readPermission;
	public Map<String, Boolean> modes = new HashMap<String, Boolean>();;
	public String alias;
	public String color;
	
	public ChatChannel(String cname){
		this.name = cname;
		this.writePermission = null;
		this.readPermission = null;
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
		this.writePermission = wperm;
		this.readPermission = rperm;
		this.alias = alias;
		this.color = colour;
		this.modes.put("MUTE", false);
		this.modes.put("RAVE", false);
		this.modes.put("BORING", false);
		this.modes.put("LOCAL", false);
		this.modes.put("PERSIST", false);
		
	}
	
	//TODO Networking, needs fixing?
	public void write(DataOutputStream out) throws IOException{
		out.writeUTF(this.name);
		if (this.writePermission == null)
			out.writeUTF("null");
		else
			out.writeUTF(this.writePermission);
		
		if (this.readPermission == null)
			out.writeUTF("null");
		else
			out.writeUTF(this.readPermission);
		
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
	
	//TODO Networking, needs fixing?
	public static ChatChannel read(DataInputStream in) throws IOException{
		String name = in.readUTF();
		String writePerm = in.readUTF();
		String readPerm = in.readUTF();
		String alias = in.readUTF();
		String color = in.readUTF();
		HashMap<String, Boolean> modes = new HashMap<String, Boolean>();
		modes.put("MUTE", in.readBoolean());
		modes.put("RAVE", in.readBoolean());
		modes.put("BORING", in.readBoolean());
		modes.put("LOCAL", in.readBoolean());
		modes.put("PERSIST", in.readBoolean());
		
		if (writePerm.equals("null"))
			writePerm = null;
		
		if (readPerm.equals("null"))
			readPerm = null;
		if (alias.equals("null"))
			alias = null;
		if (color.equals("null"))
			color = null;
		ChatChannel c = new ChatChannel(name, writePerm, readPerm, alias, color);
		c.modes = modes;
		
		return c;
	}
}
