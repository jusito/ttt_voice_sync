package com.github.jusito.teamspeak.ttt_voice_sync;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.koraktor.steamcondenser.exceptions.SteamCondenserException;
import com.github.koraktor.steamcondenser.steam.servers.SourceServer;

public class TTTServer extends SourceServer implements AutoCloseable {
	
	private static final Pattern pLiveState = Pattern.compile("^(.+)	(true|false)$");
	
	private static class Player {
		private boolean alive;

		public final String name;
		
		public Player(final String name, final boolean alive) {
			this.name = name;
			this.alive = alive;
		}
		
		public static Player get(final List<Player> players, final String name) {
			Player ret = null;

			for (Player current : players) {
				if (current.name.equals(name)) {
					ret = current;
				}
			}

			return ret;
		}

		public static void purge(final List<Player> players, final List<Player> playing) {
			for (int i = 0; i < players.size(); i++) {
				if (!playing.contains(players.get(i))) {
					System.out.println("[info] player left server " + players.remove(i).name);
					i = i - 1;
				}
			}
		}
		
		public void update(TS3VoiceServer voiceServer, boolean alive) throws InterruptedException {
			if (this.alive != alive) {
				this.alive = alive;
				
				// logging
				System.out.println("[info] " + this.toString());
				
				// if is now alive
				if (this.alive) {
					voiceServer.setPlayerAlive(name);

					// player is dead
				} else {
					voiceServer.setPlayerDead(name);
				}
			}
		}
		
		public String toString() {
			return name + " is now " + (alive ? "alive" : "dead");
		}
	}
	
	private ArrayList<Player> players = new ArrayList<Player>();
	private ArrayList<Player> lastRefresh = new ArrayList<Player>();

	public TTTServer(Config config) throws SteamCondenserException, TimeoutException {
		super(config.TTTHost, config.TTTPort);
		
		if (!rconAuth(config.TTTRconPassword)) {
			throw new IllegalStateException("rcon not successful");
		}
	}
	
	public void syncCurrentState(final TS3VoiceServer voiceServer) throws TimeoutException, SteamCondenserException, InterruptedException {
		
		// get gamemode
		String ret = rconExec("lua_run print( GAMEMODE.round_state )"); // 2 = PreGame First, 3 in game, 4 after game
		if (ret == null || !ret.contains("3")) { // TODO pregame dead is ok!
			voiceServer.resetAllClients();
		}
		
		// retrieve state GetAll vs GetHumans
		ret = rconExec("lua_run for k,v in pairs(player.GetAll()) do print( v:GetName(), v:Alive() ) end");
		
		// split lines
		String[] lines = ret.split("\n");

		// clear players
		lastRefresh.clear();

		// for each line
		for (int i = 0; i < lines.length; i++) {
			// first is just the command we send
			// others have format "name alive"
			if (i > 0) {
				Matcher m = pLiveState.matcher(lines[i]);

				// if we found player and live state
				if (m.find()) {
					Player current = Player.get(players, m.group(1));
					boolean currentIsAlive = Boolean.parseBoolean(m.group(2));
					// if we did not watch the player right now
					if (current == null) {
						current = new Player(m.group(1), currentIsAlive );
						players.add(current);
						
						// logging
						System.out.println("[info] added new player, " + current.toString());
					}

					// add player to current
					lastRefresh.add(current);
					// update player
					current.update(voiceServer, currentIsAlive);
				} else {
					System.out.println("[error] couldnt get playername, but it should be in.");
				}
			}
		}

		// purge left player
		Player.purge(players, lastRefresh);
	}

	@Override
	public void close() throws Exception {
		super.disconnect();
	}

}
