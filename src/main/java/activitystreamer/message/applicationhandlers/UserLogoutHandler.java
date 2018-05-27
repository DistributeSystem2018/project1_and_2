package activitystreamer.message.applicationhandlers;

import activitystreamer.message.MessageHandler;
import activitystreamer.server.datalayer.DataLayer;
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

public class UserLogoutHandler extends MessageHandler {

	@Override
	public boolean processMessage(JsonObject json,Connection connection) {
		Control.log.info("Logout request from {} is received.",connection.getSocket().getRemoteSocketAddress());
		Control.log.info("User {} logout, close connection.",connection.getUser().getUsername());
		DataLayer.getInstance().markUserOnline(connection.getUser().getUsername(),false);
		connection.closeCon();
		NetworkLayer.getInstance().connectionClosed(connection);
		return true;
	}
}
