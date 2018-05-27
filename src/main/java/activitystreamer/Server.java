package activitystreamer;


import activitystreamer.server.application.Control;
import activitystreamer.server.datalayer.DataLayer;
import activitystreamer.server.networklayer.NetworkLayer;
import activitystreamer.util.Settings;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Server {
	private static final Logger log = LogManager.getLogger();

	private static void help(Options options) {
		String header = "An ActivityStream Server for Unimelb COMP90015\n\n";
		String footer = "\ncontact aharwood@unimelb.edu.au for issues.";
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("ActivityStreamer.Server", header, options, footer, true);
		System.exit(-1);
	}

	public static void main(String[] args) {

		log.info("reading command line options");

		Options options = new Options();

		options.addOption("lh", true, "local hostname");
		options.addOption("lp", true, "local port number");

		options.addOption("rp", true, "remote port number");
		options.addOption("rh", true, "remote hostname");

		options.addOption("a", true, "announce interval in milliseconds");
		options.addOption("s", true, "secret for the server to use");

		options.addOption("time_before_reconnect", true, "Provide the time (in milliseconds, 0 by default) to wait before reconnect if a server crashes, mainly for testing eventually consistancy");
		options.addOption("activity_check_interval", true, "Provide the interval (in milliseconds, 1000 by default) to check whether there is new activity coming in.");
		options.addOption("sync_interval", true, "Provide the interval (in milliseconds, 5000 by default) to sync data amoung servers.");

		// build the parser
		CommandLineParser parser = new DefaultParser();

		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e1) {
			help(options);
		}

		/*======================================= local information =======================================*/
		if (cmd.hasOption("lh")) {
			Settings.setLocalHostname(cmd.getOptionValue("lh"));
			try {
				Settings.setLocalHostname(InetAddress.getLocalHost().getHostAddress());
			} catch (UnknownHostException e) {
				log.warn("failed to get localhost IP address");
			}
		}

		if (cmd.hasOption("lp")) {
			int port = praseInt(cmd.getOptionValue("lp"), "-lp requires a port number");
			Settings.setLocalPort(port);
		}



		/*======================================= remote information =======================================*/
		if (cmd.hasOption("rp")) {
			int port = praseInt(cmd.getOptionValue("rp"), "-rp requires a port number");
			Settings.setRemotePort(port);

		}

		if (cmd.hasOption("rh")) {
			String remoteHostName = cmd.getOptionValue("rh");
			Settings.setRemoteHostname(remoteHostName);
			try {
				Settings.setRemoteHostname(InetAddress.getByName(remoteHostName).getHostAddress());
			} catch (UnknownHostException e) {
				log.warn("failed to get remote IP address");
			}
		} else if (!cmd.hasOption("s")) {
			Settings.setSecret(Settings.nextSecret());
			log.info("Secret for the system: " + Settings.getSecret());
		}


		/*======================================= interval information =======================================*/
		if (cmd.hasOption("a")) {
			int a = praseInt(cmd.getOptionValue("a"), "-a requires a number in milliseconds");
			Settings.setAnnounceInterval(a);
		}

		if (cmd.hasOption("time_before_reconnect")) {
			int reconnectInterval = praseInt(cmd.getOptionValue("time_before_reconnect"),"-time_before_reconnect requires a number in milliseconds");
			Settings.setTimeBeforeReconnect(reconnectInterval);
			log.info("TimeBeforeReconnect for the system: " + Settings.getTimeBeforeReconnect());
		}

		if (cmd.hasOption("activity_check_interval")) {
			int activityInterval = praseInt(cmd.getOptionValue("activity_check_interval"),"-activity_check_interval requires a number in milliseconds");
			Settings.setActivityCheckInterval(activityInterval);
			log.info("activity_check_interval for the system: " + Settings.getActivityCheckInterval());
		}

		if (cmd.hasOption("sync_interval")) {
			int activityInterval = praseInt(cmd.getOptionValue("sync_interval"),"-sync_interval requires a number in milliseconds");
			Settings.setSyncInterval(activityInterval);
			log.info("sync_interval for the system: " + Settings.getSyncInterval());
		}




		/*======================================= system secret setting =======================================*/
		if (cmd.hasOption("s") && !cmd.hasOption("rh") && !cmd.hasOption("rp")) {
			Settings.setSecret(cmd.getOptionValue("s"));
			log.info("Secret for the system: " + Settings.getSecret());
		}

		if (!cmd.hasOption("s") && !cmd.hasOption("rh") && !cmd.hasOption("rp")) {
			log.info("secret for the first server is not provided, generate one automatically");
			Settings.setSecret(Settings.nextSecret());
			log.info("Generated secret: [{}]: ", Settings.getSecret());
		}

		if (!cmd.hasOption("s") && cmd.hasOption("rh") && cmd.hasOption("rp")) {
			log.info("Secret must be provided to join to an existing server");
		} else {
			String secret = cmd.getOptionValue("s");
			log.info("Set secret to [{}]", secret);
			Settings.setSecret(secret);
		}

		log.info("starting server");

		Settings.setServerId(Settings.nextSecret());

		final Control c = Control.getInstance();


		// the following shutdown hook doesn't really work, it doesn't give us enough time to
		// cleanup all of our connections before the jvm is terminated.
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
//				c.setTerm(true);
//				c.interrupt();
//				DataLayer.getInstance().setTerm(true);
//				NetworkLayer.getInstance().setTerm(true);
			}
		});
	}

	private static int praseInt(String intString, String errorMsg) {
		try {
			int a = Integer.parseInt(intString);
			return a;
		} catch (NumberFormatException e) {
			log.error(errorMsg + ", provide:" + intString);
			System.exit(-1);
		}
		return -1;
	}
}
