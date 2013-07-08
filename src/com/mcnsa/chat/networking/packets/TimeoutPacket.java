package com.mcnsa.chat.networking.packets;

import java.io.Serializable;

import com.mcnsa.chat.type.ChatPlayer;

public class TimeoutPacket implements Serializable{

	private static final long serialVersionUID = 972058071008966303L;
	public String reason;
	public ChatPlayer player;
	public long time;
}
