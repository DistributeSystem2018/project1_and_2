package activitystreamer.message.datasynchandlers;

import activitystreamer.message.MessageHandler;
import activitystreamer.server.datalayer.Activity;
import activitystreamer.server.datalayer.DataLayer;
import activitystreamer.server.networklayer.Connection;
import activitystreamer.server.networklayer.NetworkLayer;
import com.google.gson.JsonObject;

/**
 * UserUpdate
 * <p>
 * Author Ning Kang
 * Date 18/5/18
 */

public class ActivityUpdateHandler extends MessageHandler {
	@Override
	public boolean processMessage(JsonObject json, Connection connection) {
		DataLayer.log.info("Update single activity info {}",connection.connectionFrom());

		// Validate message
		try {
			Activity newActivity = Activity.createActivityFromServerJson(json);
			String owner = json.get("owner").getAsString();
			DataLayer.getInstance().updateActivityTable(DataLayer.OperationType.MARK_AS_DELIVERED, owner, newActivity, false);
			NetworkLayer.getInstance().broadcastToServers(json.toString(), connection);

		} catch (Exception e) {
			String error = "ACTIVITY_UPDATE message invalid:" + json.toString();
			DataLayer.log.error(error);
			connection.sendInvalidMsg(error);
			return false;
		}

		return true;
	}
}
