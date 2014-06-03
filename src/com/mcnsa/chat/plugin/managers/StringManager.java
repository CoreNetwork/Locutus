package com.mcnsa.chat.plugin.managers;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;

import com.mcnsa.chat.plugin.MCNSAChat;
import com.mcnsa.chat.plugin.utils.ConsoleLogging;
import com.mcnsa.chat.type.Pair;


public class StringManager {
	public static boolean isLoaded = false;
	private static File stringFile;
	public static void loadFile(String fileName){
		StringManager.stringFile = new File(MCNSAChat.plugin.getDataFolder(), "strings.yml");
		if(!StringManager.stringFile.exists()){
			try {
				StringManager.stringFile.createNewFile();
			} catch (IOException e) {
				ConsoleLogging.severe("Could not load or create strings.yml");
				return;
			}
		} 
		//TODO finish whatever this is
		//StringManager.stringFile.
		YamlConfiguration.loadConfiguration(StringManager.stringFile);
		isLoaded = true;
	}
	/**
	 * Replaces all the variables in a String with their values
	 * @param The message to do the replacing
	 * @param A Pair of values, contain first the String variable, and second the String replacing factor
	 * @return
	 */
	public static String replaceVariables(String message, Pair... pairs){
		for (Pair pair : pairs){
			message=message.replace(pair.getLeft().toString(),pair.getRight().toString());
		}
		return message;
		
	}
	
	public static String getString(String name){
		return null;
	}
}
