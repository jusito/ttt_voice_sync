package com.github.jusito.teamspeak.ttt_voice_sync;

import java.util.concurrent.TimeoutException;

public class Main extends Thread implements AutoCloseable{
	
	private final Config config;
	private final TS3VoiceServer voiceServer;
	private final TTTServer tttServer;
	
	public volatile int errorCode;
	
	public Main(final Config config) throws Exception {
		errorCode = 1;
		this.config = config;
		this.voiceServer = new TS3VoiceServer(config);
		this.tttServer = new TTTServer(config);
		errorCode = 0;
	}
	
	public void run() {
		long timestamp = 0;
		int timeouts = 0;
		final int maxTimeouts = 3;
		
		try {
			while (!this.isInterrupted()) {
				Thread.sleep(config.RefreshTimeMS);
				
				try {
					tttServer.syncCurrentState(voiceServer);

				} catch (TimeoutException e) {
					
					if (System.currentTimeMillis() - timestamp > 10000) {
						timeouts = 0;
						System.out.println("[Info] possible mapchange");
						Thread.sleep(5000);
						timestamp = System.currentTimeMillis();

					} else if (timeouts++ == maxTimeouts) {
						System.out.println("[Timeout]");
						throw e;
					}
				}
			}
		} catch (InterruptedException e) {
			errorCode = 0;
		} catch (Throwable e2) {
			errorCode = 2;
		}
	}
	
	public static void main(String[] args) {
		java.util.logging.LogManager.getLogManager().reset();
		int exitcode = 10;
		try (Main me = new Main(new Config(args))) {
			me.start();
			me.join();
			exitcode = me.errorCode;
		} catch (InterruptedException e) {
			exitcode = 0;
		} catch (Throwable t) {
			t.printStackTrace();
			exitcode = 1;
		}
		
		System.out.println("Exit " + exitcode);
		System.exit(exitcode); 
	}

	@Override
	public void close() throws Exception {
		if (voiceServer != null) {
			voiceServer.close();
		}
		
		if (tttServer != null) {
			tttServer.disconnect();
		}
	}
}
