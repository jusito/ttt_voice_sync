# ttt\_voice\_sync
Synchronizing your voice state between TTT server and TS3.

# What is this?
If you play TTT and using TS3, you can use this tool to sync death state to TS3. If set up, the players only need to set their name on TS same as ingame. No longer someone screams in TS after dead.

# How to get it working?
## Prepare your teamspeak server
1. Create two copies of channelgroup Guest, name it alive & dead.
2. Change permission of channel group dead `Client - talk power to speak, value 0` & `Client - channel group rights skip, value [ ](false/empty)`
3. Create a moderated channel with needed talk power of 1
4. Because unknown players are still default channelgroup Guest, give the default channel group Guest talk power of at least 1
5. Join the channel and check if you can speak with Guest / alive and can't speak with channel group dead.

## How to use in docker / java

### Example 1: use environment variables in docker
```
docker run -d \
-e TTTHost="1.2.3.4" -e TTTRconPassword="MySecurePW" \
-e VoiceHost="2.3.4.5" \
-e VoiceChannelName="MyModeratedGameChannel" \
-e VoiceLoginName="VoiceSync" -e VoiceLoginPassword="AnotherSecurePW" \
jusito/ttt_voice_sync:latest
```

### Example 2: use environment variables with plain jar
```
java -jar ttt_voice_sync.jar env
```

### Example 3: docker with each argument explicit
```
docker run -d \
jusito/ttt_voice_sync:latest \
"TTTHost" "TTTPort" "TTTRconPassword" \
"RefreshTimeMS" \
"VoiceHost" "VoiceQueryPort" "VoiceServerID" "VoiceChannelName" \
"VoiceTargetGroupName" "VoiceDefaultGroupName" \
"VoiceBotName" "VoiceLoginName" "VoiceLoginPassword"
```

### Example 4: java with each argument explicit
```
java -jar ttt_voice_sync.jar \
"TTTHost" "TTTPort" "TTTRconPassword" \
"RefreshTimeMS" \
"VoiceHost" "VoiceQueryPort" "VoiceServerID" "VoiceChannelName" \
"VoiceTargetGroupName" "VoiceDefaultGroupName" \
"VoiceBotName" "VoiceLoginName" "VoiceLoginPassword"
```

## Environment variables

|Variable|Default|Description|
|--------|-------|-----------|
|TTTHost||IP to your TTT server|
|TTTPort|27015|TCP Port for RCON, most of the time game port|
|TTTRconPassword||password to your RCON|
|RefreshTimeMS|250|Time between two syncs (if to low crashes may occur)|
|VoiceHost||IP to your TS3 server|
|VoiceQueryPort|10011|Port to serverquery|
|VoiceServerID|1|Virtual server id|
|VoiceChannelName||Name of the channel to join|
|VoiceTargetGroupName|dead|Channelgroup for dead state|
|VoiceDefaultGroupName|alive|Channelgroup for alive state(careful with Guest if you have multiple virtual server)|
|VoiceBotName|VoiceSync|Name for the tool on your TS server|
|VoiceLoginName|serveradmin|server query login name|
|VoiceLoginPassword||server query login password|

## How to use ingame
1. Set your name in Teamspeak the same as ingame, you are done.
2. If the round is over, but still someone is dead, write in the teamspeak channel chat `!reset` if the tool is ok, it will set all alive and write in the channel.

# Questions?
Write me on github. Btw I have no clue why this multi stage build is that big, will be next.

# Credits
* (koraktor github.com)[https://github.com/koraktor/steam-condenser-java]
* (TheHolyWaffle github.com)[https://github.com/TheHolyWaffle/TeamSpeak-3-Java-API]
