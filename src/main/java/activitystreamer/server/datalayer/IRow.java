package activitystreamer.server.datalayer;

import activitystreamer.server.networklayer.Connection;
import com.google.gson.JsonObject;

import java.io.Serializable;

/**
 * IRow
 * <p>
 * Author Ning Kang
 * Date 18/5/18
 */

public interface IRow {
	boolean lock();
	boolean unlock();
	IRow update(IRow row);
//	boolean update(JsonObject json);
	String getId();
	long getUpdateTime();
	void notifyChange();
//	void notifyChange(Connection connection);

}
