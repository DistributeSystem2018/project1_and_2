package activitystreamer.server.networklayer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import activitystreamer.server.application.Control;
import org.apache.logging.log4j.Logger;

import activitystreamer.util.Settings;

public class Listener extends Thread{
	private static final Logger log = Control.log;
	private ServerSocket serverSocket=null;
	private boolean term = false;
	private int portnum;
	
	public Listener() throws IOException{
		portnum = Settings.getLocalPort(); // keep our own copy in case it changes later
		serverSocket = new ServerSocket(portnum);
		start();
	}
	
	@Override
	public void run() {
		log.info("listening for new connections on "+portnum);
		while(!term){
			Socket clientSocket;
			try {
				clientSocket = serverSocket.accept();
				NetworkLayer.getInstance().incomingConnection(clientSocket);
			} catch (IOException e) {
				log.info("received exception, shutting down");
				term=true;
			}
		}
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setTerm(boolean term) {
		this.term = term;
	}

	public String getSocketAdr(){
		return serverSocket.getInetAddress()+":"+serverSocket.getLocalPort();
	}
	
	
}
