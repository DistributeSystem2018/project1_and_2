package activitystreamer.client;

import activitystreamer.server.networklayer.BackupServerInfo;
import activitystreamer.UIPanelCreator;
import activitystreamer.util.Settings;
import com.google.gson.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;



public class ClientTextFrame extends JFrame implements ActionListener {
	private static final Logger log = LogManager.getLogger("clientLogger");
	private JTextArea inputText;
	private JTextArea activityOutputText;
	private DefaultTableModel backupServers;
	private JTextArea serverMsgOutputText;
	//	private JTextArea logText;
	private JButton sendButton;
	private JButton disconnectButton;
	private JsonParser parser = new JsonParser();

	public ClientTextFrame() {

		setTitle(String.format("User:(%s) | Server:(%s:%s)", Settings.getUsername(),Settings.getRemoteHostname(),Settings.getRemotePort()));

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(2, 2));

		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new BorderLayout());
		Border lineBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.lightGray), "");
		inputPanel.setBorder(lineBorder);


		// updateOrInsert output panel
		activityOutputText = UIPanelCreator.addTextPanel(mainPanel, "JSON output, received from server");
//		activityOutputText.setEnabled(false);
		activityOutputText.setEditable(false);
		activityOutputText.setBackground(Color.lightGray);

		// updateOrInsert backup server list panel
		backupServers = UIPanelCreator.addTablePanel(mainPanel,"Backup Servers",new String[]{"host","port"});

		// updateOrInsert input panel
		inputText = UIPanelCreator.addTextPanel(inputPanel,"JSON input, to send to server");
		JPanel buttonGroup = new JPanel();
		sendButton = new JButton("Send");
		disconnectButton = new JButton("Disconnect");
		buttonGroup.add(sendButton);
		buttonGroup.add(disconnectButton);
		inputPanel.add(buttonGroup, BorderLayout.SOUTH);
		sendButton.addActionListener(this);
		disconnectButton.addActionListener(this);
		mainPanel.add(inputPanel);

		// Add a panel to show all other message from server exclude ACTIVITY message
		serverMsgOutputText = UIPanelCreator.addTextPanel(mainPanel, "Messages received from server");
		serverMsgOutputText.setEditable(false);
		serverMsgOutputText.setBackground(Color.lightGray);

		// updateOrInsert main panel to frame
		add(mainPanel);
		setLocationRelativeTo(null);
		setSize(640, 384);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				ClientSkeleton.getInstance().sendLogoutMsg();
			}
		});
	}

	private String prettyJsonString(final JsonObject obj){
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(obj.toString());
		return gson.toJson(je);
	}
	public void appendActivityPanel(final JsonObject obj) {

		appendActivityPanel(prettyJsonString(obj));
	}

	public void appendActivityPanel(String info) {
		activityOutputText.append('\n'+info);
		activityOutputText.revalidate();
		activityOutputText.repaint();
	}


	public void appendServerMsgPanel(final JsonObject obj) {
		appendServerMsgPanel(prettyJsonString(obj));
	}

	public void appendServerMsgPanel(String info) {
		serverMsgOutputText.append('\n'+info);
		serverMsgOutputText.revalidate();
		serverMsgOutputText.repaint();
	}

	//show error message
	public void showErrorMsg(String error) {
		JOptionPane.showMessageDialog(null, error, "Error", JOptionPane.INFORMATION_MESSAGE);
	}


	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == sendButton) {
			String msg = inputText.getText().trim().replaceAll("\r", " ").replaceAll("\n", " ").replaceAll("\t", " ");
			JsonObject json;
			try {
				json = (JsonObject) parser.parse(msg);
				ClientSkeleton.getInstance().sendActivityObject(json);
			} catch (ClassCastException | JsonSyntaxException e1) {
				String error = String.format("Data not sent as the string you input is not a valid json string: %s", msg);
				log.error(error);
				showErrorMsg(error);
			}

		} else if (e.getSource() == disconnectButton) {
			ClientSkeleton.getInstance().sendLogoutMsg();
			ClientSkeleton.getInstance().disconnect();
		}
	}

	public void setBackupPanel(ArrayList<BackupServerInfo> serverInfos) {
		backupServers.setRowCount(0);
		for(BackupServerInfo sInfo:serverInfos) {
			String host = sInfo.getHost();
			String port = Integer.toString(sInfo.getProt());
			backupServers.addRow(new String[]{host,port});
		}
	}
}
