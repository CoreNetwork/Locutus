MCNSAChat
=========

Cross-server, multi-channel chat plugin.
Built jars can be found at http://build.mcnsa.com/
# Commands

## Player commands

|**Command**|**Permission Node**|**Description**|
|------|:--------------|:----------|
|`/c <channel>`|`mcnsachat.player.move`|Switches to a channel.|
|`/mute <player>`||Ignore player’s chat and private messages.|
|`/clist`|`mcnsachat.player.list`|List available channels.|
|`/clisten <channel>`|`mcnsachat.player.listen`|Allows to listen to more channels, while typing in another.|
|`/csearch <player>`|`mcnsachat.player.search`|Views the channel a player is currently in.|
|`/list`||Lists everyone online. Aliases: `/who`, `/playerlist`, `/online`, `/players`|
|`/me <message>`|`mcnsachat.player.me`|Emotes your message (will appear as `* you <message>`). Example: `/me needs more diamonds`|
|`/msg <player> <message>`|`mcnsachat.player.msg`|Sends a private message to a player. Works across servers. Aliases: `/w`, `/tell`, `/whisper`|
|`/r <message>`|`mcnsachat.player.msg`|Replies to the last person who messaged you (or whom you messaged)|
|`/ranks`|`mcnsachat.player.ranks`|Lists server ranks|
    
## Moderator commands

|**Command**|**Permission Node**|**Description**|
|:------|:--------------|:----------|
|`/clock <player>`|`mcnsachat.admin.lock`|Locks a player in their channel, preventing them from switching.|
|`/cmove <player> <channel>`|`mcnsachat.admin.move`|Moves a player to a channel.|
|`/creconnect`|`mcnsachat.admin.reconnect`|Breaks the connection to the chat server, waiting for it to be restored later.|
|`/creload`|`mcnsachat.admin.reload`|Reloads configuration from file. Persistance isn’t reloaded (you need to reload the plugin or teh server).|
|`/cto <player> <time> <reason>`|`mcnsachat.admin.timeout`|Places player in timeout (they cannot chat or use `/msg`). Time is just a number (minutes).|
|`/cto <player>`|`mcnsachat.admin.timeout`|Pull out a player from timeout.|
|`/cto`|`mcnsachat.admin.timeout`|View all players currently in timeout.|
|`/crefresh`|`mcnsachat.admin.refresh`|Manually refresh the player names in the tab list|
|`/seeall`|`mcnsachat.admin.seeall`|Listen to all channels|
|`/ckick <player> <channel`|`mcnsachat.admin.remove`|Stop a player from listening to <channel>|

##Channel management

|**Command**|**Permission Node**|**Description**|
|:------|:--------------|:----------|
|`/calias <channel> <alias>`|`mcnsachat.admin.alias`|Changes channel’s alias. Alias is a short command that can be used to send directly to that channel (example: `/m <message>`) or make the channel your primary channel to write in (example: `/m`).|
|`/ccolor <channel> <color>`|`mcnsachat.admin.color`|Changes channel name’s color. [List of color codes](http://www.minecraftwiki.net/wiki/Color_Codes). Example: `/ccolor mod b`.|
|`/cmode <channel> <+/-> <mode>`|`mcnsachat.admin.mode`|Changes channel’s mode. Modes: `LOCAL` (makes channel work only on the current server), `MUTE` (nobody can talk), `RAVE` (random colors for letters, useful to stop huge fights), `RANDOM` (prepends everything with `&k` to make the text obfuscated), `LOUD` (enables bold and changes text to caps), `BORING` (removes all color and formatting), `PERSIST` (makes the channel show up in `/clist` even if it’s empty)|
|`/cname <channel> <name>`|`mcnsachat.admin.name`|Changes channel’s display name. Can be used to properly capitalize letters. Example: `/cname mod MOD`|
|`/cperm <channel> <read/write> <set/del> <permission>`|`mcnsachat.admin.perm`|Change the read and write permission `set <permission>` to set it `del ""` to remove it. (currently has to have a permission argument to remove it|
    
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
|'multiServer'|'false'|Check whether the plugin should attempt to connect to a chatserver|
|`chatServer`||Specify the chatserver address|
|`ChatServerPassword`||Specify the password for the chatserver|
|`consoleLogChat`|`true`|Whether to log the chat in the console as well as the chatlog file|
|`consoleLogServers`|`true`|Whether to log chat messages from other servers in console as well as the chatlog file|
|'consoleLogServerInfo'|'true'|Whether to display player joins and quits from another server|
|`hideJoinPlayerList`|`false`|Hide the list of players dsiplayed on join|

Channels.yml, Persistent channel configuration

# Permissions

|**Permission Node**|**Description**|
|:------|:----------|
|`mcnsachat.read`|Allows players to read chat.|
|`mcnsachat.write`|Allows players to chat.|
|`mcnsachat.read.<name>`|Allows players to chat in a special channel. Permission name is declared in `persistence.yml`, via `read_permission`.|
|`mcnsachat.write.<name>`|Allows players to chat in a special channel. Permission name is declared in `persistence.yml`, via `write_permission`.|
|`mcnsachat.forcelisten.<name>`|Force players to listen to a channel (example: useful for moderator channel, so mods don’t have to explicitly do `/clisten mod`). Permission name is declared in `persistence.yml`, via `read_permission`.|
|`mcnsachat.player.cancolor`|Allows to use color (ie: `&c`) and formatting (ie: `&o`) codes in chat. Cannot overwrite `/cmode <channel> BORING`.|
