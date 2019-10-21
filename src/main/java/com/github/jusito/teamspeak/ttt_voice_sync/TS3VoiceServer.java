package com.github.jusito.teamspeak.ttt_voice_sync;

import java.util.List;

import com.github.theholywaffle.teamspeak3.TS3ApiAsync;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.CommandFuture;
import com.github.theholywaffle.teamspeak3.api.TextMessageTargetMode;
import com.github.theholywaffle.teamspeak3.api.event.ChannelCreateEvent;
import com.github.theholywaffle.teamspeak3.api.event.ChannelDeletedEvent;
import com.github.theholywaffle.teamspeak3.api.event.ChannelDescriptionEditedEvent;
import com.github.theholywaffle.teamspeak3.api.event.ChannelEditedEvent;
import com.github.theholywaffle.teamspeak3.api.event.ChannelMovedEvent;
import com.github.theholywaffle.teamspeak3.api.event.ChannelPasswordChangedEvent;
import com.github.theholywaffle.teamspeak3.api.event.ClientJoinEvent;
import com.github.theholywaffle.teamspeak3.api.event.ClientLeaveEvent;
import com.github.theholywaffle.teamspeak3.api.event.ClientMovedEvent;
import com.github.theholywaffle.teamspeak3.api.event.PrivilegeKeyUsedEvent;
import com.github.theholywaffle.teamspeak3.api.event.ServerEditedEvent;
import com.github.theholywaffle.teamspeak3.api.event.TS3EventType;
import com.github.theholywaffle.teamspeak3.api.event.TS3Listener;
import com.github.theholywaffle.teamspeak3.api.event.TextMessageEvent;
import com.github.theholywaffle.teamspeak3.api.wrapper.Channel;
import com.github.theholywaffle.teamspeak3.api.wrapper.ChannelGroup;
import com.github.theholywaffle.teamspeak3.api.wrapper.ChannelGroupClient;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;


public class TS3VoiceServer implements AutoCloseable {
	@SuppressWarnings("unchecked")
	private static int getChannelGroupID(final TS3ApiAsync api, final String groupName) throws InterruptedException {
		int cgid = -1;

		for (ChannelGroup cg : (List<ChannelGroup>)Sync(api.getChannelGroups())) {
			if (cg.getName().equals(groupName)) {
				cgid = cg.getId();
				break;
			}
		}

		return cgid;
	}
	
	@SuppressWarnings("unchecked")
	private static int getChannelID(final TS3ApiAsync api, final String channelName) throws InterruptedException {
		int cid = -1;
		for (Channel c : (List<Channel>)Sync(api.getChannels())) {
			if (c.getName().equals(channelName)) {
				cid = c.getId();
				break;
			}
		}

		return cid;
	}

//	private static List<Client> getClients(final TS3Api api, final int cid, final int cgid) {
//		final List<Integer> ClientDatabaseIDs = new ArrayList<Integer>();
//
//		for (ChannelGroupClient c : api.getChannelGroupClients(cid, -1, cgid)) {
//			ClientDatabaseIDs.add(c.getClientDatabaseId());
//		}
//
//		return getClients(api, ClientDatabaseIDs);
//	}
//
//	private static List<Client> getClients(final TS3Api api, final List<Integer> ClientDatabaseIDs) {
//		List<Client> clients = new ArrayList<Client>();
//
//		for (Client c : api.getClients()) {
//			if (ClientDatabaseIDs.contains(c.getDatabaseId())) {
//				clients.add(c);
//			}
//		}
//
//		return clients;
//	}
//
//	private static List<Client> getClients(final TS3ApiAsync api, final int cid) throws InterruptedException {
//		final List<Client> clients = new ArrayList<Client>();
//
//		for (Client c : (List<Client>)Sync(api.getClients())) {
//			// if is in channel and is normal user
//			if (c.getChannelId() == cid && c.getType() == 0) {
//				clients.add(c);
//			}
//		}
//
//		return clients;
//	}

	private static Object Sync(final CommandFuture<?> cmd) throws InterruptedException {
		cmd.await();
		return cmd.get();
	}
	
	private final TS3ApiAsync api;
	private final int cid;
	private final TS3Config config;
	
	private final int defaultClientGroupID;
	private final int mutedClientGroupID;
	private final TS3Query query;

	public TS3VoiceServer(Config config) throws InterruptedException {
		this.config = new TS3Config();
		this.config.setHost(config.VoiceHost);
		this.config.setQueryPort(config.VoiceQueryPort);

		query = new TS3Query(this.config);
		query.connect();

		api = query.getAsyncApi();
		api.login(config.VoiceLoginName, config.VoiceLoginPassword);

		api.selectVirtualServerById(config.VoiceServerID);
		api.setNickname(config.VoiceBotName);
		
		cid = getChannelID(api, config.VoiceChannelName);
		mutedClientGroupID = getChannelGroupID(api, config.VoiceTargetGroupName);
		defaultClientGroupID = getChannelGroupID(api, config.VoiceDefaultGroupName);

		if (cid == -1 || mutedClientGroupID == -1) {
			throw new IllegalStateException("failed to initialize");
		}

		api.moveQuery(cid);
		
		api.registerEvent(TS3EventType.TEXT_CHANNEL, cid);
		api.addTS3Listeners(new TS3Listener() {
			
			@Override
			public void onTextMessage(TextMessageEvent msg) {
				if (msg.getMessage().equals("!reset")) {
					try {
						resetAllClients();
						api.sendTextMessage(TextMessageTargetMode.CHANNEL, cid, "Processed reset, all should be alive");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
			@Override
			public void onServerEdit(ServerEditedEvent arg0) {}
			
			@Override
			public void onPrivilegeKeyUsed(PrivilegeKeyUsedEvent arg0) {}
			
			@Override
			public void onClientMoved(ClientMovedEvent arg0) {}
			
			@Override
			public void onClientLeave(ClientLeaveEvent arg0) {}
			
			@Override
			public void onClientJoin(ClientJoinEvent arg0) {}
			
			@Override
			public void onChannelPasswordChanged(ChannelPasswordChangedEvent arg0) {}
			
			@Override
			public void onChannelMoved(ChannelMovedEvent arg0) {}
			
			@Override
			public void onChannelEdit(ChannelEditedEvent arg0) {}
			
			@Override
			public void onChannelDescriptionChanged(ChannelDescriptionEditedEvent arg0) {}
			
			@Override
			public void onChannelDeleted(ChannelDeletedEvent arg0) {}
			
			@Override
			public void onChannelCreate(ChannelCreateEvent arg0) {}
		});
		
		resetAllClients();
		sendChannelMessage(
				"Hello! Iam online, to mute you if you are dead please set your Nickname the same as ingame.");
	}
	
	@Override
	public void close() throws Exception {
		resetAllClients();
		sendChannelMessage("See you soon!");

		api.logout();
	}

	@SuppressWarnings("unchecked")
	public void resetAllClients() throws InterruptedException {
		for (ChannelGroupClient c : (List<ChannelGroupClient>)Sync(api.getChannelGroupClients(cid, -1, mutedClientGroupID))) {
			api.setClientChannelGroup(defaultClientGroupID, cid, c.getClientDatabaseId());
		}
	}

	private void sendChannelMessage(final String msg) {
		api.sendChannelMessage(msg);
	}

	public boolean setPlayerAlive(final String name) throws InterruptedException {
		Client c = (Client)Sync(api.getClientByNameExact(name, true));
		if (c != null) {
			api.setClientChannelGroup(defaultClientGroupID, cid, c.getDatabaseId());
			return true;
		} else {
			return false;
		}
	}

	public boolean setPlayerDead(final String name) throws InterruptedException {
		Client c = (Client)Sync(api.getClientByNameExact(name, true));
		if (c != null) {
			api.setClientChannelGroup(mutedClientGroupID, cid, c.getDatabaseId());
			return true;
		} else {
			return false;
		}
	}
}
