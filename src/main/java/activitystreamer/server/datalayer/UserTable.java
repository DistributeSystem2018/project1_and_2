package activitystreamer.server.datalayer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * UserTable
 * <p>
 * Author Ning Kang
 * Date 18/5/18
 */

public class UserTable extends Table<UserRow> implements Serializable {
	private HashMap<String,UserRow> userList;

	public UserTable() {
		userList = new HashMap<>();
	}

//	@Override
//	public boolean lockRow(UserRow row) {
//		return false;
//	}
//
//	@Override
//	public boolean unlockRow(UserRow row) {
//		return false;
//	}
//
//	@Override
//	public boolean insert(UserRow row) {
//		return false;
//	}

	@Override
	protected UserRow updateOrInsert(UserRow row) {
		UserRow existRow = userList.get(row.getUsername());
		if(existRow == null){
			userList.put(row.getUsername(),row);
		}else{
			existRow.update(row);
		}
		return row;
	}

	@Override
	protected UserRow delete(String id) {
		UserRow row = userList.get(id);
		userList.remove(id);
		return row;
	}

	@Override
	protected UserRow selectById(String id) {
		return userList.get(id);
	}

	@Override
	protected HashMap<String, UserRow> getAll() {
		return userList;
	}


//	protected void markUserOnline(String username,boolean online){
//		userList.get(username).login(online);
//	}

	protected ArrayList<UserRow> connectedUserList(){
		ArrayList<UserRow> connectUsers = new ArrayList<>();
		for(UserRow user:userList.values()){
			if(user.isOnline()){
				connectUsers.add(user);
			}
		}
		return connectUsers;
	}
}
