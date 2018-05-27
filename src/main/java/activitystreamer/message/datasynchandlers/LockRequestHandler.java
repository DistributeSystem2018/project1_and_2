package activitystreamer.message.datasynchandlers;

import activitystreamer.message.MessageGenerator;
import activitystreamer.message.MessageHandler;
import activitystreamer.message.applicationhandlers.UserRegisterHandler;
import activitystreamer.server.datalayer.UserRow;
import activitystreamer.server.networklayer.Connection;
import activitystreamer.server.application.Control;
import activitystreamer.server.networklayer.NetworkLayer;
import com.google.gson.JsonObject;

/**
 * RegisterMessage
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class LockRequestHandler extends MessageHandler {

	@Override
	public boolean processMessage(JsonObject json, Connection connection) {
		Control.log.info("Lock request received from {}", connection.connectionFrom());
		UserRow newUser = null;
		String username = null;
		String secret = null;

		// Validate message
		try {
			username = json.get("username").getAsString();
			secret = json.get("secret").getAsString();

		}
		catch (NullPointerException | UnsupportedOperationException e) {
			String error = String.format("Lock request command missing information username=[%s] secret=[%s]", username, secret);
			failHandler(error, connection);
			return false;
		}

		newUser = new UserRow(username, secret);
//
//		// check locally
//		UserRow localUser = DataLayer.getInstance().getUserByName(username);
//		if (localUser != null) {
//			Control.log.info("User [{}] exists in this server, reply lock denied (user found) request with secret in this server",username);
//			connection.sendLockDeniedMsg(username, localUser.getSecret());
//			return true;
//		}

		// check lockrequest list
		if (UserRegisterHandler.registerLockHashMap.get(username)!=null){
			Control.log.info("User [{}] is already under register process, reject",username);
			connection.sendLockDeniedMsg(username, secret);
			return true;
		}
		
		// Check remotely if server load > 0 exclude the sending server
//		Control.log.info("User [{}] does not exist in this server", username);
		if (NetworkLayer.getInstance().getServerLoads(connection) > 0) {
			Control.log.info("More server found, send lock request to other servers(exclude the sending server)");
			// updateOrInsert this requst to local requst hashmap
			BroadcastResult lockResult = new BroadcastResult(connection,
					NetworkLayer.getInstance().getServerLoads(connection),
					newUser);
			UserRegisterHandler.registerLockHashMap.put(username, lockResult);
			String lockMessage = MessageGenerator.lockRequest(newUser.getUsername(),newUser.getSecret());
			NetworkLayer.getInstance().broadcastToServers(lockMessage, connection);

		} else {
			Control.log.info("No other servers in this system(exclude the sending server), reply lockAllow message back");
			connection.sendLockAllowedMsg(username, secret);
			return true;
		}

		return true;
	}

	private void failHandler(String error, Connection connection) {
		Control.log.error(error);
		connection.sendInvalidMsg(error);
		connection.closeCon();
		NetworkLayer.getInstance().connectionClosed(connection);
	}
}
