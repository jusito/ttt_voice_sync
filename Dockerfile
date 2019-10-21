FROM maven:3.6.2-jdk-8

COPY . /home/

RUN cd /home/ && \
	mvn clean package
	
FROM openjdk:8-jre-alpine

COPY --from=0 "/home/target/ttt_voice_sync-*-jar-with-dependencies.jar" "/home/ttt_voice_sync.jar"

WORKDIR "/home/"

ENV TTTHost="" \
	TTTPort="27015" \
	TTTRconPassword="" \
	RefreshTimeMS=250 \
	VoiceHost="" \
	VoiceQueryPort=10011 \
	VoiceServerID=1 \
	VoiceChannelName="" \
	VoiceTargetGroupName="dead" \
	VoiceDefaultGroupName="alive" \
	VoiceBotName="VoiceSync" \
	VoiceLoginName="serveradmin" \
	VoiceLoginPassword=""

ENTRYPOINT ["java", "-jar", "ttt_voice_sync.jar" ]
CMD [ "env" ]