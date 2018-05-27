package activitystreamer.message.datasynchandlers;

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

public class LockAllowedHandler extends MessageHandler {

	@Override
	public boolean processMessage(JsonObject json, Connection connection) {
		Control.log.info("Lock allowed is received from {}", connection.connectionFrom());
		UserRow u = null;
		String username = null;
		String secret = null;

		// Validate message
		try {
			username = json.get("username").getAsString();
			secret = json.get("secret").getAsString();
			u = new UserRow(username, secret);
		} catch (NullPointerException | UnsupportedOperationException e) {
			String error = String.format("Lock allowed command missing information username=[%s] secret=[%s]", username, secret);
			failHandler(error, connection);
			return false;
		}


		BroadcastResult l = UserRegisterHandler.registerLockHashMap.get(username);

		// whether the register information in pending list
		if (l == null) {
			// just ignore to align with Aaron's server
			Control.log.info("No register information received for user [{}] or the register information is processed already, ignore this message", username);
			return true;
		}

		// whether all servers allowed
		l.addAllow();
		if (l.getResult() == BroadcastResult.LOCK_STATUS.PENDING) {
			Control.log.info("Although LOCK ALLOWED is received, not all servers reply, continue waiting future information...");
			return true;
		}

		// here means this server receives LOCK responses (user not found) from all other servers
		// if owner is the server itself
		if (!l.getFrom().isAuthedServer()) {
			Control.log.info("LOCK ALLOWEDs for user [{}] from all servers are received .", username);
			try {
				BroadcastResult.LOCK_STATUS searchStatus = l.getResult();
				return BroadcastResult.processLock(searchStatus, l, u);
			} catch (Exception e) {
				Control.log.info("The client sending register request was disconnected");
				return true;
			}
		}

		Control.log.info("LOCK ALLOWEDs for user [{}] from all servers are received, " +
				"send LOCK_ALLOW to the server who sends LOCK_REQUEST", username);

		// if not owner, send lockAllow to "from" server
		try {
			l.getFrom().sendLockAllowedMsg(username, secret);
			UserRegisterHandler.registerLockHashMap.remove(username);
			return true;

		} catch (Exception e) {
			Control.log.info("The server sending lock request request is disconnected");
			return true;
		}

	}

	private void failHandler(String error, Connection connection) {
		Control.log.error(error);
		connection.sendInvalidMsg(error);
		connection.closeCon();
		NetworkLayer.getInstance().connectionClosed(connection);
	}
}
