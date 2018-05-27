package activitystreamer.message.applicationhandlers;

import activitystreamer.server.datalayer.Activity;
import activitystreamer.message.MessageHandler;
import activitystreamer.server.datalayer.DataLayer;
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

public class ActivityRequestHandler extends MessageHandler {

//	private final Control control;
//
//	public ActivityRequestHandler(Control control) {
//		this.control = control;
//	}

	@Override
	public boolean processMessage(JsonObject json, Connection connection) {
		Control.log.info("Activity request received from {}", connection.connectionFrom());

		String username = json.get("username").getAsString();
		String secret = null;
		try {
			secret = json.get("secret").getAsString();
		} catch (UnsupportedOperationException e) {
			secret = null;
		}

		UserRow conUser = connection.getUser();

		if (!connection.isAuthedClient()) {
			connection.sendAuthFailedMsg("The user has not logged in");
			Control.log.info("The user has not logged in");
			connection.closeCon();
			NetworkLayer.getInstance().connectionClosed(connection);
			return false;
		} else if (username == null) {
			connection.sendInvalidMsg("The message don't have username");
			Control.log.info("The message don't have username");
			connection.closeCon();
			NetworkLayer.getInstance().connectionClosed(connection);
			return false;
		} else if (!username.equals("anonymous") && secret == null) {
			connection.sendInvalidMsg("The message don't have secret and the user is not anonymous");
			Control.log.info("The message don't have secret and the user is not anonymous");
			connection.closeCon();
			NetworkLayer.getInstance().connectionClosed(connection);
			return false;
		} else if (json.get("activity") == null) {
			connection.sendInvalidMsg("The message does not have activity");
			Control.log.info("The message does not have activity");
			connection.closeCon();
			NetworkLayer.getInstance().connectionClosed(connection);
			return false;
		} else if (!username.equals("anonymous") &&
				(conUser == null || !conUser.getUsername().equals(username) || !conUser.getSecret().equals(secret))
				) {
			connection.sendAuthFailedMsg("The username and secret do not match the logged in the user");
			Control.log.info("The username and secret do not match the logged in the user");
			connection.closeCon();
			NetworkLayer.getInstance().connectionClosed(connection);
			return false;
		}

		Control.log.info("Process activity from user {}", username);
		JsonObject actJson = json.get("activity").getAsJsonObject();
		Activity activity = Activity.createActivityFromClientJson(actJson, connection.getUser().getUsername());
		DataLayer.getInstance().updateActivityTable(DataLayer.OperationType.INSERT,null,activity,true);


		return true;
	}
}
