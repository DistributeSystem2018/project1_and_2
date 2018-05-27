package activitystreamer.util;

import java.math.BigInteger;
import java.net.Socket;
import java.security.SecureRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Settings {
	private static final Logger log = LogManager.getLogger();
	private static SecureRandom random = new SecureRandom();
	private static int localPort = 3780;
	private static String localHostname = "localhost";
	private static String remoteHostname = null;
	private static int remotePort = 3780;
	private static int announceInterval = 5000; // milliseconds
	private static int activityCheckInterval = 500; // milliseconds
	private static int timeBeforeReconnect = 0; // milliseconds
	private static int syncInterval = 5000; // milliseconds
	private static String secret = null;
	private static String username = "anonymous";
	private static String serverId=null;
	private static final String TIME_FORMAT = "yyyy-mm-dd hh:mm:ss";

	
	public static int getLocalPort() {
		return localPort;
	}

	public static void setLocalPort(int localPort) {
		if(localPort<0 || localPort>65535){
			log.error("supplied port "+localPort+" is out of range, using "+getLocalPort());
		} else {
			Settings.localPort = localPort;
		}
	}

	public static String getTimeFormat() {
		return TIME_FORMAT;
	}

	public static int getRemotePort() {
		return remotePort;
	}

	public static void setRemotePort(int remotePort) {
		if(remotePort<0 || remotePort>65535){
			log.error("supplied port "+remotePort+" is out of range, using "+getRemotePort());
		} else {
			Settings.remotePort = remotePort;
		}
	}
	//setLoadBalancerPort, setLoadBalancerHostname, getLoadBalancerPort, getLoadBalancerhostname
	public static String getRemoteHostname() {
		return remoteHostname;
	}

	public static void setRemoteHostname(String remoteHostname) {
		Settings.remoteHostname = remoteHostname;
	}
	
	public static int getAnnounceInterval() {
		return announceInterval;
	}


	public static void setAnnounceInterval(int announceInterval) {
		Settings.announceInterval = announceInterval;
	}

	public static int getActivityCheckInterval() {
		return activityCheckInterval;
	}

	public static void setActivityCheckInterval(int activityCheckInterval) {
		Settings.activityCheckInterval = activityCheckInterval;
	}

	public static int getTimeBeforeReconnect() {
		return timeBeforeReconnect;
	}

	public static void setTimeBeforeReconnect(int timeBeforeReconnect) {
		Settings.timeBeforeReconnect = timeBeforeReconnect;
	}

	public static int getSyncInterval() {
		return syncInterval;
	}

	public static void setSyncInterval(int syncInterval) {
		Settings.syncInterval = syncInterval;
	}




	public static String getSecret() {
		return secret;
	}

	public static void setSecret(String s) {
		secret = s;
	}
	
	public static String getUsername() {
		return username;
	}

	public static void setUsername(String username) {
		Settings.username = username;
	}
	
	public static String getLocalHostname() {
		return localHostname;
	}

	public static void setLocalHostname(String localHostname) {
		Settings.localHostname = localHostname;
	}

	public static String getServerId() {
		return serverId;
	}

	public static void setServerId(String s) {
		serverId = s;
	}
	
	/*
	 * some general helper functions
	 */
	
	public static String socketAddress(Socket socket){
		return socket.getInetAddress()+":"+socket.getPort();
	}

	public static String nextSecret() {
	    return new BigInteger(130, random).toString(32);
	 }



	
}
