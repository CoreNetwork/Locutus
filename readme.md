MCNSAChat
=========

Cross-server, multi-channel chat plugin.

# Commands

## Player commands

|**Command**|**Permission Node**|**Description**|
|------|:--------------|:----------|
|`/c <channel>`|`mcnsachat.player.move`|Switches to a channel.|
|`/mute <player>`||Ignore player's chat and private messages.|
|`/clist`|`mcnsachat.player.list`|List available channels.|
|`/clisten <channel>`|`mcnsachat.player.listen`|Allows to listen to more channels, while typing in another.|
|`/list`||Lists everyone online. Aliases: `/who`, `/playerlist`, `/online`, `/players`|
|`/me <message>`|`mcnsachat.player.me`|Emotes your message (will appear as `* you <message>`). Example: `/me needs more diamonds`|
|`/msg <player> <message>`|`mcnsachat.player.msg`|Sends a private message to a player. Works across servers. Aliases: `/w`, `/tell`, `/whisper`|
|`/r <message>`|`mcnsachat.player.msg`|Replies to the last person who messaged you (or whom you messaged)|
    
## Moderator commands

Not Implemented yet

##Channel management

Not Implemented yet
    
##MCNSA Fun Commands

|**Command**|**Permission Node**|**Description**|
|:------|:--------------|:----------|
|`/dicks`|`mcnsachat.fun.dicks`|A message of love.|
|`/mab`|`mcnsachat.fun.mab`|A message of love.|
|`/pong`|`mcnsachat.fun.pong`|Alternative to `/ping`.|
|`/rand [<min>] <max>`|`mcnsachat.fun.rand`|Gives a random number according to `min` (default: `0`) and `max` (default: `20`).|

# Configuration
Config.yml, Main plugin configuration

|**Config**|**Default**|**Description**|
|:------|:--------------|:----------|
|`name`|`S`|Set the short name of the server. This is displayed in next to the channel name by default|
|`longname`|`Survival`|Set the long name for the server. Usually displayed when a player joins and quits|
|`defaultChannel`|`s`|Set the channel a player should be in on first join|
|`defaultListen`|`[S, Server, Global]`|Set the default list of channels a player should be listening to on first join|
|`multiServer`|'false'|Check whether the plugin should attempt to connect to a chatserver|
|`chatServer`||Specify the chatserver address|
|`ChatServerPassword`||Specify the password for the chatserver|
|`consoleLogChat`|`true`|Whether to log the chat in the console as well as the chatlog file|
|`consoleLogServers`|`true`|Whether to log chat messages from other servers in console as well as the chatlog file|
|`consoleLogServerInfo`|'true'|Whether to display player joins and quits from another server|
|`hideJoinPlayerList`|`false`|Hide the list of players dsiplayed on join|

Channels.yml, Persistent channel configuration

# Permissions

|**Permission Node**|**Description**|
|:------|:----------|
|`mcnsachat.read`|Allows players to read chat.|
|`mcnsachat.write`|Allows players to chat.|
|`mcnsachat.read.<name>`|Allows players to chat in a special channel. Permission name is declared in `persistence.yml`, via `read_permission`.|
|`mcnsachat.write.<name>`|Allows players to chat in a special channel. Permission name is declared in `persistence.yml`, via `write_permission`.|
|`mcnsachat.forcelisten.<name>`|Force players to listen to a channel (example: useful for moderator channel, so mods don�t have to explicitly do `/clisten mod`). Permission name is declared in `persistence.yml`, via `read_permission`.|
|`mcnsachat.player.cancolor`|Allows to use color (ie: `&c`) and formatting (ie: `&o`) codes in chat. Cannot overwrite `/cmode <channel> BORING`.|
