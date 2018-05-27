package activitystreamer.server.datalayer;

import java.util.HashMap;

/**
 * ITable
 * <p>
 * Author Ning Kang
 * Date 18/5/18
 */

public abstract class Table<T> {

//	protected abstract boolean lockRow(T row);
//	protected abstract boolean unlockRow(T row);
//	protected abstract boolean insert(T row);
	protected abstract T updateOrInsert(T row);
	protected abstract T delete(String id);
	protected abstract T selectById(String id);
	protected abstract HashMap<String, T> getAll();

}
