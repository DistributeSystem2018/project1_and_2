package activitystreamer.message.datasynchandlers;

import activitystreamer.message.MessageHandler;
import activitystreamer.server.datalayer.DataLayer;
import activitystreamer.server.datalayer.UserRow;
import activitystreamer.server.networklayer.Connection;
import activitystreamer.server.networklayer.NetworkLayer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * UserSyncHandler
 * <p>
 * Author Ning Kang
 * Date 18/5/18
 */

public class UserSyncHandler extends MessageHandler {

	@Override
	public boolean processMessage(JsonObject json, Connection connection) {
		DataLayer.log.info("User sync message received {}",connection.connectionFrom());
		JsonArray user_list = null;

		// Validate message
		try {
			user_list = json.get("user_list").getAsJsonArray();
			for (JsonElement userJson : user_list) {
				UserRow userRow = DataLayer.getInstance().updateUserTable(
						DataLayer.OperationType.UPDATE_OR_INSERT,
						new UserRow(userJson.getAsJsonObject()),
						false
				);
			}
			NetworkLayer.getInstance().broadcastToServers(json.toString(), connection);

		} catch (Exception e) {
			String error = "User sync message invalid:" + json.toString();
			DataLayer.log.error(error);
			connection.sendInvalidMsg(error);
			return false;
		}
		return true;
	}
}
