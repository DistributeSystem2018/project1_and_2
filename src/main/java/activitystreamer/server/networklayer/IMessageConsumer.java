package activitystreamer.server.networklayer;

import activitystreamer.server.networklayer.Connection;
import com.google.gson.JsonObject;

/**
 * IMessageConsumer
 * <p>
 * Author Ning Kang
 * Date 18/5/18
 */

public interface IMessageConsumer {
	boolean process(Connection con, JsonObject jsonObject);
}
