package activitystreamer.server.datalayer;

import activitystreamer.message.MessageGenerator;
import activitystreamer.message.MessageHandler;
import activitystreamer.message.MessageType;
import activitystreamer.message.applicationhandlers.UserRegisterHandler;
import activitystreamer.message.datasynchandlers.*;
import activitystreamer.server.application.Control;
import activitystreamer.server.networklayer.Connection;
import activitystreamer.server.networklayer.IMessageConsumer;
import activitystreamer.server.networklayer.NetworkLayer;
import activitystreamer.util.Settings;
import activitystreamer.util.Tools;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;

public class DataLayer extends Thread implements IMessageConsumer {
	public enum OperationType {
		UPDATE_OR_INSERT,
		DELETE,
		INSERT,
		UPDATE,
		SYNC,
		MARK_AS_DELIVERED,
	}

	public static final Logger log = Control.log;
	private static DataLayer dataLayer = new DataLayer();
	private UserTable userTable;
	private ServerTable serverTable;
	private ActivityTable activityTable;
	private HashMap<MessageType, MessageHandler> handlerMap;
	private boolean term;

	public static DataLayer getInstance() {
//		if (DataLayer.dataLayer.isAlive()) {
			return DataLayer.dataLayer;
//		} else {
//			DataLayer.dataLayer.start();
//			return DataLayer.dataLayer;
//		}
	}

	private DataLayer() {
		userTable = new UserTable();
		serverTable = new ServerTable();
		handlerMap = new HashMap<>();
		activityTable = new ActivityTable();
		term = false;
		initialHandlers();
	}

	private void initialHandlers() {
		this.handlerMap.put(MessageType.SERVER_ANNOUNCE, new ServerAnnounceHandler());
		this.handlerMap.put(MessageType.LOCK_REQUEST, new LockRequestHandler());
		this.handlerMap.put(MessageType.LOCK_ALLOWED, new LockAllowedHandler());
		this.handlerMap.put(MessageType.LOCK_DENIED, new LockDeniedHandler());
		this.handlerMap.put(MessageType.USER_UPDATE, new UserUpdateHandler());
		this.handlerMap.put(MessageType.USER_SYNC, new UserSyncHandler());
		this.handlerMap.put(MessageType.ACTIVITY_BROADCAST, new ActivityBroadcastHandler());
		this.handlerMap.put(MessageType.ACTIVITY_UPDATE,new ActivityUpdateHandler());
		this.handlerMap.put(MessageType.ACTIVITY_SYNC,new ActivitySyncHandler());

		NetworkLayer.getInstance().registerConsumer(handlerMap.keySet(), this);
	}

	@Override
	public boolean process(Connection con, JsonObject json) {
		boolean isSucc = false;
		try {
			MessageType m = MessageType.valueOf(json.get("command").getAsString());
			MessageHandler h = handlerMap.get(m);
			if (h != null) {
				isSucc = h.processMessage(json, con);
			} else {
				log.info("[DataLayer]Ignore message type [{}]", m.name());
			}
		} catch (IllegalStateException e) {
			String info = String.format("Invalid message [%s]", json.toString());
			log.error(info);
			isSucc = false;
			String invalidMsg = MessageGenerator.invalid(info);
			con.writeMsg(invalidMsg);
		}
		return isSucc;
	}

	/*======================================= data sync  =======================================*/
	@Override
	public void run() {
		log.info("using sync interval period of " + Settings.getSyncInterval() + " milliseconds");
		while (!term) {
			// do something with 5 second intervals in between
			try {
				updateCurrentLoad();
				syncUserData();
				syncActivityData();
				Thread.sleep(Settings.getSyncInterval());
			} catch (InterruptedException e) {
				log.info("received an interrupt, system is shutting down");
				break;
			}
		}
	}

	public void setTerm(boolean term) {
		this.term = term;
	}
	private void syncUserData(){
		String syncString = MessageGenerator.userSyncCommand();
		NetworkLayer.getInstance().broadcastToServers(syncString,null);
	}

	private void syncActivityData(){
		String syncString = MessageGenerator.activitySyncCommand();
		NetworkLayer.getInstance().broadcastToServers(syncString,null);
	}

	private void updateCurrentLoad() {
		DataLayer.getInstance().updateServerTable(OperationType.UPDATE_OR_INSERT,
				new ServerRow(
						Settings.getServerId(),
						NetworkLayer.getInstance().getClientLoads(),
						Settings.getLocalHostname(),
						Settings.getLocalPort()),
				true
		);
	}

	/*======================================= Server related API =======================================*/
	public synchronized ServerRow updateServerTable(OperationType operationType,ServerRow serverRow, boolean notify) {
		ServerRow row = null;
		switch (operationType) {
//			case DELETE:
////				row = deleteServer(serverRow.getId());
//				break;
			case UPDATE_OR_INSERT:
				row = updateOrInsert(serverRow);
				break;
			default:
				DataLayer.log.error("Miss branch for type [{}]",operationType);
				System.exit(-1);
		}
		if (notify && row != null) {
			row.notifyChange();
		}
		return row;
	}

	public ServerRow getMinLoadServer() {
		return (ServerRow) Tools.deepClone(serverTable.getMinLoadServer());
	}




	public HashMap<String, ServerRow> getServerStateList() {
		return (HashMap<String, ServerRow>) Tools.deepClone(serverTable.getAll());
	}


	private ServerRow updateOrInsert(ServerRow server) {
		return serverTable.updateOrInsert(server);
	}

//	private ServerRow deleteServer(String id) {
//		return serverTable.delete(id);
//	}

	public ServerRow getServerById(String id){
		return (ServerRow)Tools.deepClone(serverTable.selectById(id));
	}

	public void mergeAllServerData(JsonArray json) throws Exception {
		for (JsonElement je : json) {
			ServerRow serverRow = new ServerRow(je.getAsJsonObject());
			updateServerTable(OperationType.UPDATE_OR_INSERT,serverRow,false);
		}
	}
	/*======================================= User Related API  =======================================*/
	/* User Related API */
	public synchronized UserRow updateUserTable(OperationType operationType, UserRow userRow, boolean notify) {
		UserRow row = null;
		switch (operationType) {
			case DELETE:
				break;
			case UPDATE_OR_INSERT:
				row = updateOrInsert(userRow);
				break;
			default:
				DataLayer.log.error("Miss branch for type [{}]",operationType);
				System.exit(-1);
		}
		if (notify && row != null) {
			row.notifyChange();
		}
		return row;
	}

	private UserRow updateOrInsert(UserRow user) {
		return userTable.updateOrInsert(user);
	}


	public UserRow getUserByName(String username) {
		return (UserRow) Tools.deepClone(userTable.selectById(username));

	}

	public BroadcastResult.REGISTER_RESULT registerUser(String username, String secret, Connection from) {

		// 2.1 check if username exists
		if (DataLayer.getInstance().getUserByName(username) != null) {
			return BroadcastResult.REGISTER_RESULT.FAIL;
		}

		// 2.1 check if username under register process( in the register list but not approved)
		if (UserRegisterHandler.registerLockHashMap.containsKey(username)) {
			return BroadcastResult.REGISTER_RESULT.FAIL_UNDER_REGISTER;
		}

		// 2.1.1 check if any remote servers exists
		int serverLoads = NetworkLayer.getInstance().getServerLoads(null);
		if (serverLoads > 0) {
			BroadcastResult lockResult = new BroadcastResult(from, serverLoads, new UserRow(username, secret));
			UserRegisterHandler.registerLockHashMap.put(username, lockResult);
			// broadcastToAll lock request and then waiting for lock_allow & lock_denied, this register process will be handled by LockAllowedHandler & LockDeniedHandler
			String lockRequest = MessageGenerator.lockRequest(username, secret);
			NetworkLayer.getInstance().broadcastToServers(lockRequest, null);
			return BroadcastResult.REGISTER_RESULT.PROCESSING;
		}

		// register successfully if no above condision
		DataLayer.log.info("No additional server connected, send REGISTER_SUCC for user:{} ", username);
		DataLayer.log.info("Add user {} into local register user list", username);
		UserRow newUser = new UserRow(username, secret);
		DataLayer.getInstance().updateUserTable(OperationType.UPDATE_OR_INSERT, newUser, false);
		return BroadcastResult.REGISTER_RESULT.SUCC;
	}

	public HashMap<String, UserRow> getAllUsers() {
		return (HashMap<String, UserRow>) Tools.deepClone(userTable.getAll());
	}

	public ArrayList<UserRow> getConnectedUsers() {
		return (ArrayList<UserRow>) Tools.deepClone(userTable.connectedUserList());
	}

	public void mergeAllUserData(JsonArray json) throws Exception {
		for (JsonElement je : json) {
			UserRow userRow = new UserRow(je.getAsJsonObject());
			updateUserTable(OperationType.UPDATE_OR_INSERT, userRow, false);
		}
	}

	public void markUserOnline(String username, boolean online) {
		UserRow row = getUserByName(username);
		row.login(online);
		updateUserTable(OperationType.UPDATE_OR_INSERT, row, true);
	}

	/************************************************************************************************************************/
	/* Activity Related API */
	public synchronized ActivityRow updateActivityTable(OperationType operationType,String username, Activity activity, boolean notify) {
		switch (operationType) {
			case DELETE:
				break;
			case INSERT:
				updateOrInsert(activity);
				JsonObject clientJson = activity.toClientJson();
				clientJson.addProperty("command",MessageType.ACTIVITY_BROADCAST.name());
				NetworkLayer.getInstance().sendMsgToAnonymous(clientJson.toString());
				if (notify) {
					JsonObject serverJson = activity.toJson();
					serverJson.addProperty("command", MessageType.ACTIVITY_BROADCAST.name());
					NetworkLayer.getInstance().broadcastToServers(serverJson.toString(),null);;
				}
				break;
			case MARK_AS_DELIVERED:
				setActivityDelivered(username,activity);
				if (notify) activityTable.selectById(username).notifyActivityChange(activity);
				break;
			case SYNC:
				Activity updatedActivity = activityTable.syncActivityForUser(username,activity);
				break;
			default:
				DataLayer.log.error("Miss branch for type [{}]",operationType);
				System.exit(-1);

		}

		return null;
	}

	private void updateOrInsert(Activity activity) {
		activityTable.updateOrInsert(activity);

	}

	public ActivityRow pendingActivity(String name) {
		return (ActivityRow) Tools.deepClone(activityTable.selectById(name));
	}

	public void mergeAllActivityData(JsonArray json) throws Exception {
		for (JsonElement je : json) {
			ActivityRow activityRow = new ActivityRow(je.getAsJsonObject());
			for(Activity activity:activityRow.getActivityList()) {
				updateActivityTable(OperationType.SYNC, activityRow.getOwner(),activity,false);
			}
		}

	}

	public HashMap<String, ActivityRow> getAllActivities() {
		return (HashMap<String, ActivityRow>) Tools.deepClone(activityTable.getAll());
	}

	private void setActivityDelivered(String username, Activity activity) {
		activityTable.selectById(username).updateOrInsert(activity);
//		activity.setDelivered(true);
	}
}
