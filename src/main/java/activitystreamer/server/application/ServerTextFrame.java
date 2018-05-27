package activitystreamer.server.application;

import activitystreamer.UIPanelCreator;
import activitystreamer.server.datalayer.ServerRow;
import activitystreamer.server.datalayer.UserRow;
import activitystreamer.server.networklayer.Connection;
import activitystreamer.util.Settings;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;


@SuppressWarnings("serial")
public class ServerTextFrame extends JFrame implements ActionListener {

	private DefaultTableModel registeredUserArea;
	private DefaultTableModel loginUserArea;
	private DefaultTableModel serverArea;
	private DefaultTableModel loadArea;
	private JTextArea logText;


	public ServerTextFrame() {
		setTitle(String.format("Server:(%s:%d)", Settings.getLocalHostname(), Settings.getLocalPort()));
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(2, 1));

		JPanel upPanel = new JPanel();
		upPanel.setLayout(new GridLayout(2, 2));
		mainPanel.add(upPanel);

		registeredUserArea = UIPanelCreator.addTablePanel(upPanel, "Registered User List", new String[]{"Username", "Secret"});
		loginUserArea = UIPanelCreator.addTablePanel(upPanel, "Online User List", new String[]{"Username", "Secret"});
		serverArea = UIPanelCreator.addTablePanel(upPanel, "Neighbor  Servers", new String[]{"Host", "Port"});
		loadArea = UIPanelCreator.addTablePanel(upPanel, "Server Loads", new String[]{"IP", "Port", "Load", "Update Time"});

		logText = UIPanelCreator.addTextPanel(mainPanel, "Log");

		add(mainPanel);
		setLocationRelativeTo(null);
		setSize(640, 640);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setVisible(true);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}


	public void setLoginUserArea(ArrayList<UserRow> userList) {
		loginUserArea.setRowCount(0);
		for (UserRow u : userList) {
			try {
				loginUserArea.addRow(new String[]{u.getUsername(), u.getSecret()});
			} catch (Exception e) {
				Control.log.error("Error login area username:[{}], secret:[{}]", u.getUsername(), u.getSecret());
//				System.exit(-1);
				e.printStackTrace();
			}
		}
	}

	public void setRegisteredArea(Collection<UserRow> registerUsers) {
		registeredUserArea.setRowCount(0);
		for (UserRow u : registerUsers) {
			try {
				registeredUserArea.addRow(new String[]{u.getUsername(), u.getSecret()});
			} catch (Exception e) {
				Control.log.error("Error regisetr area username:[{}], secret:[{}]", u.getUsername(), u.getSecret());
//				System.exit(-1);
				e.printStackTrace();
			}
		}
	}

	public void setServerArea(ArrayList<Connection> connections) {
		this.serverArea.setRowCount(0);
		for (Connection c : connections) {
			if (c.isAuthedServer()) {
				String host = c.getRemoteServerHost();
				String port = Integer.toString(c.getRemoteServerPort());
				try {
					this.serverArea.addRow(new String[]{host, port});
				} catch (Exception e) {
					e.printStackTrace();
					Control.log.error("Error connection area host:[{}], port:[{}]", host, port);
//					System.exit(-1);
				}
			}
		}
	}

	public void setLoadArea(Collection<ServerRow> serverStates) {
		loadArea.setRowCount(0);
		for (ServerRow ss : serverStates) {
			if (ss.isOnline()) {
				String host = ss.getIp();
				String port = Integer.toString(ss.getPort());
				String load = Integer.toString(ss.getLoad());
				String time = ss.getUpdateTimeString();
				try {
					loadArea.addRow(new String[]{host, port, load, time});
				} catch (Exception e) {
					Control.log.error("Error server load area host:[{}], port:[{}]", host, port);
//					System.exit(-1);
					e.printStackTrace();
				}

			}
		}
	}


	//show error message
	public void showErrorMsg(String error) {
		JOptionPane.showMessageDialog(null, error, "Error", JOptionPane.INFORMATION_MESSAGE);
	}

	public JTextArea getLogArea() {
		return logText;
	}

	public void actionPerformed(ActionEvent e) {

	}


}
