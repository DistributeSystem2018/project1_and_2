package activitystreamer.server.datalayer;

import activitystreamer.message.MessageType;
import activitystreamer.server.networklayer.NetworkLayer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.*;

/**
 * ServerRow
 * <p>
 * Author Ning Kang
 * Date 18/5/18
 */

public class ActivityRow implements IRow, Serializable {
	private String owner;
	private ArrayList<Activity> activityList;

	public ActivityRow(String owner){
		this.owner = owner;
		activityList = new ArrayList<>();
	}


	public ActivityRow(JsonObject actJson){
		activityList = new ArrayList<>();
		this.owner = actJson.get("owner").getAsString();
		JsonArray actArray = actJson.get("activity_list").getAsJsonArray();
		for(JsonElement je:actArray){
			Activity act = Activity.createActivityFromServerJson(je.getAsJsonObject());
			activityList.add(act);
		}
		Collections.sort(this.activityList);
	}

	public ArrayList<Activity> getActivityList() {
		return activityList;
	}

	public String getOwner() {
		return owner;
	}

	@Override
	public long getUpdateTime() {
		return 0;
	}

	public JsonObject toJson(){
		JsonObject json = new JsonObject();
		json.addProperty("owner",owner);
		JsonArray actListJson = new JsonArray();
		for(Activity act:activityList){
			actListJson.add(act.toJson());
		}
		json.add("activity_list",actListJson);
		return json;
	}

	public Activity updateOrInsert(Activity activity){
		int index = activityList.indexOf(activity);
		if(index == -1) {
			activityList.add(activity);
			Collections.sort(this.activityList);
			return activity;
		}else{
			Activity needUpdate = activityList.get(index);
			needUpdate.update(activity);
			Collections.sort(this.activityList);
			return needUpdate;
		}
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
	public ActivityRow update(IRow newRow) {
		int changedCount = 0 ;
		for(Activity newOne:((ActivityRow)newRow).getActivityList()){
			int index = activityList.indexOf(newOne);
			if(index >=0){
				Activity oldActivity = activityList.get(index);
				Long oldUpdtTime = oldActivity.getUpdateTime();
				Activity updatedActivity = activityList.get(index).update(newOne);
				if(oldUpdtTime < updatedActivity.getUpdateTime()){
					changedCount+=1;
				}
			}
		}
		if(changedCount > 0){
			return (ActivityRow)newRow;
		}
		return null;
	}

	@Override
	public String getId() {
		return owner;
	}

	public void notifyChange(){
//		String notifytStr = notifyJsonString();
//		NetworkLayer.getInstance().broadcastToServers(notifytStr, null);
	}

	public void notifyActivityChange(Activity activity){
		JsonObject json = activity.toJson();
		json.addProperty("owner",owner);
		json.addProperty("command",MessageType.ACTIVITY_UPDATE.name());
		NetworkLayer.getInstance().broadcastToServers(json.toString(), null);
	}

//	@Override
//	public void notifyChange(Connection connection) {
//		String notifytStr = notifyJsonString();
//		NetworkLayer.getInstance().broadcastToServers(notifytStr, connection);
//	}

//	private String notifyJsonString(){
//		JsonObject json = this.toJson();
//		json.addProperty("command", MessageType.ACTIVITY_UPDATE.name());
//		return json.toString();
//	}


}
