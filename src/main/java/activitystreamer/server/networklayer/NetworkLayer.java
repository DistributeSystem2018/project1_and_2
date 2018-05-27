package activitystreamer.server.networklayer;

import activitystreamer.message.MessageGenerator;
import activitystreamer.message.MessageHandler;
import activitystreamer.message.MessageType;
import activitystreamer.message.networklayerhandlers.ServerBackupListHandler;
import activitystreamer.message.networklayerhandlers.ServerInvalidHandler;
import activitystreamer.util.Settings;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * NetworkLayer
 * <p>
 * Author Ning Kang
 * Date 18/5/18
 */

public class NetworkLayer extends Thread implements IMessageConsumer {
	public static final Logger log = LogManager.getLogger("serverLogger");
	private static NetworkLayer networkLayer = new NetworkLayer();
	private Listener listener;
	private ArrayList<Connection> connectionList;
	private boolean term;
	private HashMap<String, IMessageConsumer> consumerMap;
	private HashMap<MessageType, MessageHandler> handlerMap;

	private NetworkLayer() {
		connectionList = new ArrayList<>();
		term = false;
		consumerMap = new HashMap<>();
		handlerMap = new HashMap<>();
		initialHandlers();

	}

	public static NetworkLayer getInstance() {
			return networkLayer;

	}

	public Connection connectToServer(String remoteServer, int remotePort) {
		try {
			Socket s = new Socket(remoteServer, remotePort);
			Connection c = outgoingConnection(s);
			c.setServer(true);
			c.setAuthed(false);
			return c;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void initialHandlers() {
		this.handlerMap.put(MessageType.BACKUP_LIST, new ServerBackupListHandler());
		this.handlerMap.put(MessageType.INVALID_MESSAGE, new ServerInvalidHandler());
		registerConsumer(handlerMap.keySet(), this);
	}


	@Override
	public synchronized boolean process(Connection con, JsonObject json) {
		boolean isSucc = false;
		try {
			MessageType m = MessageType.valueOf(json.get("command").getAsString());
			MessageHandler h = handlerMap.get(m);
			if (h != null) {
				isSucc = h.processMessage(json, con);
			} else {
				log.error("Cannot find message handler for message type [{}]", m.name());
			}
		} catch (IllegalStateException e) {
			String info = String.format("Invalid message [%s] in ", json.toString());
			log.error(info);
			isSucc = false;
			String invalidMsg = MessageGenerator.invalid(info);
			con.writeMsg(invalidMsg);
		}
		return isSucc;
	}

	public ArrayList<Connection> getConnectionList() {
		return connectionList;
	}

	public void startListener() {
		// start a listener
		try {
			if (listener == null) {
				listener = new Listener();
			}
		} catch (IOException e1) {
			log.fatal("failed to startup a listening thread: " + e1);
			System.exit(-1);
		}
	}

	public void initiateConnection() {
		String remoteHost = Settings.getRemoteHostname();
		int remotePort = Settings.getRemotePort();
		Connection c = connectToServer(remoteHost, remotePort);
		if (c != null) {
			c.sendAuthMsg(Settings.getSecret());
		} else {
			log.error("Cannot connect to remote server {}:{}", remoteHost, remotePort);
			System.exit(-1);
		}
	}

	public IMessageConsumer getConsumer(String typeStr) {
		return consumerMap.get(typeStr);
	}

	public void registerConsumer(Set<MessageType> messageTypes, IMessageConsumer consumer) {
		for (MessageType messageType : messageTypes) {
			consumerMap.put(messageType.name(), consumer);
		}
	}

	/*
	 * The connection has been closed by the other party.
	 */
	public synchronized void connectionClosed(Connection con) {
		if (!term) {
			log.info("Remove connection {} from list", con.getSocket().getRemoteSocketAddress());
			connectionList.remove(con);
		}
	}

	/*
	 * A new incoming connection has been established, and a reference is returned to it
	 */
	public synchronized Connection incomingConnection(Socket s) throws IOException {
		log.debug("incomming connection: " + Settings.socketAddress(s));
		Connection c = new Connection(s);
		connectionList.add(c);
		return c;
	}

	/*
	 * A new outgoing connection has been established, and a reference is returned to it
	 */
	public synchronized Connection outgoingConnection(Socket s) throws IOException {
		log.debug("outgoing connection: " + Settings.socketAddress(s));
		Connection c = new Connection(s);
		connectionList.add(c);
		return c;

	}

	public synchronized void broadcastToServers(String msg, Connection from) {
		for (Connection c : connectionList) {
			if (c != from && c.isAuthedServer()) {
				c.writeMsg(msg);
			}
		}
	}

	public synchronized void broadcastToAll(String msg, Connection from) {
		for (Connection c : connectionList) {
			if ((c.isAuthedServer() || c.isAuthedClient()) && c != from) {
				c.writeMsg(msg);
			}
		}
	}

	public void sendMsgToAnonymous(String activity){
		ArrayList<Connection> connections = NetworkLayer.getInstance().getConnectionList();
		for(Connection c:connections){
			if(c.isAuthedClient() && c.getUser().getUsername().toLowerCase().equals("anonymous")){
				c.sendActivityBroadcastMsg(activity);
			}
		}
	}

	@Override
	public void run() {
		log.info("using announce interval of " + Settings.getAnnounceInterval() + " milliseconds");
		while (!term) {
			try {
				sendBackupList();
				Thread.sleep(Settings.getAnnounceInterval());
			} catch (InterruptedException e) {
				log.info("received an interrupt, system is shutting down");
				break;
			}
		}
		networkLayer.closeAllConnections();
	}

	/* normal quit */
	public synchronized void closeAllConnections() {
		log.info("closing " + connectionList.size() + " connections");
		// clean up
		for (Connection connection : connectionList) {
			connection.closeCon();
		}
		listener.setTerm(true);
	}

	/* Force quit */
	public synchronized void terminate() {
		log.info("Terminate " + connectionList.size() + " connections");
		// clean up
		for (Connection connection : connectionList) {
			connection.interrupt();
		}
		listener.interrupt();
		term = true;
		this.interrupt();
	}

	public void setTerm(boolean term){
		this.term = term;
		if(term){
			closeAllConnections();
		}
	}

	/* High avaliable for project 2 */
	private void sendBackupList() {
		String msg = MessageGenerator.backupList(networkLayer.getConnectionList());
		networkLayer.broadcastToServers(msg, null);
	}



	public int getClientLoads() {
		int load = 0;
		for (Connection c : connectionList) {
			if (c.isAuthedClient()) {
				load++;
			}
		}
		return load;
	}


	public int getServerLoads(Connection exclude) {
		int load = 0;
		for (Connection c : connectionList) {
			if (exclude != c && c.isAuthedServer()) {
				load++;
			}
		}
		return load;
	}


}
