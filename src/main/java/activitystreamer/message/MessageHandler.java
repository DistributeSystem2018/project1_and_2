package activitystreamer.message;

import activitystreamer.server.networklayer.Connection;
import com.google.gson.JsonObject;

/**
 * ClientHandler
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public abstract class MessageHandler {

	public abstract boolean processMessage(JsonObject json,Connection connection);

}
