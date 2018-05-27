package activitystreamer.message.applicationhandlers;

import activitystreamer.message.datasynchandlers.BroadcastResult;
import activitystreamer.message.MessageHandler;
import activitystreamer.server.networklayer.Connection;
import com.google.gson.JsonObject;

/**
 * ReigsterHandler
 * <p>
 * Author Ning Kang
 * Date 18/5/18
 */

public class RegisterResultHandler extends MessageHandler {
	@Override
	public boolean processMessage(JsonObject json, Connection connection) {
		BroadcastResult.REGISTER_RESULT result = BroadcastResult.REGISTER_RESULT.valueOf(json.get("result").getAsString());
		if(result == BroadcastResult.REGISTER_RESULT.SUCC) {
			connection.sendRegisterSuccMsg(json.get("username").toString());
			return true;
		}else{
			connection.sendRegisterFailedMsg(json.get("username").toString());
			return false;
		}
	}
}
