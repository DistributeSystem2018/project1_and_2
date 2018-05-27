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

public class LoginFailedHandler extends MessageHandler {

	private final ClientSkeleton clientSkeleton;

	public LoginFailedHandler(ClientSkeleton clientSkeleton) {
		this.clientSkeleton = clientSkeleton;
	}

	@Override
	public boolean processMessage(JsonObject json,Connection connection) {
		String error = String.format("Login failed to server [%s:%s]", Settings.getRemoteHostname(), Settings.getRemotePort());
		ClientSkeleton.log.info(error);
		ClientSkeleton.log.info("Connection will be closed");
		clientSkeleton.setAuthen(false);
		return false;
	}
}
