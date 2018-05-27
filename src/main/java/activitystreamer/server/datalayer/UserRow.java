package activitystreamer.server.datalayer;

import activitystreamer.message.MessageType;
import activitystreamer.server.networklayer.NetworkLayer;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.Calendar;

/**
 * UserRow
 * <p>
 * Author Ning Kang
 * Date 18/5/18
 */

public class UserRow implements IRow, Serializable {
	private String username;
	private String secret;
//	private ArrayList<Activity> activityQueue;
	private boolean online;
	private long updateTime;


	public UserRow(String username, String secret) {
		this.username = username;
		this.secret = secret;
		this.updateTime = Calendar.getInstance().getTimeInMillis();
		this.online = false;
	}
	public UserRow(JsonObject userJson){
			this.username = userJson.get("username").getAsString();
			this.secret = userJson.get("secret").getAsString();
			this.online = Boolean.parseBoolean(userJson.get("online").getAsString());
			this.updateTime = Long.parseLong(userJson.get("updateTime").getAsString());
	}

	public String getUsername() {
		return username;
	}

	public String getSecret() {
		return secret;
	}


	public boolean isOnline() {
		return this.online;
	}

	@Override
	public long getUpdateTime() {
		return updateTime;
	}

	public JsonObject toJson(){
		JsonObject json = new JsonObject();
		json.addProperty("username",username);
		json.addProperty("secret",secret);
		json.addProperty("online",online);
		json.addProperty("updateTime",updateTime);
		return json;
	}

	@Override
	public boolean lock() {
		return false;
	}

	@Override
	public boolean unlock() {
		return false;
	}

	@Override
	public UserRow update(IRow user) {
		if(user.getUpdateTime() > getUpdateTime()){
			this.online = ((UserRow)user).isOnline();
			this.updateTime = user.getUpdateTime();
		}
		return this;
	}

	@Override
	public String getId() {
		return username;
	}

	public void login(boolean online){
		this.online = online;
		this.updateTime = Calendar.getInstance().getTimeInMillis();
	}

	public void notifyChange(){
		String resultBroadcastStr = userUpdateJsonString();
		NetworkLayer.getInstance().broadcastToServers(resultBroadcastStr, null);;
	}

//	@Override
//	public void notifyChange(Connection connection) {
//		String resultBroadcastStr = userUpdateJsonString();
//		NetworkLayer.getInstance().broadcastToServers(resultBroadcastStr, connection);
//	}

	private String userUpdateJsonString(){
		JsonObject json = this.toJson();
		json.addProperty("command", MessageType.USER_UPDATE.name());
		return json.toString();
	}
}
