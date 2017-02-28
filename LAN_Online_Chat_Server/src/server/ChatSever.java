package server;

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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class ChatSever extends JFrame implements ActionListener {

	// 序列化和反序列化的标识
	private static final long serialVersionUID = 3262626856260260913L;

	private ArrayList<Socket> sockets;

	private Vector<String> userNameList;

	private BufferedReader reader;
	private BufferedWriter writer;

	// 声明控件
	private JList<String> userList; // 在线用户列表
	private JScrollPane userListPane; // 将userList放到ScrollPane中
	public JTextArea messageTextArea; // 聊天消息框
	private JScrollPane messageTextPane;// 将聊天消息框放到ScrollPane中
	private JPanel topPanel; // 包含userList和message
	private JButton startServer; // 开启服务器的按钮
	private JButton stopServer; // 关闭服务器的按钮
	private JPanel buttonPanel; // 将按钮放到Panel中
	private JTextArea intro;
	private JPanel mySelf;

	// 声明指令
	private static final String START = "start";
	private static final String STOP = "stop";

	private ServerSocket server;

	public ChatSever() {
		initUI();
		setListenerAndInstruct();
	}

	public static void main(String[] args) {
		new ChatSever();
	}

	/**
	 * 为控件添加监听器
	 */
	private void setListenerAndInstruct() {
		startServer.addActionListener(this);
		stopServer.addActionListener(this);
		startServer.setActionCommand(START);
		stopServer.setActionCommand(STOP);
	}

	/**
	 * 初始化服务端UI
	 */
	private void initUI() {

		// 左边的userList
		userList = new JList<>();// 实例化userList
		userListPane = new JScrollPane();// 将userList放到ScrollPane中
		userListPane.getViewport().setView(userList);
		userListPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		userListPane.setOpaque(false); // 不绘制内部元素
		userListPane.setPreferredSize(new Dimension(200, 130));
		userListPane.setBorder(BorderFactory.createTitledBorder("online user list:")); // 给userListPane添加边界title

		// 右边的message窗口
		messageTextArea = new JTextArea();// 实例化消息框
		messageTextArea.setPreferredSize(new Dimension(400, 200));
		messageTextArea.setEditable(false);
		messageTextPane = new JScrollPane(messageTextArea);// 将消息框放到ScollPane中
		messageTextPane.setOpaque(false);// 不绘制内部元素
		messageTextPane.setBorder(BorderFactory.createTitledBorder("Message:"));// 给messageTextPane添加边界title

		// TopPanel
		topPanel = new JPanel(new GridLayout(1, 2, 10, 2));// 一行两列
		topPanel.add(userListPane);
		topPanel.add(messageTextPane);

		// 下方的ButtonPanel
		startServer = new JButton("start");// 实例化开启按钮
		startServer.setEnabled(true);
		stopServer = new JButton("stop");// 实例化关闭按钮
		stopServer.setEnabled(false);
		buttonPanel = new JPanel(new FlowLayout());// 设置流式布局，默认居中对齐
		buttonPanel.add(startServer);
		buttonPanel.add(stopServer);

		// 介绍
		intro = new JTextArea("一、原理：\n" + "    基于Socket通信方式的局域网聊天系统，以C/S模型实现。\n\n" + "二、功能：\n" + "    1.服务端\n"
				+ "        1.1客户端连接，动态刷新线上用户列表\n" + "        1.2获取客户端数据，在MessageTextArea显示出来\n" + "        1.3数据转发\n\n"
				+ "    2.客户端:\n" + "        2.1单聊\n" + "        2.2群聊\n\n" + "三、实现步骤\n" + "    1.搭建UI框架\n"
				+ "        1.1搭建基本的UI框架\n" + "        1.2为基本控件添加事件\n\n" + "    2.服务端逻辑编码\n"
				+ "        2.1建立服务端Socket，开启程序，等待客户端连接\n" + "            PS：因为是阻塞的方法，所以要开启线程来无限的接受客户端的连接\n"
				+ "        2.2客户端连接后，开启一个线程，用于接收客户端数据，并转发数据\n\n" + "    3.客户端逻辑编码\n"
				+ "        3.1建立客户端Socket，开启程序，配置参数，连接服务端\n" + "        3.2开启线程，接受服务器转发的消息，并处理\n" + "        3.3发送消息");
		intro.setOpaque(false);
		intro.setEditable(false);
		mySelf = new JPanel();
		mySelf.add(intro);

		// 添加几个主要的布局部分
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(topPanel, BorderLayout.NORTH);
		getContentPane().add(mySelf, BorderLayout.WEST);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 点击“x”关闭窗口
		pack(); // 调整窗口大小，以适应子组件的首选大小
		setVisible(true);
		setTitle("ChatServer");
		// setBounds(0, 0, 600, 700);
		setResizable(false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
		case START:
			// JOptionPane.showMessageDialog(null, "点击了start");
			startServer();// 开启服务器
			break;
		case STOP:
			// JOptionPane.showMessageDialog(null, "点击了stop");
			stopServer();// 关闭服务器
			break;
		default:
			break;
		}

	}

	/**
	 * 开启服务器
	 */
	private void stopServer() {
		// 开启后将开启按钮设置为可用，关闭按钮设置为不可用
		startServer.setEnabled(true);
		stopServer.setEnabled(false);
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 关闭服务器
	 */
	private void startServer() {
		// 开启后将开启按钮设置为不可用，关闭按钮设置为可用
		startServer.setEnabled(false);
		stopServer.setEnabled(true);
		userNameList = new Vector<>();
		sockets = new ArrayList<>();
		/**
		 * 开启服务端线程，无限接受客户端请求
		 */
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					server = new ServerSocket(Constants.SERVER_PORT);
					JOptionPane.showMessageDialog(null, "服务器在端口号为" + Constants.SERVER_PORT + "的端口开启");
//					int num = 0;
					while (true) {
						Socket client = server.accept();
						reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
						writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
						sockets.add(client);// 将客户端的socket添加到集合中
						System.out.println("IP地址为" + client.getInetAddress().getHostAddress() + "的客户端已连接");
						// 开启线程与client通信
						new TransMsg(sockets, client).start();
					}
				} catch (Exception e) {

				}
			}
		}).start();
	}

	class TransMsg extends Thread {
		private Socket client;
		private ArrayList<Socket> sockets;

		public TransMsg(ArrayList<Socket> sockets, Socket client) {
			this.sockets = sockets;
			this.client = client;
		}

		@SuppressWarnings("deprecation")
		@Override
		public void run() {
//			System.out.println("run");
			try {
				 BufferedReader reader = new BufferedReader(new
				 InputStreamReader(client.getInputStream()));
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.contains(Constants.GET_CONTENT_FROM_CLIENT)) {
						String ip = line.split(Constants.GET_CONTENT_FROM_CLIENT)[2];
						for (Socket s : sockets) {
							if (s.getInetAddress().getHostAddress().equals(ip)) {
								BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
								// 转发格式为 发送人姓名+divide+发送内容
								writer.write(line.split(Constants.GET_CONTENT_FROM_CLIENT)[0]
										+ Constants.SEND_CONTENT_TO_CLIENT
										+ line.split(Constants.GET_CONTENT_FROM_CLIENT)[3]);
								writer.write("\n");
								writer.flush();
							}
						}
						messageTextArea.append(line.split(Constants.GET_CONTENT_FROM_CLIENT)[0] + "对"
								+ line.split(Constants.GET_CONTENT_FROM_CLIENT)[1] + "说："
								+ line.split(Constants.GET_CONTENT_FROM_CLIENT)[3] + "\n");
					} else if (line.contains(Constants.GET_TO_ALL)) {
						for (Socket s : sockets) {
							if (!s.getInetAddress().getHostAddress().equals(client.getInetAddress().getHostAddress())) {
								BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
								// 转发格式为 发送人姓名+divide+发送内容
								writer.write(line.split(Constants.GET_TO_ALL)[0] + Constants.SEND_TO_ALL
										+ line.split(Constants.GET_TO_ALL)[1]);
								writer.write("\n");
								writer.flush();
							}
						}
						messageTextArea.append(line.split(Constants.GET_TO_ALL)[0] + "对所有人说："
								+ line.split(Constants.GET_TO_ALL)[1] + "\n");
					} else if (line.contains(Constants.GET_NAME_IP_FROM_CLIENT)) {
						if (!userNameList.contains(line.split(Constants.GET_NAME_IP_FROM_CLIENT)[0]
								+ line.split(Constants.GET_NAME_IP_FROM_CLIENT)[1])) {
							userNameList.addElement(line.split(Constants.GET_NAME_IP_FROM_CLIENT)[0]
									+ line.split(Constants.GET_NAME_IP_FROM_CLIENT)[1]);
						}
						userList.setModel(new DefaultComboBoxModel<>(userNameList));
						updateUserList();
					} else if (line.contains(",close")) {
						 sockets.remove(client);
						 for(int i=0;i<userNameList.size();i++){
							 if(userNameList.get(i).toString().contains(line.split(",close")[0])){
								 userNameList.removeElement(userNameList.get(i));
							 }
						 }
						 userList.setModel(new DefaultComboBoxModel<>(userNameList));
						 updateUserList();
						messageTextArea.append(line.split(",close")[0] + "断开连接\n");
						for (Socket s : sockets) {
							BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
							// 转发格式为 发送人姓名+divide+发送内容
							writer.write(line.split(",close")[0]
									+ ",close");
							writer.write("\n");
							writer.flush();
						}
						 this.stop();
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void updateUserList() {
		// 通知客户端哪些用户上线了
		for (Socket s : sockets) {
			StringBuilder buider = new StringBuilder();
			BufferedWriter writer;
			try {
				writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
				for (String name_ip : userNameList) {
					buider.append(Constants.SEND_NAME_TO_CLIENT + name_ip);
				}
				writer.write(buider.toString() + "\n");
				writer.newLine();
				writer.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
