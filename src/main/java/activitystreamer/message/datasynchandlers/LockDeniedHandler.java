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

public class LockDeniedHandler extends MessageHandler {

	/**
	 * |- validate message
	 * |- whether owner is the server itself
	 * 		|- Yes: send back RegisterFailed message to corresponding user
	 * 		|- No: send LockDenied to the server who sends lock request
	 * @param json
	 * @param connection
	 * @return
	 */
	@Override
	public boolean processMessage(JsonObject json,Connection connection) {
		Control.log.info("Lock Denied message is recieved {}",connection.connectionFrom());
		String username = null;
		String secret = null;

		// Validate message
		try{
			username = json.get("username").getAsString();
			secret = json.get("secret").getAsString();
		}catch (NullPointerException | UnsupportedOperationException e){
			String error = String.format("Lock denied command missing information username=[%s] secret=[%s]", username, secret);
			failHandler(error, connection);
			return false;
		}


		BroadcastResult l = UserRegisterHandler.registerLockHashMap.get(username);
//		BroadcastResult loginRequest = UserLoginHandler.enquiryRequestHashmap.get(username);
//		BroadcastResult l = lockRequest == null ? loginRequest:lockRequest;
		if (l == null ) {
			// just ignore to align with Aaron's server
			Control.log.error("No register information received for user [{}]", username);
			return true;
		}

		// whether all servers allowed
		l.addDeny();
		if( l.getResult() == BroadcastResult.LOCK_STATUS.PENDING) {
			Control.log.info("LOCK DEINED (user found) for user [{}] is received, not all servers reply, continue waiting future information...",username);
			return true;
		}

		// whether owner is the server itself, if the 'from' connection is not a server, then it is the user who sends register request
		if (!l.getFrom().isAuthedServer()) {
			try {
				BroadcastResult.LOCK_STATUS searchStatus = l.getResult();
				return BroadcastResult.processLock(searchStatus,l,new UserRow(username,secret));
			} catch (Exception e) {
				Control.log.info("The client sending register request is disconnected");
				return true; // do not close any connection as closing connection should be handled in other way
			}
		}

		// if not owner, send lockDenied to "from" server
		try {

			l.getFrom().sendLockDeniedMsg(username, secret);
			UserRegisterHandler.registerLockHashMap.remove(username);
			return true;

		} catch (Exception e) {
			Control.log.info("The server sending lock request request is disconnected");
			return true; // do not close any connection as closing connection should be handled in other way
		}





	}
	private void failHandler(String error, Connection connection) {
		Control.log.error(error);
		connection.sendInvalidMsg(error);
		connection.closeCon();
		NetworkLayer.getInstance().connectionClosed(connection);
	}
}
