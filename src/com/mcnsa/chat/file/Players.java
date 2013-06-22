package com.mcnsa.chat.file;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.mcnsa.chat.plugin.MCNSAChat;

public class Players {
	private FileConfiguration customConfig = null;
	  private File customConfigFile = null;
	  private String player;

	  public Players(String player) {
	    this.player = player;
	  }

	  public void reload() {
	    if (this.customConfigFile == null)
	      this.customConfigFile = new File("plugins/MCNSAChat/Players/"+this.player+".yml");
	    this.customConfig = YamlConfiguration.loadConfiguration(this.customConfigFile);
	  }

	  public FileConfiguration get() {
		  if (this.customConfig == null) {
			 reload();
		  }
	    return this.customConfig;
	  }

	  public void clear() {
	    this.customConfig = new YamlConfiguration();
	  }

	  public void save() {
	    if ((this.customConfig == null) || (this.customConfigFile == null))
	      return;
	    try {
	      get().save(this.customConfigFile);
	    } catch (IOException ex) {
	      MCNSAChat.console.warning("Could not save config to " + this.customConfigFile);
	    }
	  }
}
