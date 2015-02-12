package com.mcnsa.chat.plugin.managers;

import com.mcnsa.chat.networking.Network;
import com.mcnsa.chat.plugin.MCNSAChat;
import com.mcnsa.chat.plugin.utils.Colours;
import com.mcnsa.chat.plugin.utils.MessageSender;
import com.mcnsa.chat.type.ChatPlayer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.minecraft.server.v1_8_R1.ChatSerializer;
import net.minecraft.server.v1_8_R1.EntityPlayer;
import net.minecraft.server.v1_8_R1.EnumPlayerInfoAction;
import net.minecraft.server.v1_8_R1.IChatBaseComponent;
import net.minecraft.server.v1_8_R1.PacketPlayOutPlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PlayerManager {
	public static ArrayList<ChatPlayer> players;
	
	public PlayerManager() {
		players = new ArrayList<ChatPlayer>();
	}
	
	public static void PlayerLogin(Player player){
		
		if (PlayerManager.getPlayer(player.getName(), MCNSAChat.shortCode) == null) {
			ChatPlayer newPlayer= new ChatPlayer(player);

			if (MCNSAChat.isLockdown && newPlayer.firstTime)
			{
				return;
			}
			players.add(newPlayer);
			if (Permissions.checkPermission("admin.notify", newPlayer.name) && MCNSAChat.isLockdown)
			{
				if (MCNSAChat.lockdownTimerID == 0)
				{

					String message = MCNSAChat.plugin.getConfig()
					.getString("strings.lockdown-login-persist");
					message.replace("%reason%", MCNSAChat.lockdownReason);
					MessageSender.send(message,newPlayer.name );
				}
				else
				{
					String message = MCNSAChat.plugin.getConfig()
					.getString("strings.lockdown-login-temp");
					
					long currentTime = new Date().getTime();
					long timeLeft = MCNSAChat.lockdownUnlockTime - currentTime;
					message = message.replace("%seconds%", String.valueOf(TimeUnit.MILLISECONDS.toSeconds(timeLeft) % 60));
					message = message.replace("%minutes%", String.valueOf(TimeUnit.MILLISECONDS.toMinutes(timeLeft) % 60));
					message = message.replace("%reason%", MCNSAChat.lockdownReason);
					MessageSender.send(message, player.getName() );
				}
			}
		}
	}
	public static void PlayerLogout(String player){
		ChatPlayer cplayer = getPlayer(player, MCNSAChat.shortCode);
		if (cplayer == null)
			return;
		if (!MCNSAChat.isLockdown)
			cplayer.savePlayer();
		//Notify network
		Network.playerQuit(cplayer);
		removePlayer(player, MCNSAChat.shortCode);
	}
	public static void removePlayer(String name, String server) {
	    for (int i = 0; i < players.size(); i++) {
	      ChatPlayer play = (ChatPlayer)players.get(i);
	      if (play.name.equalsIgnoreCase(name) && play.server.equalsIgnoreCase(server)) {
	        players.remove(play);
	      }
	    }
	}
	public static ChatPlayer getPlayer(String name) {
		for (ChatPlayer player: players) {
			if (player.name.equalsIgnoreCase(name)){
				return player;
			}
		}
		return null;
	}

    public static ChatPlayer getPlayer(String name, String server) {
		for (ChatPlayer player: players) {
			if (player.name.equalsIgnoreCase(name) && player.server.equals(server)){
				return player;
			}
		}
		return null;
	}
	public static List<ChatPlayer> getServerPlayers(String server) {
		List<ChatPlayer> serverPlayers = new ArrayList<ChatPlayer>();
		for (int i = 0; i < players.size(); i++) {
			ChatPlayer player = players.get(i);
			if (player.server.equals(server) && !serverPlayers.contains(player)) {
				serverPlayers.add(player);
			}
		}
		
		return serverPlayers;
	}

	public static ArrayList<ChatPlayer> playerSearch(String string) {
		ArrayList<ChatPlayer> results = new ArrayList<ChatPlayer>();
		for (int i = 0; i < players.size(); i++){
			if (players.get(i).name.toLowerCase().startsWith(string.toLowerCase()) && !results.contains(players.get(i))) {
				results.add(players.get(i));
			}
		}
		return results;
	}
	public static void unmutePlayer(String Player) {
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i).name.equalsIgnoreCase(Player)) {
				players.get(i).modes.put("MUTE", false);
				
				//Notify player
				MessageSender.send("&6You have been removed from timeout", Player);
				//UpdatePlayers on network
				Network.updatePlayer(players.get(i));
			}
		}
	}
	public static void shadowUnmutePlayer(String Player) {
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i).name.equalsIgnoreCase(Player)) {
				players.get(i).modes.put("S-MUTE", false);
				
				String notifyMessage = MCNSAChat.plugin.getConfig().getString("strings.shadow-unmute-notify");
				for (ChatPlayer p : players)
				{
					if(Permissions.checkPermission("admin.shadow-notify", p.name))
					{
						MessageSender.send(notifyMessage.replace("%player%", players.get(i).name), p.name);
					}
				}
				//UpdatePlayers on network
				Network.updatePlayer(players.get(i));
			}
		}
	}
	
	public static void mutePlayer(String Player, String time, String reason) {
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i).name.equalsIgnoreCase(Player)) {
				players.get(i).modes.put("MUTE", true);
				players.get(i).timeoutTill = (long) (new Date().getTime() + (Double.valueOf(time) * 60000));
				//Inform the player
				if (players.get(i).server.equals(MCNSAChat.shortCode))
					MessageSender.timeoutPlayer(Player, time, reason);
				//UpdatePlayers on network
				Network.updatePlayer(players.get(i));
			}
		}
	}
	
	public static void shadowMutePlayer(String Player, String time, String reason) {
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i).name.equalsIgnoreCase(Player)) {
				players.get(i).modes.put("S-MUTE", true);
				players.get(i).timeoutTill = (long) (new Date().getTime() + (Double.valueOf(time) * 60000));
				
				
				String notifyMessage = MCNSAChat.plugin.getConfig().getString("strings.shadow-mute-notify");
				notifyMessage = notifyMessage.replace("%player%", players.get(i).name);
				notifyMessage = notifyMessage.replace("%time%", time);
				for (ChatPlayer p : players)
				{
					if(Permissions.checkPermission("admin.shadow-notify", p.name))
					{
						MessageSender.send(notifyMessage, p.name);
					}
				}
				//UpdatePlayers on network
				Network.updatePlayer(players.get(i));
				return;
			}
		}
	}

	public static void updatePlayer(ChatPlayer player) {
		PlayerManager.players.remove(PlayerManager.getPlayer(player.name, player.server));
		PlayerManager.players.add(player);
	}

	public static void addPlayers(ArrayList<ChatPlayer> players2) {
		for (ChatPlayer play: players2) {
			if (getPlayer(play.name, play.server) == null)
				players.add(play);
		}
		
	}

	public static void removeNonServerPlayers() {
		ArrayList<ChatPlayer> newPlayers = new ArrayList<ChatPlayer>();
		for (Player player: Bukkit.getOnlinePlayers()) {
			newPlayers.add(getPlayer(player.getName(), MCNSAChat.shortCode));
		}
		players = newPlayers;
	}
	
	public static void kickPlayer(String player)
	{
		kickPlayer(Bukkit.getServer().getPlayer(player));
	}
	
	public static void kickPlayer(ChatPlayer player)
	{
		kickPlayer(player.name);
	}

	public static void kickPlayer(Player player)
	{
		player.kickPlayer("");
	}
	
	public static void kickPlayer(String player, String message)
	{
		kickPlayer(Bukkit.getServer().getPlayer(player), message);
	}
	
	public static void kickPlayer(ChatPlayer player, String message)
	{
		kickPlayer(player.name, message);
	}
	
	public static void kickPlayer(Player player, String message)
	{
		player.kickPlayer(message);
	}

    public static void updateTabNames(Player player)
    {
        final EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();

        // Change the name on the client side
        for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
            if (otherPlayer.equals(player))
                continue;

            String playerName = otherPlayer.getName();
            String nameWithPrefix = Colours.PlayerPrefix(playerName).replaceAll("[^a-zA-Z0-9&]", "");
            String playerlistName = Colours.color(nameWithPrefix
                    + playerName);
            if (playerlistName.length() > 16)
                playerlistName = playerlistName.substring(0, 16);

            EntityPlayer nmsOtherPlayer = ((CraftPlayer) otherPlayer).getHandle();

            nmsOtherPlayer.listName = ChatSerializer.a(playerlistName);
            final PacketPlayOutPlayerInfo realNamePacket = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, nmsOtherPlayer);

            if (player.canSee(otherPlayer)) {

                Bukkit.getScheduler().runTask(MCNSAChat.plugin, new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (nmsPlayer.playerConnection != null)
                         nmsPlayer.playerConnection.sendPacket(realNamePacket);
                    }
                });
            }
        }
    }

}
