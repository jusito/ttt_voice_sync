package com.github.jusito.teamspeak.ttt_voice_sync;

public class Config {
	public final String TTTHost;
	public final int TTTPort;
	public final String TTTRconPassword;
	public final int RefreshTimeMS;
	
	public final String VoiceHost;
	public final int VoiceQueryPort;
	public final int VoiceServerID;
	public final String VoiceChannelName;
	public final String VoiceTargetGroupName;
	public final String VoiceDefaultGroupName;
	
	public final String VoiceBotName;
	public final String VoiceLoginName;
	public final String VoiceLoginPassword;
	
	
	
	Config(final String[] args) {
		final boolean useEnvironmentVariables = args.length == 1 && args[0].equals("env");
		
		if (args == null || (args.length != 13 && !useEnvironmentVariables )) {
			throw new IllegalArgumentException("java -jar ttt_voice_sync.jar " +
					"TTTHost TTTPort TTTRconPassword " +
					"RefreshTimeMS " +
					"VoiceHost VoiceQueryPort VoiceServerID " +
					"VoiceChannelName VoiceTargetGroupName VoiceDefaultGroupName " +
					"VoiceBotName VoiceLoginName VoiceLoginPassword (given " + args.length + "/13)" +
					"\n or java -jar ttt_voice_sync.jar env (you need to provide equal named env variables)");
		}
		
		this.TTTHost = useEnvironmentVariables ? System.getenv("TTTHost") : args[0];
		this.TTTPort = Integer.parseInt(useEnvironmentVariables ? System.getenv("TTTPort") : args[1]);
		this.TTTRconPassword = useEnvironmentVariables ? System.getenv("TTTRconPassword") : args[2];
		this.RefreshTimeMS = Integer.parseInt(useEnvironmentVariables ? System.getenv("RefreshTimeMS") : args[3]);
		
		this.VoiceHost = useEnvironmentVariables ? System.getenv("VoiceHost") : args[4];
		this.VoiceQueryPort = Integer.parseInt(useEnvironmentVariables ? System.getenv("VoiceQueryPort") : args[5]);
		this.VoiceServerID = Integer.parseInt(useEnvironmentVariables ? System.getenv("VoiceServerID") : args[6]);
		this.VoiceChannelName = useEnvironmentVariables ? System.getenv("VoiceChannelName") : args[7];
		this.VoiceTargetGroupName = useEnvironmentVariables ? System.getenv("VoiceTargetGroupName") : args[8];
		this.VoiceDefaultGroupName = useEnvironmentVariables ? System.getenv("VoiceDefaultGroupName") : args[9];
		
		this.VoiceBotName = useEnvironmentVariables ? System.getenv("VoiceBotName") : args[10];
		this.VoiceLoginName = useEnvironmentVariables ? System.getenv("VoiceLoginName") : args[11];
		this.VoiceLoginPassword = useEnvironmentVariables ? System.getenv("VoiceLoginPassword") : args[12];
	}
}
