package activitystreamer.message.datasynchandlers;

import activitystreamer.message.MessageGenerator;
import activitystreamer.message.applicationhandlers.UserRegisterHandler;
import activitystreamer.server.application.Control;
import activitystreamer.server.datalayer.DataLayer;
import activitystreamer.server.datalayer.UserRow;
import activitystreamer.server.networklayer.Connection;
import activitystreamer.server.networklayer.NetworkLayer;

import static activitystreamer.message.datasynchandlers.BroadcastResult.LOCK_STATUS.USER_NOT_FOUND;

/**
 * BroadcastResult
 * <p>
 * Author Ning Kang
 * Date 14/4/18
 */

public class BroadcastResult {
	public enum LOCK_STATUS {
		PENDING,
		USER_FOUND,
		USER_NOT_FOUND
	}

	public enum REGISTER_RESULT {
		SUCC,
		FAIL,
		FAIL_UNDER_REGISTER,
		PROCESSING
	}

	//	private String serverIdentifier;
	private int enquiryServerCount;
	private int allowedServerCount;
	private int deniedServerCount;
	private Connection from;
	private UserRow user;

	public BroadcastResult(Connection c, int enqieryServerCount, UserRow u) {
		this.from = c;
		this.enquiryServerCount = enqieryServerCount;
		this.allowedServerCount = 0;
		deniedServerCount = 0;
		user = u;

	}

	public UserRow getUser() {
		return user;
	}

	public void addAllow() {
		this.allowedServerCount += 1;
//		return this.allowedServerCount == this.enquiryServerCount;
	}

	public void addDeny() {
		this.deniedServerCount += 1;
//		return this.deniedServerCount == this.enquiryServerCount;
	}

	public LOCK_STATUS getResult() {
		if (allowedServerCount + deniedServerCount != enquiryServerCount) {
			return LOCK_STATUS.PENDING;
		}
		if (deniedServerCount > 0) {
			return LOCK_STATUS.USER_FOUND;
		} else {
			return USER_NOT_FOUND;
		}

	}


	public Connection getFrom() {
		return from;
	}

	public static boolean processLock(LOCK_STATUS searchStatus, BroadcastResult lockRequest, UserRow u) {
		String username = u.getUsername();
		String secret = u.getSecret();
		switch (searchStatus) {
			case USER_NOT_FOUND:
				// if it is a REGISTER request reply
				Control.log.info("User [{}] registered successfully.", username);
				UserRegisterHandler.registerLockHashMap.remove(username);

				UserRow userRow = new UserRow(username, secret);
				DataLayer.getInstance().updateUserTable(DataLayer.OperationType.UPDATE_OR_INSERT, userRow, true);

				String resultStr = MessageGenerator.registerResult(REGISTER_RESULT.SUCC, username, secret);
				Control.getInstance().process(lockRequest.from, resultStr);
				break;
			case USER_FOUND:
				Control.log.info("User [{}] exists in this system, register failed.", username);
				UserRegisterHandler.registerLockHashMap.remove(username);

				lockRequest.getFrom().closeCon();
				NetworkLayer.getInstance().connectionClosed(lockRequest.getFrom());

				resultStr = MessageGenerator.registerResult(REGISTER_RESULT.FAIL, username, secret);
				NetworkLayer.getInstance().broadcastToServers(resultStr, null);
				Control.getInstance().process(lockRequest.from, resultStr);

				break;
			default:
				Control.log.error("BroadResult handler should not run to here !!");
				break;
		}
		return true;
	}
}
