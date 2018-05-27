package activitystreamer.server.networklayer;

/**
 * BackupServerInfo
 * <p>
 * Author Ning Kang
 * Date 5/5/18
 */

public class BackupServerInfo {
	private String serverId;
	private String host;
	private int prot;

	public BackupServerInfo(String serverId,String host, int prot) {
		this.serverId = serverId;
		this.host = host;
		this.prot = prot;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getProt() {
		return prot;
	}

	public void setProt(int prot) {
		this.prot = prot;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}
}
