package activitystreamer.message.applicationhandlers;

import activitystreamer.message.MessageHandler;
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

public class ServerAuthenFailedHandler extends MessageHandler {

	@Override
	public boolean processMessage(JsonObject json,Connection connection) {
		Control.log.info("Authen Failed message received from {}",  connection.getSocket().getRemoteSocketAddress());
		Control.log.info(json.get("info").getAsString());
		Control.log.info("Close connection to {}", connection.getSocket().getRemoteSocketAddress());
		connection.closeCon();
		NetworkLayer.getInstance().connectionClosed(connection);
		// return false to close related connection and thread
		return false;
	}
}
