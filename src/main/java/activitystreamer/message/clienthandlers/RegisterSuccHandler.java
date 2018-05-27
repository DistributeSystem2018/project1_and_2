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

public class RegisterSuccHandler extends MessageHandler {

	private final ClientSkeleton clientSkeleton;

	public RegisterSuccHandler(ClientSkeleton clientSkeleton) {
		this.clientSkeleton = clientSkeleton;
	}

	@Override
	public boolean processMessage(JsonObject json,Connection connection) {
		//register success, close connection
		clientSkeleton.disconnect();
		ClientSkeleton.log.info("Register successfully to server {}:{}", Settings.getRemoteHostname(),Settings.getRemotePort());
		ClientSkeleton.log.info("Close client and login with parameters:");
		ClientSkeleton.log.info("-u {} -s {} -rh {} -rp {} ", Settings.getUsername(),Settings.getSecret(),Settings.getRemoteHostname(),Settings.getRemotePort());
		ClientSkeleton.log.info("Login automatically after register success, accroding to head tutor's comment");
		return clientSkeleton.redirectToServer(Settings.getRemoteHostname(),Settings.getRemotePort());
//		clientSkeleton.sendLoginMsg();
//		return true;// return false so that run loop will end
	}
}
