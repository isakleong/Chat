
package com.chat;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.chat.Network.ChatMessage;
import com.chat.Network.ImageMessage;
import com.chat.Network.RegisterName;
import com.chat.Network.UpdateNames;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import java.util.ArrayList;
import java.util.List;

public class ChatClient {
	ChatFrame chatFrame;
	Client client;
	String name;
        static public int portal;
        public int no_port;
        List<ChatClient> listofClient = new ArrayList<ChatClient>();
	public ChatClient () {
		client = new Client();
		client.start();
		// For consistency, the classes to be sent over the network are
		// registered by the same method for both the client and server.
		Network.register(client);
                
		client.addListener(new Listener() {
			public void connected (Connection connection) {
				RegisterName registerName = new RegisterName();
				registerName.name = name;
				client.sendTCP(registerName);
			}

			public void received (Connection connection, Object object) {
//                             Object[] myArray = (Object[]) object;
				if (object instanceof UpdateNames) {
					UpdateNames updateNames = (UpdateNames)object;
					chatFrame.setNames(updateNames.names);
					return;
				}

				if (object instanceof ChatMessage) {
					ChatMessage chatMessage = (ChatMessage)object;
					chatFrame.addMessage(chatMessage.text);
					return;
				}
				if (object instanceof ImageMessage) {
					ImageMessage chatMessage = (ImageMessage)object;
					chatFrame.addPicture(chatMessage.image);
					return;
				}
			}

			public void disconnected (Connection connection) {
				EventQueue.invokeLater(new Runnable() {
					public void run () {
						// Closing the frame calls the close listener which will stop the client's update thread.
						chatFrame.dispose();
					}
				});
			}
		});

//		// Request the host from the user.
//		String input = (String)JOptionPane.showInputDialog(null, "Host:", "Connect to chat server", JOptionPane.QUESTION_MESSAGE,
//			null, null, "localhost");
//		if (input == null || input.trim().length() == 0) System.exit(1);
//		final String host = input.trim();
//
//		// Request the user's name.
//		input = (String)JOptionPane.showInputDialog(null, "Name:", "Connect to chat server", JOptionPane.QUESTION_MESSAGE, null,
//			null, "Test");
//		if (input == null || input.trim().length() == 0) System.exit(1);
//		name = input.trim();

		// All the ugly Swing stuff is hidden in ChatFrame so it doesn't clutter the KryoNet example code.
//                chatFrame = new ChatFrame(ChatFram.jTextField1.getText());
                chatFrame = new ChatFrame("localhost");
		// This listener is called when the send button is clicked.
		chatFrame.setSendListener(new Runnable() {
			public void run () {
				ChatMessage chatMessage = new ChatMessage();
				chatMessage.text = chatFrame.getSendText();
				client.sendTCP(chatMessage);
			}
		});
		chatFrame.setPicturelistener(new Runnable() {
			public void run () {
				ImageMessage image = new ImageMessage();
				image.image = chatFrame.getImage();
				client.sendTCP(image);
			}
		});
		// This listener is called when the chat window is closed.
		chatFrame.setCloseListener(new Runnable() {
			public void run () {
				client.stop();
			}
		});
		chatFrame.setVisible(true);
                //String text = ServerSf.jTextField1.getText();
//               final String host = ChatFram.jTextField1.getText();
                 final String host = "localhost";
                 name = Login.jTextField1.getText();
                //Network.setPort(no_port);
		// We'll do the connect on a new thread so the ChatFrame can show a progress bar.
		// Connecting to localhost is usually so fast you won't see the progress bar.
		new Thread("Connect") {
			public void run () {
//                              System.out.println(Network.port);
//                                   System.out.println(ChatFram.jTextField1.getText());
//                                   System.out.println(Integer.parseInt(ChatFram.jTextField2.getText()));
				try {
//					client.connect(5000, ChatFram.jTextField1.getText(), Integer.parseInt(ChatFram.jTextField2.getText()));
                                        client.connect(5000, host, 
                                                13000);
                                        
					// Server communication after connection can go here, or in Listener#connected().
				} catch (IOException ex) {
					ex.printStackTrace();
					System.exit(1);
				}
			}
		}.start();
                
        }
	static public class ChatFrame extends JFrame {
		CardLayout cardLayout;
		JProgressBar progressBar;
		JList messageList;
		JList pictureList;
		JTextField sendText;
		JButton sendButton, picture;
		JList nameList;
		BufferedImage image;
		final JFileChooser fc = new JFileChooser();
		
		public ChatFrame (String host) {
			//super("Chat Client");
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setSize(640, 200);
			setLocationRelativeTo(null);
			
			FileNameExtensionFilter filter = new FileNameExtensionFilter("JPEG", "jpeg", "jpg", "png", "bmp", "gif");
			fc.addChoosableFileFilter(filter);

			Container contentPane = getContentPane();
			cardLayout = new CardLayout();
			contentPane.setLayout(cardLayout);
			{
				JPanel panel = new JPanel(new BorderLayout());
				contentPane.add(panel, "progress");
				panel.add(new JLabel("Connecting to " + host + "..."));
				{
					panel.add(progressBar = new JProgressBar(), BorderLayout.SOUTH);
					progressBar.setIndeterminate(true);
				}
			}
			{
				JPanel panel = new JPanel(new BorderLayout());
				contentPane.add(panel, "chat");
				{
					JPanel topPanel = new JPanel(new GridLayout(1, 2));
					panel.add(topPanel);
					{
						topPanel.add(new JScrollPane(messageList = new JList()));
						messageList.setModel(new DefaultListModel());
					}
					{
						topPanel.add(new JScrollPane(nameList = new JList()));
						nameList.setModel(new DefaultListModel());
					}
					{
						topPanel.add(new JScrollPane(pictureList = new JList()));
						pictureList.setModel(new DefaultListModel());
					}
					DefaultListSelectionModel disableSelections = new DefaultListSelectionModel() {
						public void setSelectionInterval (int index0, int index1) {
						}
					};
					messageList.setSelectionModel(disableSelections);
					nameList.setSelectionModel(disableSelections);
					pictureList.setSelectionModel(disableSelections);
				}
				{
					JPanel bottomPanel = new JPanel(new GridBagLayout());
					panel.add(bottomPanel, BorderLayout.SOUTH);
					bottomPanel.add(sendText = new JTextField(), new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER,
						GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					bottomPanel.add(sendButton = new JButton("Send"), new GridBagConstraints(1, 0, 1, 1, 0, 0,
						GridBagConstraints.CENTER, 0, new Insets(0, 0, 0, 0), 0, 0));
					bottomPanel.add(picture = new JButton("Picture"), new GridBagConstraints(2, 0, 1, 1, 0, 0,
							GridBagConstraints.EAST, 0, new Insets(0, 0, 0, 0), 0, 0));
				}
			}

			sendText.addActionListener(new ActionListener() {
				public void actionPerformed (ActionEvent e) {
					sendButton.doClick();
				}
			});
			picture.addActionListener(new ActionListener() {
				public void actionPerformed (ActionEvent e) {
					picture.doClick();
				}
			});
		}

		public void setSendListener (final Runnable listener) {
			sendButton.addActionListener(new ActionListener() {
				public void actionPerformed (ActionEvent evt) {
					if (getSendText().length() == 0) return;
					listener.run();
					sendText.setText("");
					sendText.requestFocus();
				}
			});
		}
		
		public void setPicturelistener(final Runnable listener) {
			picture.addActionListener(new ActionListener() {
				public void actionPerformed (ActionEvent evt) {
			        int returnVal = fc.showOpenDialog(ChatFrame.this);

			        if (returnVal == JFileChooser.APPROVE_OPTION) {
			            File file = fc.getSelectedFile();
			            try {
			                image = ImageIO.read(file);
			            } catch (IOException ex) {
			                ex.printStackTrace();
			            }
			            //This is where a real application would open the file.
			            System.out.println("Opening: " + file.getName() + "." );
			        } else {
			        	System.out.println("Open command cancelled by user.");
			        }
				}
			});

		}

		public void setCloseListener (final Runnable listener) {
			addWindowListener(new WindowAdapter() {
				public void windowClosed (WindowEvent evt) {
					listener.run();
				}

				public void windowActivated (WindowEvent evt) {
					sendText.requestFocus();
				}
			});
		}

		public String getSendText () {
			return sendText.getText().trim();
		}
		
		public BufferedImage getImage () {
			return image;
		}
                

		public void setNames (final String[] names) {
			// This listener is run on the client's update thread, which was started by client.start().
			// We must be careful to only interact with Swing components on the Swing event thread.
			EventQueue.invokeLater(new Runnable() {
				public void run () {
					cardLayout.show(getContentPane(), "chat");
					DefaultListModel model = (DefaultListModel)nameList.getModel();
					model.removeAllElements();
					for (String name : names)
						model.addElement(name);
				}
			});
		}

		public void addMessage (final String message) {
			EventQueue.invokeLater(new Runnable() {
				public void run () {
					DefaultListModel model = (DefaultListModel)messageList.getModel();
					model.addElement(message);
					messageList.ensureIndexIsVisible(model.size() - 1);
				}
			});
		}
		
		public void addPicture (final BufferedImage message) {
			EventQueue.invokeLater(new Runnable() {
				public void run () {
					DefaultListModel model = (DefaultListModel)pictureList.getModel();
					model.addElement(message);
					pictureList.ensureIndexIsVisible(model.size() - 1);
				}
			});
		}
	}

	public static void main (String[] args) {
		Log.set(Log.LEVEL_DEBUG);
		new ChatClient();
	}
}
