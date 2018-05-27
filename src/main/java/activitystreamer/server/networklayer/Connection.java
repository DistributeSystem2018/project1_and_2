package activitystreamer.server.networklayer;


import activitystreamer.message.MessageGenerator;
import activitystreamer.server.application.Control;
import activitystreamer.server.datalayer.DataLayer;
import activitystreamer.server.datalayer.ServerRow;
import activitystreamer.server.datalayer.UserRow;
import activitystreamer.util.Settings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;


public class Connection extends Thread {

	private BufferedReader inreader;
	private PrintWriter outwriter;
	private boolean open = false;
	private Socket socket;
	private boolean term = false;
	private boolean isAuthed = false;
	private UserRow user;

	private String remoteServerHost;
	private int remoteServerPort;
	private String remoteServerId;

	private ArrayList<BackupServerInfo> backupServers;

	private Connection(Socket socket, Boolean isServer) throws IOException {
		DataInputStream in = new DataInputStream(socket.getInputStream());
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		inreader = new BufferedReader(new InputStreamReader(in));
		outwriter = new PrintWriter(out, true);
		this.socket = socket;
		open = true;
		isAuthed = false;
		user = null;
		start();
	}

	Connection(Socket socket) throws IOException {
		this(socket, false);
	}


	/*
		 * returns true if the message was written, otherwise false
		 */
	public void writeMsg(String msg) {
		if (open) {
			outwriter.println(msg);
			outwriter.flush();
		}
	}

	public String connectionFrom() {
		if (isAuthed) {
			if (isAuthedServer()) {
				return this.remoteServerHost + this.remoteServerPort;
			} else {
				return this.user.getUsername();
			}
		}
		return "No_Authed_Connection";
	}

	public void closeCon() {
		if (open) {
			Control.log.info("closing connection " + Settings.socketAddress(socket));
			try {
				term = true;
				inreader.close();
				outwriter.close();
			} catch (IOException e) {
				Control.log.error("received exception closing the connection " + Settings.socketAddress(socket) + ": " + e);
			}
		}
	}


	public void run() {

		String data;
		while (!term) {
			try {
				while (!term && (data = inreader.readLine()) != null) {
					Control.log.debug("receive data {} from {}", data, connectionFrom());
					JsonParser parser = new JsonParser();
					JsonObject json = parser.parse(data).getAsJsonObject();
					String m = json.get("command").getAsString();
					IMessageConsumer h = NetworkLayer.getInstance().getConsumer(m);
					if (h != null) {
						h.process(this, json);
					} else {
						Control.log.error("No layer can conduct messaget type=[{}]", m);
					}
				}
				Control.log.debug("connection closed to " + Settings.socketAddress(socket));
			} catch (IOException e) {
				Control.log.error("connection " + Settings.socketAddress(socket) + " closed with exception: " + e);
			}

			try {
				doReconnection();

			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(-1);
			}

		}
		NetworkLayer.getInstance().connectionClosed(this);
		open = false;
	}

	private void doReconnection() throws InterruptedException {
		if (isAuthedServer() && !term) {
			Control.log.info("Disconnection is from an authened server, " +
					"reconnecting to backup server will be conducted in [{}] millsecond.", Settings.getTimeBeforeReconnect());
			sleep(Settings.getTimeBeforeReconnect());

			/* mark this server as offline */
			ServerRow deleteRow = DataLayer.getInstance().getServerById(remoteServerId);
			if (deleteRow != null) {
				deleteRow.setOnline(false);
				DataLayer.getInstance().updateServerTable(DataLayer.OperationType.UPDATE_OR_INSERT, deleteRow, true);
			}

			// Set this connect to false as the connection between servers is not authened
			isAuthed = false;
			remoteServerId = null;
			if (reConnectBackupServer()) {
				String info = String.format("Reconnect to backup server %s:%s, sending AUTHENTICATE message",
						Settings.getRemoteHostname(), Settings.getRemotePort());
				Control.log.info(info);
				sendAuthMsg(Settings.getSecret());
			}
		} else if (isAuthedClient() && !term) {
			DataLayer.getInstance().markUserOnline(getUser().getUsername(), false);
			term = true;
		} else {
			term = true;
		}
	}

	private boolean reConnectBackupServer() {
		if (backupServers != null && backupServers.size() > 0) {
			for (BackupServerInfo sInfo : backupServers) {
				if (!sInfo.getServerId().equals(Settings.getServerId())) {
					if (tryOneBackupServer(sInfo.getHost(), sInfo.getProt())) {
						Settings.setRemoteHostname(sInfo.getHost());
						Settings.setRemotePort(sInfo.getProt());
						return true;
					}
				} else {
					Control.log.info("The first available backup server is this server itself, no need do reconnection.Wait for other servers.");
					return false;
				}

			}
			Control.log.error("All backup servers are out of service, server terminates");
			this.closeCon();
			NetworkLayer.getInstance().connectionClosed(this);
			return false;
		}
		Control.log.info("no backup servers exist, server terminates");
		return false;
	}

	private boolean tryOneBackupServer(String host, int port) {
		Control.log.info("try to connect to backup server {}:{}", host, port);
		try {
			this.socket.close();
			Socket s = new Socket(host, port);
			DataInputStream in = new DataInputStream(s.getInputStream());
			DataOutputStream out = new DataOutputStream(s.getOutputStream());
			inreader = new BufferedReader(new InputStreamReader(in));
			outwriter = new PrintWriter(out, true);
			return true;
		} catch (IOException e) {
			Control.log.info("re-connect backup server {}:{} failed.", host, port);
			return false;
		}
	}

	public Socket getSocket() {
		return socket;
	}

//	public void setAuthed(boolean isAuthed,String host, int port) {
//		this.remoteServerHost = host;
//		this.remoteServerPort = port;
//		this.isAuthed = isAuthed;
//	}

	public void setAuthed(boolean isAuthed, String serverId, String host, int port) {
		this.remoteServerHost = host;
		this.remoteServerPort = port;
		this.remoteServerId = serverId;
		this.isAuthed = isAuthed;
	}

	public void setAuthed(boolean isAuthed) {
		this.isAuthed = isAuthed;
	}

	public void setRemoteServerId(String serverId) {
		this.remoteServerId = serverId;
	}

	public String getRemoteServerId() {
		return remoteServerId;
	}

	public String getRemoteServerHost() {
		return remoteServerHost;
	}

	public int getRemoteServerPort() {
		return remoteServerPort;
	}

	public UserRow getUser() {
		return user;
	}

	public void setUser(UserRow u) {
		this.user = u;
	}

	public void setServer(boolean isServer) {
		if (isServer) {
			this.user = null;
		}
	}
//
//	public boolean isMain() {
//		return isMain;
//	}
//
//	public void setMain(boolean main) {
//		isMain = main;
//	}

	public boolean isAuthedClient() {
		return user != null && isAuthed;
	}

	public boolean isAuthedServer() {
		return user == null && isAuthed;
	}

	public void sendInvalidMsg(String info) {
		Control.log.debug("send invalid message to server with info={}", info);
		String invalidStr = MessageGenerator.invalid(info);
		this.writeMsg(invalidStr);
	}

	public void sendLoginSuccMsg(String info) {
		Control.log.debug("send login succ message to client with info=[{}]", info);
		String loginSuccStr = MessageGenerator.loginSucc(info);
		this.writeMsg(loginSuccStr);
	}

	public void sendLoginFailedMsg(String info) {
		Control.log.debug("send login failed message to client with info=[{}]", info);
		String loginFailedStr = MessageGenerator.loginFail(info);
		this.writeMsg(loginFailedStr);
	}

	public void sendRegisterSuccMsg(String username) {
		String registerSucc = MessageGenerator.registerSucc(username);
		this.writeMsg(registerSucc);
	}

	public void sendRegisterFailedMsg(String username) {
		String registerFail = MessageGenerator.registerFail(username);
		this.writeMsg(registerFail);
	}

	public void sendAuthMsg(String secret) {
		String authenticate = MessageGenerator.authen(Settings.getSecret());
		this.writeMsg(authenticate);
	}

	public void sendAuthFailedMsg(String info) {
		String authFail = MessageGenerator.authFail(info);
		this.writeMsg(authFail);
	}

	public void sendAnnounceMsg(String id, int load, String host, int port) {
		String announce = MessageGenerator.serverAnnounce(id, load, host, port);
		this.writeMsg(announce);
	}

	public boolean sendActivityBroadcastMsg(String msg) {
//		String activityBroadcast = MessageGenerator.actBroadcast(act);
		this.writeMsg(msg);
		return true;
	}

//	public void sendActivityBroadcastMsg(Activity act) {
//		String activityBroadcast = MessageGenerator.actBroadcast(act);
//		this.writeMsg(activityBroadcast);
//	}

	// User register messages
	public void sendLockAllowedMsg(String username, String secret) {
		String message = MessageGenerator.lockAllowed(username, secret);
		this.writeMsg(message);
	}

	public void sendLockDeniedMsg(String username, String secret) {
		String message = MessageGenerator.lockDenied(username, secret);
		this.writeMsg(message);
	}

	public void sendRedirectMsg(String hostname, int port) {
		String message = MessageGenerator.redirect(hostname, port);
		this.writeMsg(message);
	}

	public void setBackupServers(ArrayList<BackupServerInfo> bsInfo) {
		this.backupServers = bsInfo;
	}
}
