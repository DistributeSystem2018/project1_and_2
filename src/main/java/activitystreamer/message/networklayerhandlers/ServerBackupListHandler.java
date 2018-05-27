package activitystreamer.message.networklayerhandlers;

import activitystreamer.server.networklayer.BackupServerInfo;
import activitystreamer.message.MessageHandler;
import activitystreamer.server.networklayer.Connection;
import activitystreamer.server.application.Control;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;

/**
 * RegisterMessage
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class ServerBackupListHandler extends MessageHandler {

	@Override
	public boolean processMessage(JsonObject json, Connection connection) {
		// refresh backup servers in case of crash
		Control.log.debug("Backup list received from server");
		try {
			ArrayList<BackupServerInfo> bsList = new ArrayList<>();
			JsonArray serverList = json.getAsJsonArray("servers");
			if (serverList == null) {
				connection.sendInvalidMsg("Invalid BACKUP_LIST message");
				return false;
			}
			for (JsonElement oneServer : serverList) {
				JsonObject info = oneServer.getAsJsonObject();
				String host = info.get("host").getAsString();
				int ip = info.get("port").getAsInt();
				String serverId = info.get("serverId").getAsString();
				bsList.add(new BackupServerInfo(serverId,host, ip));
			}
			Control.log.debug("Update backup list for server {}:{}",
					connection.getRemoteServerHost(), connection.getRemoteServerPort());
			connection.setBackupServers(bsList);
			return true;
		} catch (NullPointerException | UnsupportedOperationException e) {
			String error = String.format("Invaid BACKUP_LIST message received:%s", json.toString());
			Control.log.info(error);
			return false;
		}
	}
}
