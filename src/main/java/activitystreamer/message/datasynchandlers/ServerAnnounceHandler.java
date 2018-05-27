package activitystreamer.message.datasynchandlers;

import activitystreamer.message.MessageHandler;
import activitystreamer.server.datalayer.DataLayer;
import activitystreamer.server.datalayer.ServerRow;
import activitystreamer.server.networklayer.Connection;
import activitystreamer.server.application.Control;
import activitystreamer.server.networklayer.NetworkLayer;
import com.google.gson.JsonObject;

import static activitystreamer.message.datasynchandlers.ServerAnnounceHandler.AnnounceType.UPDATE_OR_INSERT;

/**
 * RegisterMessage
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class ServerAnnounceHandler extends MessageHandler {
	public enum AnnounceType{
		UPDATE_OR_INSERT,
		DELETE
	}

	@Override
	public boolean processMessage(JsonObject json, Connection connection) {
		Control.log.debug("Announcement received from {}" , connection.connectionFrom());
		try {
			AnnounceType announceType = AnnounceType.valueOf(json.get("action").getAsString());
			if(announceType == UPDATE_OR_INSERT) {
//				String serverId = json.get("serverId").getAsString();
//				int load = json.get("load").getAsInt();
//				String host = json.get("ip").getAsString();
//				int port = json.get("port").getAsInt();
				ServerRow receivedInfo = new ServerRow(json);
//				if(!receivedInfo.isOnline()){
//					System.out.println("haha");
//				}
				DataLayer.getInstance().updateServerTable(DataLayer.OperationType.UPDATE_OR_INSERT,receivedInfo,false);
			}
//			else{
//				String serverId = json.get("serverId").getAsString();
//				ServerRow deleteRwo = new ServerRow(serverId,false);
//				DataLayer.getInstance().updateServerTable(DataLayer.OperationType.DELETE,deleteRwo,false);
//			}

			NetworkLayer.getInstance().broadcastToServers(json.toString(),connection);

		}catch (Exception e){
			DataLayer.log.error("Invalid ServerAnnounce message:[{}]",json.getAsString());
			e.printStackTrace();
		}
		return true;
	}
}
