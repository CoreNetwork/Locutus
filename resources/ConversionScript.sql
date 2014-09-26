
ALTER TABLE chat_Players RENAME TO chat_Players_temp;
ALTER TABLE chat_Servers RENAME TO chat_Servers_temp;
ALTER TABLE chat_PlayerServers RENAME TO chat_PlayerServers_temp;
ALTER TABLE chat_Channels RENAME TO chat_Channels_temp;
ALTER TABLE chat_PlayerChannels RENAME TO chat_PlayerChannels_temp;
ALTER TABLE chat_MutedPlayers RENAME TO chat_MutedPlayers_temp;
ALTER TABLE chat_Modes RENAME TO chat_Modes_temp;

CREATE TABLE transfer(
	UUID CHAR(36),
	name VARCHAR(100)
);
CREATE TABLE chat_Players(
	UUID CHAR(36) NOT NULL PRIMARY KEY UNIQUE,
	player VARCHAR(100),
	channel VARCHAR(100),
	lastPM CHAR(36),
	timeoutTill BIGINT,
	lastLogin BIGINT,
	CONSTRAINT fk_lastPM FOREIGN KEY (lastPM) REFERENCES chat_Players(UUID)
);

CREATE TABLE chat_Servers(
	name VARCHAR(100) NOT NULL,
	id INTEGER
);

CREATE TABLE chat_PlayerServers(
	playerUUID CHAR(36) NOT NULL,
	serverID INTEGER,
	CONSTRAINT fk_playerUUID_Server FOREIGN KEY (playerUUID) REFERENCES chat_Players(UUID),
	CONSTRAINT fk_serverID FOREIGN KEY (serverID) REFERENCES chat_Servers(id)
);
CREATE TABLE chat_Channels(
	id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,
	name VARCHAR(100) UNIQUE
);

CREATE TABLE chat_PlayerChannels(
	playerUUID CHAR(36),
	channelID INTEGER,
	CONSTRAINT fk_playerUUID_Channel FOREIGN KEY (playerUUID) REFERENCES chat_Players(UUID),
	CONSTRAINT fk_channelID FOREIGN KEY (channelID) REFERENCES chat_Channels(id)
);

CREATE TABLE chat_MutedPlayers(
	mutedPlayer CHAR(36),
	muteePlayer CHAR(36),
	CONSTRAINT fk_mutedID_Mute FOREIGN KEY (mutedPlayer) REFERENCES chat_Players(UUID),
	CONSTRAINT fk_muteeID_Mute FOREIGN KEY (muteePlayer) REFERENCES chat_Players(UUID)
);

CREATE TABLE chat_Modes(
	playerUUID CHAR(36),
	modeName VARCHAR(100),
	modeStatus BOOL,
	CONSTRAINT fk_playerUUID_Mode FOREIGN KEY (playerUUID) REFERENCES chat_Players(UUID)
);

INSERT INTO transfer (UUID, name) VALUES ('3ddeae2e-0124-4f0f-bb8c-bedaa52ed0df', 'Geoff95');
INSERT INTO transfer (UUID, name) VALUES ('0187c4ea-04ad-4ce2-b624-9fa356fd8e33', 'Waterrat95');
INSERT INTO chat_Players (player, channel, timeoutTill, lastLogin, UUID) SELECT old.player, old.channel, old.timeoutTill, old.lastLogin, t.UUID FROM chat_Players_temp old, transfer t WHERE old.player=t.name;
UPDATE chat_Players SET lastPM = (SELECT t.uuid FROM transfer t, chat_Players_temp old, chat_Players new WHERE old.lastPM=t.name AND old.player = new.player);

INSERT INTO chat_Servers (id,name) SELECT id,name FROM chat_Servers_temp;
INSERT INTO chat_PlayerServers(playerUUID, serverID) SELECT t.UUID, old.serverID FROM chat_PlayerServers_temp old, transfer t WHERE old.playerName=t.name;

INSERT INTO chat_Channels SELECT * FROM chat_Channels_temp;

INSERT INTO chat_PlayerChannels (channelID, playerUUID) SELECT old.channelID, t.UUID FROM chat_PlayerChannels_temp old, transfer t WHERE old.playerName=t.name;

INSERT INTO chat_Modes (playerUUID, modeName, modeStatus) SELECT t.UUID, old.modeName, old.modeStatus FROM chat_Modes_temp old, transfer t WHERE old.playerName=t.name;


# This does not work. If loss of data, this is just which players have 'ignored' other players. Relatively safe loss.
#INSERT INTO chat_MutedPlayers (mutedPlayer) SELECT t.UUID FROM chat_MutedPlayers_temp old, transfer t WHERE old.mutedPlayer=t.name;
#UPDATE chat_MutedPlayers SET muteePlayer = (SELECT t.uuid FROM transfer t, chat_MutedPlayers_temp old, chat_MutedPlayers new WHERE new.muteePlayer=t.name AND new.muteePlayer=old.muteePlayer);

DROP TABLE chat_Players_temp;
DROP TABLE chat_Servers_temp;
DROP TABLE chat_PlayerServers_temp;
DROP TABLE chat_Channels_temp;
DROP TABLE chat_PlayerChannels_temp;
DROP TABLE chat_MutedPlayers_temp;
DROP TABLE chat_Modes_temp;