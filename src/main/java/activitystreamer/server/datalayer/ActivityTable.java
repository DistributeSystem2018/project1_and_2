package activitystreamer.server.datalayer;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * ServerTable
 * <p>
 * Author Ning Kang
 * Date 18/5/18
 */

public class ActivityTable extends Table<ActivityRow> implements Serializable {
	private HashMap<String, ActivityRow> activityMap;

	public ActivityTable() {
		this.activityMap = new HashMap<>();
	}

	@Override
	public ActivityRow updateOrInsert(ActivityRow newRow) {
		ActivityRow row = activityMap.get(newRow.getId());
		if (row != null) {
			row.update(row);
		} else {
			activityMap.put(newRow.getId(), newRow);
		}
		return newRow;
	}

	public void updateOrInsert(Activity activity) {
		Collection<UserRow> allUsers = DataLayer.getInstance().getAllUsers().values();
		for (UserRow user : allUsers) {
			if (user.isOnline()) {
				ActivityRow activityRow = activityMap.get(user.getUsername());
				if (activityRow == null) {
					ActivityRow newActivityRow = new ActivityRow(user.getUsername());
					newActivityRow.updateOrInsert(activity.copy());
					activityMap.put(user.getUsername(), newActivityRow);
				} else {
					activityRow.updateOrInsert(activity.copy());
				}
			}
		}
	}

	public Activity syncActivityForUser(String username, Activity newActivity) {
		ActivityRow oldRow = activityMap.get(username);
		Activity updatedRow;
		if(oldRow != null) {
			int index = oldRow.getActivityList().indexOf(newActivity);
			if (index >= 0) {
				updatedRow = oldRow.getActivityList().get(index).update(newActivity);
			} else {
				oldRow.getActivityList().add(newActivity.copy());
				updatedRow = newActivity;
			}

			return updatedRow;
		}else{
			ActivityRow activityRow = new ActivityRow(username);
			activityRow.getActivityList().add(newActivity.copy());
			activityMap.put(username,activityRow);
			return newActivity;
		}
	}

	@Override
	public ActivityRow delete(String id) {
		ActivityRow row = activityMap.get(id);
		activityMap.remove(id);
		return row;
	}

	@Override
	public ActivityRow selectById(String id) {
		return activityMap.get(id);
	}

	@Override
	public HashMap<String, ActivityRow> getAll() {
		return activityMap;
	}
}
