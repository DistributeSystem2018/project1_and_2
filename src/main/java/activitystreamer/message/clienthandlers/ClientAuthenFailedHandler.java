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

public class ClientAuthenFailedHandler extends MessageHandler {

	private final ClientSkeleton clientSkeleton;

	public ClientAuthenFailedHandler(ClientSkeleton clientSkeleton) {
		this.clientSkeleton = clientSkeleton;
	}

	@Override
	public boolean processMessage(JsonObject json,Connection connection) {

		ClientSkeleton.log.info("Cannot send activity as username or secret is not correct or you are an anonymous");
		ClientSkeleton.log.info("Connection will be closed");
		return false;
	}
}
