package activitystreamer.message.clienthandlers;

import activitystreamer.client.ClientSkeleton;
import activitystreamer.message.MessageHandler;
import activitystreamer.server.networklayer.Connection;
import com.google.gson.JsonObject;

/*
author Yiru Pan
date 14/4/18
 */
public class ClientActivityBroadcastHandler extends MessageHandler {
    private final ClientSkeleton clientSkeleton;

    public ClientActivityBroadcastHandler(ClientSkeleton clientSkeleton)
    {
        this.clientSkeleton=clientSkeleton;
    }

    @Override
    public boolean processMessage(JsonObject json, Connection connection) {
        clientSkeleton.log.info("Client received broadcastToAll activity from server");
        //display received content in UI
        try {
            JsonObject act = json.get("activity").getAsJsonObject();
            clientSkeleton.showOutput(act);
            return true;
        }catch (Exception e){
            ClientSkeleton.log.info("Invalid activity message, no 'activity' part found");
            ClientSkeleton.log.error("Disconnect with server");
            return false;
        }
    }
}
