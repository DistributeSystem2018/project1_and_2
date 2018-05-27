package activitystreamer.message.applicationhandlers;

import activitystreamer.message.MessageHandler;
import activitystreamer.server.application.Control;
import activitystreamer.server.datalayer.DataLayer;
import activitystreamer.server.datalayer.ServerRow;
import activitystreamer.server.datalayer.UserRow;
import activitystreamer.server.networklayer.Connection;
import activitystreamer.server.networklayer.NetworkLayer;
import com.google.gson.JsonObject;

/**
 * RegisterMessage
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class UserLoginHandler extends MessageHandler {
//	public static HashMap<String, BroadcastResult> enquiryRequestHashmap = new HashMap<>();;

	/**
	 * 1. check if anoymous user
	 * 2. check if both username & secret exist
	 * 3. check if user exists locally
	 */
	@Override
	public boolean processMessage(JsonObject json, Connection connection) {

		Control.log.info("login message is received from {}", connection.getSocket().getRemoteSocketAddress());

//		if(control.isOutOfService()){
//			connection.sendLoginFailedMsg("This server is temporary out-of-service, try again later");
//			connection.closeCon();
//			control.connectionClosed(connection);
//			return false;
//		}

		String username = null;
		String secret = null;


		// 1. check if both username & secret exist
		try {
			username = json.get("username").getAsString();

			//2.check anonymous login
			if (username.equals("anonymous")) {
				connection.setAuthed(true);
				connection.setUser(new UserRow(username, ""));
				connection.sendLoginSuccMsg(String.format("login successfully as user '%s '", username));

				//check redirect
				if (redirectCheck(connection, username)) {
					return true;
				}
				return true;
			}
			secret = json.get("secret").getAsString();
		} catch (NullPointerException | UnsupportedOperationException e) {
			String error = String.format("Information missing for login, username=[%s] secret=[%s]", username, secret);
			Control.log.info(error);
			connection.sendInvalidMsg(error);
			connection.closeCon();
			NetworkLayer.getInstance().connectionClosed(connection);
			return false;
		}


		// 3. Check if user exists locally
		UserRow localUser = DataLayer.getInstance().getUserByName(username);
		if (localUser != null) {
			// if secret is correct
			if (localUser.getSecret().equals(secret)) {
				connection.setAuthed(true);
				connection.setUser(localUser);
				connection.sendLoginSuccMsg(String.format("login successfully as user [%s]", username));
				//check redirect
				if (redirectCheck(connection, username)) {
					return true;
				}
				DataLayer.getInstance().markUserOnline(username, true);
				return true;
			} else {
				String info = String.format("Secret [%s] does not match for user [%s]", secret, username);
				Control.log.info(info);
				connection.sendLoginFailedMsg(info);
				connection.closeCon();
				NetworkLayer.getInstance().connectionClosed(connection);
				return false;
			}
		}
		connection.sendLoginFailedMsg(String.format("User [%s] does not exist.", username));
		Control.log.info("User [{}] does not exist.", username);
		connection.closeCon();
		NetworkLayer.getInstance().connectionClosed(connection);
		return false;
	}

	public static boolean redirectCheck(Connection connection, String username) {
		Control control = Control.getInstance();
		ServerRow server = Control.getInstance().findRedirectServer();
		if (server != null) {
			Control.log.info("Redirection is triggered, redirect user to server {}:{}", server.getIp(), server.getPort());
			control.doRedirect(connection, server, username);
			return true;
		}
		return false;
	}

}
