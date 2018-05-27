package activitystreamer.message.clienthandlers;

import activitystreamer.client.ClientSkeleton;
import activitystreamer.message.MessageHandler;
import activitystreamer.server.networklayer.Connection;
import com.google.gson.JsonObject;

/**
 * RegisterMessage
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class ClientInvalidHandler extends MessageHandler {

	private final ClientSkeleton clientSkeleton;

	public ClientInvalidHandler(ClientSkeleton clientSkeleton) {
		this.clientSkeleton = clientSkeleton;
	}

	@Override
	public boolean processMessage(JsonObject json,Connection connection) {

		ClientSkeleton.log.info("Invalid message received from server");
		ClientSkeleton.log.info("Connection will be closed");
		return false;
	}
}
