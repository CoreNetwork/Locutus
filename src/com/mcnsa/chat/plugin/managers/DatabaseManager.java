package com.mcnsa.chat.plugin.managers;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.mcnsa.chat.plugin.MCNSAChat;
import com.mcnsa.chat.plugin.annotations.DatabaseTableInfo;
import com.mcnsa.chat.plugin.annotations.Setting;
import com.mcnsa.chat.plugin.exceptions.DatabaseException;
import com.mcnsa.chat.plugin.utils.ConsoleLogging;

public class DatabaseManager {
	// our connection settings

	private static HashMap<String, String> tableConstructions = new HashMap<String, String>();
	// our connections
	private static Connection connection = null;
	private static PreparedStatement preparedStatement = null;
	private static ResultSet resultSet = null;
	private static String url = "chat.db";
	
	public void enable() {
		try {
		    Class.forName("org.sqlite.JDBC");
			// connect
			url = new File(MCNSAChat.plugin.getDataFolder().getPath(), url).getPath();
			//url = (String) MCNSAChat.plugin.getConfig().get("database-url");
			connect();
			
			//chatPlayers, chatServers, and the foreign key table
			String[] fields = { "player VARCHAR(100) NOT NULL PRIMARY KEY UNIQUE", "channel VARCHAR(100)", "lastPM VARCHAR(255)", "timeoutTill BIGINT", "lastLogin BIGINT"};
			addTableConstruct("chat_Players", fields);
			fields = new String[]{"id INTEGER NOT NULL PRIMARY KEY UNIQUE", "name VARCHAR(100)"};
			addTableConstruct("chat_Servers", fields);
			fields = new String[]{"playerName VARCHAR(100)", "serverID INTEGER", "CONSTRAINT fk_playerName_Server FOREIGN KEY (playerName) REFERENCES chat_Players(player)","CONSTRAINT fk_serverID FOREIGN KEY (serverID) REFERENCES chat_Servers(id)"};
			addTableConstruct("chat_PlayerServers", fields);

			//chatChannels and the foreign key table
			fields = new String[]{"id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE", "name VARCHAR(100) UNIQUE"};
			addTableConstruct("chat_Channels", fields);
			fields = new String[]{"playerName VARCHAR(100)", "channelID INTEGER", "CONSTRAINT fk_playerName_Channel FOREIGN KEY (playerName) REFERENCES chat_Players(player)","CONSTRAINT fk_channelID FOREIGN KEY (channelID) REFERENCES chat_Channels(id)"};
			addTableConstruct("chat_PlayerChannels", fields);
			
			//Muted players and the foreign key table
			fields = new String[]{"muteePlayer VARCHAR(100)", "mutedPlayer VARCHAR(100) ", "CONSTRAINT fk_playerName_Mute FOREIGN KEY (muteePlayer) REFERENCES chat_Players(player)"};
			addTableConstruct("chat_MutedPlayers", fields);
			
			//Player modes and the foreign key table
			fields = new String[]{"playerName VARCHAR(100)", "modeName VARCHAR(100)", "modeStatus BOOL", "CONSTRAINT fk_playerName_Mode FOREIGN KEY (playerName) REFERENCES chat_Players(player)"};
			addTableConstruct("chat_Modes", fields);

			
			
			// build our tables
			ensureTablesExist();
			

		}
		catch(Exception e) {
			//Cannot connect
			
//			// disconnect on error
			e.printStackTrace();
			ConsoleLogging.severe("Failed to initialize database connection! Using url " + "jdbc:sqlite:" + url );
			ConsoleLogging.warning("You won't be able to use any commands that utilize the database!");
			disconnect();
		}

		//Very hacky way to see if the table has been updated to include last login
		try
		{
			ResultSet results = DatabaseManager.accessQuery(
					"select lastLogin from chat_Players;");
			
		}
		catch(DatabaseException e)
		{
			//Catching an exception means that the field doesn't exist, need to add it
			try {
				DatabaseManager.updateQuery("ALTER TABLE chat_Players ADD COLUMN lastLogin BIGINT");
				//Lets load some timestamps shall we
				String playerFolder = MCNSAChat.plugin.getConfig().getString("database-player-folder");
				File folder = new File(playerFolder);
				if (!folder.isDirectory() || !folder.exists())
					ConsoleLogging.severe("Player folder is not a folder");
				for (File file : folder.listFiles())
				{
					String username = file.getName().substring(0, file.getName().length()-4);
					DatabaseManager.updateQuery("UPDATE chat_players SET lastlogin=? WHERE player=?", file.lastModified(), username);
				}
			} catch (DatabaseException e1) {
				ConsoleLogging.severe("Could not add new column lastLogin");
			}
		}
		
	}
	
	public static void disconnect()
	{
		try {
			if(resultSet != null) {
				resultSet.close();
			}
			
			if(preparedStatement != null) {
				preparedStatement.close();
			}
			
			if(connection != null) {
				connection.close();
			}
		}
		catch(SQLException e) {
			e.printStackTrace();
			ConsoleLogging.severe("Failed to terminate database connection! " + e.getMessage());
		}
		
	}

	public static void connect() throws SQLException, DatabaseException {
		connection = DriverManager.getConnection("jdbc:sqlite:" + url);
		
		if(connection != null) {
			ConsoleLogging.info("&aDatabase connected!");
		}
		else {
			throw new DatabaseException("Failed to retrieve database!");
		}
	}
	
	// table construction commands
	public static void addTableConstruct(DatabaseTableInfo tableInfo) {
		String query = String.format("CREATE TABLE IF NOT EXISTS %s ( id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT UNIQUE", tableInfo.name());
		for(String field: tableInfo.fields()) {
			query += ", " + field;
		}
		query += " );";
		tableConstructions.put(tableInfo.name(), query);
	}
	public static void addTableConstruct(String name, String[] fields) {
		String query = String.format("CREATE TABLE IF NOT EXISTS %s ( ", name);
		query+=fields[0];
		for(int i = 1; i < fields.length; i++) {
			query += ", " + fields[i];
		}
		query += " );";
		tableConstructions.put(name, query);
	}


	private void ensureTablesExist() throws SQLException {
		for(String table: tableConstructions.keySet()) {
			try {
				preparedStatement = connection.prepareStatement(tableConstructions.get(table));
				preparedStatement.executeUpdate();
			}
			catch(SQLException e) {
				ConsoleLogging.severe("Failed to ensure table construction for query " + tableConstructions.get(table) + "reason: "+ e.getMessage()+"! Skipping...");
				
			}
			finally {
				if (preparedStatement != null)
					preparedStatement.close();
			}
		}
	}
	
	public static Connection getConnection() {
		return connection;
	}
	private static PreparedStatement prepareStatement(String query, Object... args) throws SQLException, DatabaseException {
		// prepare our statement
		if (connection == null)
		{
			ConsoleLogging.severe("Have no connection to the DB\n");
			return null;
		}
		preparedStatement = connection.prepareStatement(query);
		
		// keep track of where in the statement to do stuff
		int i = 1;
		for(Object arg: args) {
			// now add to the prepared statement based on what data type we have
			if(arg.getClass().equals(String.class)) {
				preparedStatement.setString(i, (String)arg);
			}
			else if(arg.getClass().equals(int.class) || arg.getClass().equals(Integer.class)) {
				preparedStatement.setInt(i, (Integer)arg);
			}
			else if(arg.getClass().equals(boolean.class) || arg.getClass().equals(Boolean.class)) {
				preparedStatement.setBoolean(i, (Boolean)arg);
			}
			else if(arg.getClass().equals(float.class) || arg.getClass().equals(Float.class)) {
				preparedStatement.setFloat(i, (Float)arg);
			}
			else if(arg.getClass().equals(long.class) || arg.getClass().equals(Long.class)) {
				preparedStatement.setLong(i, (Long)arg);
			}
			else if(arg.getClass().equals(Date.class)) {
				preparedStatement.setDate(i, new java.sql.Date(((java.util.Date)arg).getTime()));
			}
			else if(arg.getClass().equals(Timestamp.class)) {
				preparedStatement.setTimestamp(i, (Timestamp)arg);
			}
			else {
				throw new DatabaseException("Unknown SQL data type: %s", arg.getClass().getSimpleName());
			}
			
			// increment our index
			i++;
		}
		
		return preparedStatement;
	}
	
	public static ResultSet accessQuery(String query, Object... args) throws DatabaseException {
		try {
			return accessQuery(prepareStatement(query, args));
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new DatabaseException("Failed to prepare query: (%s)!", e.getMessage());
		}
		
	}
	
	public static ResultSet accessQuery(PreparedStatement preparedStatement) throws DatabaseException {
		try {
			// make sure we have a connection
			if(connection == null || connection.isClosed()) {
				throw new DatabaseException("Not connected to a database!");
			}
			
			// ok, now execute our query!
			ResultSet results = preparedStatement.executeQuery();
			
			return results;
			
			/*
			// get the result set meta data so we can access column names
			ResultSetMetaData metaData = results.getMetaData();
			
			// build our returned results
			ArrayList<HashMap<String, Object>> ret = new ArrayList<HashMap<String, Object>>();
			while(results.next()) {
				HashMap<String, Object> row = new HashMap<String, Object>();
				for(int column = 1; column <= metaData.getColumnCount(); column++) {
					row.put(metaData.getColumnName(column), results.getObject(column));
				}
				ret.add(row);
			}
			
			return ret;*/
		}
		catch(Exception e) {
			//e.printStackTrace();
			throw new DatabaseException("Failed to prepare query: (%s)!", e.getMessage());
		}
	}
	
	public static int updateQuery(String query, Object... args) throws DatabaseException {
		try {
			return updateQuery(prepareStatement(query, args));
		}
		catch (Exception e) {
			//e.printStackTrace();
			throw new DatabaseException("Failed to prepare query: (%s)!", e.getMessage());
		}
		finally {
			try {
				preparedStatement.close();
			}
			catch(SQLException e) {
				//e.printStackTrace();
				ConsoleLogging.severe("Failed to close prepared statement on query: "+e.getMessage()+"!");
			}
		}
	}
	
	public static int updateQuery(PreparedStatement preparedStatement) throws DatabaseException {
		try {
			// make sure we have a connection
			if(connection == null || connection.isClosed()) {
				throw new DatabaseException("Not connected to a database!");
			}
			
			// ok, now execute our query!
			return preparedStatement.executeUpdate();
		}
		catch(Exception e) {
			//e.printStackTrace();
			throw new DatabaseException("Failed to prepare query: (%s)!", e.getMessage());
		}
		finally {
			try {
				preparedStatement.close();
			}
			catch(SQLException e) {
				//e.printStackTrace();
				ConsoleLogging.severe("Failed to close prepared statement on query: "+e.getMessage()+"!");
			}
		}
	}

}
