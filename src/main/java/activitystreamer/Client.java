package activitystreamer;

import activitystreamer.client.ClientSkeleton;
import activitystreamer.util.Settings;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.InetAddress;

public class Client {

	private static final Logger log = LogManager.getLogger();

	private static void help(Options options) {
		String header = "An ActivityStream Client for Unimelb COMP90015\n\n";
		String footer = "\ncontact aharwood@unimelb.edu.au for issues.";
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("ActivityStreamer.Client", header, options, footer, true);
		System.exit(-1);
	}

	public static void main(String[] args) throws IOException {

		log.info("reading command line options");

		Options options = new Options();


		options.addOption("u", true, "username");
		options.addOption("s", true, "secret for username");

		options.addOption("rp", true, "remote port number");
		options.addOption("rh", true, "remote hostname");

//		options.addOption("a", false, "anonymous login");
		// build the parser
		CommandLineParser parser = new DefaultParser();

		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e1) {
			help(options);
		}

//		if (cmd.hasOption("c")) {
		if (cmd.hasOption("rh")) {
			String remoteHostname = cmd.getOptionValue("rh");
			int timeOut = 3000;
			boolean status = InetAddress.getByName(remoteHostname).isReachable(timeOut);
			if (status) {
				log.debug("Set remote host to {}", remoteHostname);
				Settings.setRemoteHostname(remoteHostname);
			} else {
				log.error("The provided remote host name is not reachable, please try again");
				help(options);
			}
		}

		if (cmd.hasOption("rp")) {
			try {
				int port = Integer.parseInt(cmd.getOptionValue("rp"));
				log.debug("Set remote port to {}", port);
				Settings.setRemotePort(port);
			} catch (NumberFormatException e) {
				log.error("-rp requires a port number, parsed: " + cmd.getOptionValue("rp"));
				help(options);
			}
		}

		if (cmd.hasOption("s")) {
			Settings.setSecret(cmd.getOptionValue("s"));
		}

		if (cmd.hasOption("u")) {
			Settings.setUsername(cmd.getOptionValue("u"));
		}

		log.info("starting client");

		// user info
		if (cmd.hasOption("u")) {
			Settings.setUsername(cmd.getOptionValue("u"));
		}
		if (cmd.hasOption("s")) {
			Settings.setSecret(cmd.getOptionValue("s"));
		}


//		//Register
//		if (cmd.hasOption("r")) {
//			ClientSkeleton c = ClientSkeleton.getInstance();
//			c.sendRegisterMsg();
//		}
//
//		//anonymous login
//		if (cmd.hasOption("a")) {
//			Settings.setUsername("anonymous");
//			Settings.setSecret("");
//			ClientSkeleton c = ClientSkeleton.getInstance();
//			c.sendAnonymousLoginMsg();
//		}
//
//		if(cmd.hasOption('l')){
//			ClientSkeleton c = ClientSkeleton.getInstance();
//			c.sendLoginMsg();
//		}
		String username = Settings.getUsername();
		String secret = Settings.getSecret();

		// If anonymous login
		if (username == null || username.equals("anonymous")) {
			Settings.setUsername("anonymous");
			log.info("Username is 'anonymous', try to login as anonymous");
			ClientSkeleton c = ClientSkeleton.getInstance();
			c.sendAnonymousLoginMsg();

		}

		// if secret is null and username is not null
		else if (secret == null) {
			log.info("Username is provided [{}] but secret is not, try to register...", Settings.getUsername());
			secret = Settings.nextSecret();
			Settings.setSecret(secret);
			log.info("First generate the secret as: [{}]", secret);

			// write down the secret file for testing batch testing

			try {

				FileWriter writer = new FileWriter("secret_map.csv", true);
				writer.write(String.format("%s,%s%s",Settings.getUsername(),Settings.getSecret(),System.lineSeparator()));
				writer.close();
			} catch (IOException e) {
				log.error("Cannot create/updateOrInsert file 'secret_map.csv'.");
			}

			ClientSkeleton c = ClientSkeleton.getInstance();
			c.sendRegisterMsg();
		}else {

			// If none of above cases, the username and secret must be both provided, just try to login
			log.info("Both username({}) and secret({}) are provide, try to login...", username, secret);
			ClientSkeleton c = ClientSkeleton.getInstance();
			c.sendLoginMsg();
		}
	}


}
