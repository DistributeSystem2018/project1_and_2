package activitystreamer.message.datasynchandlers;

import activitystreamer.message.MessageHandler;
import activitystreamer.server.datalayer.Activity;
import activitystreamer.server.datalayer.ActivityRow;
import activitystreamer.server.datalayer.DataLayer;
import activitystreamer.server.networklayer.Connection;
import activitystreamer.server.networklayer.NetworkLayer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * ActivitySyncHandler
 * <p>
 * Author Ning Kang
 * Date 20/5/18
 */


public class ActivitySyncHandler extends MessageHandler {
	@Override
	public boolean processMessage(JsonObject json, Connection connection) {
		DataLayer.log.info("Activity sync message received from {}",connection.connectionFrom());
		JsonArray activity_list = null;

		// Validate message
		try {
			activity_list = json.get("activity_entity").getAsJsonArray();
			for (JsonElement actRow : activity_list) {
				ActivityRow activityRow = new ActivityRow(actRow.getAsJsonObject());
				String owner = activityRow.getOwner();
				for (Activity activity : activityRow.getActivityList()) {
					DataLayer.getInstance().updateActivityTable(
							DataLayer.OperationType.SYNC,
							owner,
							activity,
							false
					);
				}
			}
			NetworkLayer.getInstance().broadcastToServers(json.toString(), connection);

		} catch (Exception e) {
			String error = "Activity sync message invalid:" + json.toString();
			DataLayer.log.error(error);
			connection.sendInvalidMsg(error);
			e.printStackTrace();
			return false;
		}
		return true;
		//TODO pending
	}
}
