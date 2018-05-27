package activitystreamer.message.clienthandlers;

import activitystreamer.client.ClientSkeleton;
import activitystreamer.message.MessageHandler;
import activitystreamer.server.networklayer.Connection;
import activitystreamer.util.Settings;
import com.google.gson.JsonObject;

/**
 * RegisterMessage
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class RedirectHandler extends MessageHandler {

	private final ClientSkeleton clientSkeleton;

	public RedirectHandler(ClientSkeleton clientSkeleton) {
		this.clientSkeleton = clientSkeleton;
	}

	@Override
	public boolean processMessage(JsonObject json, Connection connection) {
		try {
			ClientSkeleton.log.info("redirect message received from server {}:{}", Settings.getRemoteHostname(), Settings.getRemotePort());

			String newRemoteHost = json.get("hostname").getAsString();
			int newRemotePort = json.get("port").getAsInt();

			ClientSkeleton.log.info("redirect to server {}:{}", newRemoteHost, newRemotePort);

			if (!clientSkeleton.redirectToServer(newRemoteHost, newRemotePort)) {
				ClientSkeleton.log.error("Cannot redirect to server {}:{}", newRemoteHost, newRemotePort);
				clientSkeleton.disconnect();
				return false;
			}

			return true;
		} catch (NullPointerException e) {
			ClientSkeleton.log.info("Invalid redirection message received:{}", json.toString());
			clientSkeleton.disconnect();
		}
		return false;
	}
}
