package client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;



public class ChatClient extends JFrame implements ActionListener {

	private static Socket client;// 连接服务端的套接字
	
	private static BufferedReader reader;
	private static BufferedWriter writer;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// 左侧的窗口
	private JPanel leftPanel;
	private JPanel settingPanel;// 左侧参数配置窗口
	private JLabel nameLable;// 用户名标签
	private JTextField nameField; // 用户名
	private JLabel passLable;// 密码标签
	private JPasswordField passField;// 密码
	private JLabel ipLable;// IP标签
	private JTextField ipField; // IP
	private JLabel portLable;// 参数配置端口
	private JTextField portField;// 端口

	private JList<String> userList;// 左侧中间的userList
	private Vector<String> userNameVector;// 因为要刷新数据，所以要把用户名添加到vector中
	private JScrollPane userListPane;// userList放在ScrollPane中

	private JPanel leftButtonPanel;// 左侧下方的按钮放在JPanel中
	private JButton connect;// 连接按钮
	private JButton disconnect;// 关闭按钮

	// 右侧聊天窗口
	private JPanel chatPanel;
	private JScrollPane historyPane;// 历史纪录
	private JTextArea historyArea;// 历史纪录

	private JScrollPane contentPane;// 输入框
	private JTextArea contentArea;// 输入框

	private JPanel buttonPane;
	private JButton singleSendButton;// 单聊按钮
	private JButton mutiSendButtonl;// 多聊按钮

	// 指令，根据指令判断点击的是哪一个按钮
	private static final String CONNECT = "connect";
	private static final String DISCONNECT = "disconnect";
	private static final String SINGGLE_SEND = "single_send";
	private static final String MUTI_SEND = "muti_send";

	public ChatClient() {
		initialUI();// 初始化界面
		setListenerAndInstruct();// 给界面中的空间添加监听器和指令
	}

	public static void main(String[] args) {
		new ChatClient();
	}

	private void setListenerAndInstruct() {
		connect.addActionListener(this);
		disconnect.addActionListener(this);
		singleSendButton.addActionListener(this);
		mutiSendButtonl.addActionListener(this);
		connect.setActionCommand(CONNECT);
		disconnect.setActionCommand(DISCONNECT);
		singleSendButton.setActionCommand(SINGGLE_SEND);
		mutiSendButtonl.setActionCommand(MUTI_SEND);
	}

	private void initialUI() {
		// 参数配置窗口
		nameLable = new JLabel("用户名：");
		nameField = new JTextField();
		passLable = new JLabel("密码：");
		passField = new JPasswordField();
		ipLable = new JLabel("服务端IP地址：");
		ipField = new JTextField();
		portLable = new JLabel("端口号：");
		portField = new JTextField();
		settingPanel = new JPanel(new GridLayout(4, 2));// 四行两列
		settingPanel.setBorder(BorderFactory.createTitledBorder("参数配置"));
		settingPanel.add(nameLable);
		settingPanel.add(nameField);
		settingPanel.add(passLable);
		settingPanel.add(passField);
		settingPanel.add(ipLable);
		settingPanel.add(ipField);
		settingPanel.add(portLable);
		settingPanel.add(portField);

		// 在线用户列表窗口
		userList = new JList<>();
		userListPane = new JScrollPane(userList);
		userListPane.setBorder(BorderFactory.createTitledBorder("在线用户列表"));

		// ButtonPane
		connect = new JButton("连接");
		disconnect = new JButton("断开连接");
		disconnect.setEnabled(false);
		leftButtonPanel = new JPanel(new FlowLayout());
		leftButtonPanel.add(connect);
		leftButtonPanel.add(disconnect);

		// 往左侧布局中添加三个以上panel
		leftPanel = new JPanel(new BorderLayout());
		leftPanel.add(settingPanel, BorderLayout.NORTH);
		leftPanel.add(userListPane, BorderLayout.CENTER);
		leftPanel.add(leftButtonPanel, BorderLayout.SOUTH);

		// 历史纪录窗口
		historyArea = new JTextArea();
		historyArea.setEditable(false);
		historyArea.setPreferredSize(new Dimension(283, 283));

		historyPane = new JScrollPane(historyArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		historyPane.setBorder(BorderFactory.createTitledBorder("历史纪录"));

		// 输入框
		contentArea = new JTextArea();
		contentPane = new JScrollPane(contentArea);
		contentPane.setBorder(BorderFactory.createTitledBorder("输入消息框"));

		// 发送按钮Pane
		buttonPane = new JPanel(new FlowLayout());
		singleSendButton = new JButton("发送");
		mutiSendButtonl = new JButton("群发");
		buttonPane.add(singleSendButton);
		buttonPane.add(mutiSendButtonl);

		chatPanel = new JPanel(new BorderLayout());
		chatPanel.add(historyPane, BorderLayout.NORTH);
		chatPanel.add(contentPane, BorderLayout.CENTER);
		chatPanel.add(buttonPane, BorderLayout.SOUTH);

		getContentPane().setLayout(new GridLayout(1, 2, 20, 30));
		getContentPane().add(leftPanel);
		getContentPane().add(chatPanel);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 点击“x”关闭窗口
		pack(); // 调整窗口大小，以适应子组件的首选大小
		setVisible(true);
		setBounds(0, 0, 600, 500);
		setTitle("ChatClient");
		setResizable(false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
		case CONNECT:
			connectServer();
			userNameVector = new Vector<>();// 连接上服务器后，开始初始化vector
			break;
		case DISCONNECT:
			disconnectServer();
			break;
		case SINGGLE_SEND:
			singleSendMsg();
			break;
		case MUTI_SEND:
			mutiSend();
			break;
		default:
			break;
		}
	}

	private void mutiSend() {
		if (contentArea.getText().equals("") || contentArea.getText().toString() == null) {
			JOptionPane.showMessageDialog(null, "输入内容为空");
			return;
		}
		historyArea.append( "我对所有人说：" + contentArea.getText().toString() + "\n");
		try {
			//发送格式为  发送方名字+divide+发送内容
			writer.write(nameField.getText().toString()+Constants.SEND_TO_ALL+
					contentArea.getText().toString()
					+ "\n");// ip&content
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		contentArea.setText("");
	}

	private void singleSendMsg() {
		if (contentArea.getText().equals("") || contentArea.getText().toString() == null) {
			JOptionPane.showMessageDialog(null, "输入内容为空");
			return;
		}
		if(userList.getSelectedValue().split("/")[0].equals(nameField.getText().toString())){
			JOptionPane.showMessageDialog(null, "您不能给自己发送信息");
			return;
		}
		historyArea.append( "我对" + userList.getSelectedValue().toString().split("/")[0]
				+ "说：" + contentArea.getText().toString() + "\n");
		try {
			//发送格式为  发送方名字+divide+接收方姓名+divide+接收方IP+divide+发送内容
			writer.write(nameField.getText().toString()+Constants.SEND_CONTENT_TO_SERVER+userList.getSelectedValue().toString().split("/")[0]+
					Constants.SEND_CONTENT_TO_SERVER+userList.getSelectedValue().toString().split("/")[1]+Constants.SEND_CONTENT_TO_SERVER+
					contentArea.getText().toString()
					+ "\n");// IP&content
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		contentArea.setText("");
	}

	private void disconnectServer() {
		setSetting(true);
		try {
			writer.write(nameField.getText().toString()+",close");
			writer.write("\n");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void connectServer() {
		try {
			String ip = ipField.getText().toString();
			String port = portField.getText().toString();
			String userName = nameField.getText().toString();
			char[] passwordChar = passField.getPassword();
			String passwordString = String.copyValueOf(passwordChar);
			//检查是否为空
			if (checkNull(ip) || checkNull(port) || checkNull(userName) || checkNull(passwordString)) {
				JOptionPane.showMessageDialog(null, "连接失败，请检查您的配置信息");
				return;
			}
			if (!passwordString.equals("admin")) {
				JOptionPane.showMessageDialog(null, "密码错误");
				return;
			}
			// 验证完参数配置后即可连接服务器
			client = new Socket(ip, Integer.parseInt(port));
			reader = getBufferedReader();
			writer = getBufferedWriter();
			setTitle(nameField.getText().toString());
			sendNameToServer(userName +	Constants.SEND_NAME_IP_TO_SERVER + client.getLocalAddress());// 将用户名和ip发送到服务端
//			JOptionPane.showMessageDialog(null, "连接成功");
			setSetting(false);

			// 开启线程接收服务端转发的数据
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						String line;
						while ((line = reader.readLine()) != null) {
							if (line.contains(Constants.GET_CONTENT_FROM_SERVER)) {//服务端转发单聊的消息
								String fromUser = line.split(Constants.GET_CONTENT_FROM_SERVER)[0];
								String content =  line.split(Constants.GET_CONTENT_FROM_SERVER)[1];
								historyArea.append(fromUser+"说："+content+"\n");
							}else if(line.contains(Constants.GET_NAME_FROM_SERVER)){//服务器发送的在线用户
								updateUserList(line);
							}else if(line.contains(Constants.GET_TO_ALL)){//服务端转发群聊的消息
								String fromUser = line.split(Constants.GET_TO_ALL)[0];
								String content =  line.split(Constants.GET_TO_ALL)[1];
								historyArea.append(fromUser+"对所有人说："+content+"\n");
							}else if(line.contains(",close")){
								historyArea.append(line.split(",close")[0]+"下线啦！！！！\n");
							}
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();

		} catch (UnknownHostException e) {
			JOptionPane.showMessageDialog(null, "找不到此主机或但口号不存在，请修改后重试");
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "找不到此主机或端口号不存在，请修改后重试");
		}
	}

	/**
	 * 往服务端传递用户名
	 * 
	 * @param name
	 */
	private void sendNameToServer(String name_ip) {
		try {
			writer.write(name_ip);// readLine只有看见\n才算做是一行
			writer.write("\n");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void setSetting(boolean b) {
		connect.setEnabled(b);
		disconnect.setEnabled(!b);
		ipField.setEditable(b);
		portField.setEditable(b);
		nameField.setEditable(b);
		passField.setEditable(b);
	}

	/**
	 * 检查配置参数
	 */
	private boolean checkNull(String toCheck) {
		if (toCheck.equals("") || toCheck == null)
			return true;
		return false;
	}

	private void updateUserList(String line) {
		String userNameList[] = line.split(Constants.GET_NAME_FROM_SERVER);
		userNameVector.removeAllElements();
		for (String name : userNameList) {
			userNameVector.addElement(name);
		}
		userList.setModel(new DefaultComboBoxModel<>(userNameVector));
	}
	
	public static BufferedWriter getBufferedWriter(){
		if(writer==null){
			try {
				writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return writer;
	}
	
	public static BufferedReader getBufferedReader(){
		if(reader==null){
			try {
				reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return reader;
	}

}
