package activitystreamer.message.networklayerhandlers;

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

public class ServerInvalidHandler extends MessageHandler {

	@Override
	public boolean processMessage(JsonObject json,Connection connection) {
		Control.log.info("Invalid message received from {}",connection.getSocket().getRemoteSocketAddress());
		Control.log.info(json.get("info"));
		Control.log.info("Close connection with {}",connection.getSocket().getRemoteSocketAddress());
		connection.closeCon();
		NetworkLayer.getInstance().connectionClosed(connection);
		// return false to close related connection and thread
		return false;
	}
}
