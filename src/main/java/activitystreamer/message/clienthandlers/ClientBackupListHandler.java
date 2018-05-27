package activitystreamer.message.clienthandlers;

import activitystreamer.server.networklayer.BackupServerInfo;
import activitystreamer.client.ClientSkeleton;
import activitystreamer.message.MessageHandler;
import activitystreamer.server.networklayer.Connection;
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

public class ClientBackupListHandler extends MessageHandler {

	private final ClientSkeleton clientSkeleton;

	public ClientBackupListHandler(ClientSkeleton clientSkeleton) {
		this.clientSkeleton = clientSkeleton;
	}

	@Override
	public boolean processMessage(JsonObject json, Connection connection) {
		// refresh backup servers in case of crash
//		ClientSkeleton.log.info("Backup list received from server");
//		try {
//			ArrayList<BackupServerInfo> bsList = new ArrayList<>();
//			JsonArray serverList = json.getAsJsonArray("servers");
//			if (serverList == null) {
//				connection.sendInvalidMsg("Invalid BACKUP_LIST message");
//				return false;
//			}
//			for (JsonElement oneServer : serverList) {
//				JsonObject info = oneServer.getAsJsonObject();
//				String host = info.get("host").getAsString();
//				int ip = info.get("port").getAsInt();
//				bsList.add(new BackupServerInfo(host, ip));
//			}
//			ClientSkeleton.log.info("Update backup list:{}", json.toString());
//			clientSkeleton.setBackupServers(bsList);
//			return true;// return false so that run loop will end
//		} catch (NullPointerException | UnsupportedOperationException e) {
//			String error = String.format("Invaid message received:%s", json.toString());
//			ClientSkeleton.log.info(error);
//			return false;
//		}
		return false;
	}
}
