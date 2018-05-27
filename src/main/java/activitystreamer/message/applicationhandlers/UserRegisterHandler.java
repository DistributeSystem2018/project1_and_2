package activitystreamer.message.applicationhandlers;

import activitystreamer.message.datasynchandlers.BroadcastResult;
import activitystreamer.message.MessageHandler;
import activitystreamer.server.application.Control;
import activitystreamer.server.datalayer.DataLayer;
import activitystreamer.server.networklayer.Connection;
import activitystreamer.server.networklayer.NetworkLayer;
import com.google.gson.JsonObject;

import java.util.HashMap;

/**
 * UserRegisterHandler
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class UserRegisterHandler extends MessageHandler {
	// HashMap<username, LockResult>
	public static HashMap<String, BroadcastResult> registerLockHashMap = new HashMap<>();

	/**
	 * Check user exists and register
	 * 1. Validate message
	 * 2. Check if user exists locally
	 * YES: send register failed msg and return false
	 * NO: 2.1 check if username under register process( in the register list but not approved)
	 * YES: send register failed msg and return false
	 * NO:  2.1.1 check if any remote servers exists
	 * YES: broadcastToAll
	 * NO: register succ
	 *
	 * @param json       message
	 * @param connection connection that received this message
	 * @return true if succ
	 */
	@Override
	public boolean processMessage(JsonObject json, Connection connection) {
		Control.log.info("Register message is received from {}", connection.getSocket().getRemoteSocketAddress());

//		if(control.isOutOfService()){
//			connection.sendInvalidMsg("This server is temporary out-of-service, try again later");
//			connection.closeCon();
//			control.connectionClosed(connection);
//			return false;
//		}


//		User newUser = null;
		String username = null;
		String secret = null;

		// 1. Validate message
		try {
			username = json.get("username").getAsString();
			secret = json.get("secret").getAsString();

		} catch (NullPointerException | UnsupportedOperationException e) {
			String error = String.format("User register command missing information username=[%s] secret=[%s]", username, secret);
			connection.sendInvalidMsg(error);
			failHandler(error, connection);
			return false;
		}

		if (username.equals("anonymous")) {
			String error = String.format("Invalid username 'anonymous' or you forget input your username");
			connection.sendInvalidMsg(error);
			failHandler(error, connection);
			return false;
		}

		Control.log.info("process register for user {}", username);
		BroadcastResult.REGISTER_RESULT registerRegister = DataLayer.getInstance().registerUser(
				username,
				secret,
				connection);
		switch(registerRegister){
			case SUCC:
				connection.sendRegisterSuccMsg(username);
				return true;
			case FAIL_UNDER_REGISTER:
				String error1 = String.format("User [%s] is under register processing'", username);
				connection.sendAuthFailedMsg(error1);
				return false;
			case FAIL:
				String error2 = String.format("User [%s] exists in this server'", username);
				connection.sendRegisterFailedMsg(error2);
				break;
			case PROCESSING:
				Control.log.info("Remote servers exist, need to get confirmation from remote servers for user register [{}] ", username);
				return true;
		}

		return false;

	}

	private void failHandler(String error, Connection connection) {
		Control.log.info(error);
		connection.closeCon();
		NetworkLayer.getInstance().connectionClosed(connection);
	}
}
