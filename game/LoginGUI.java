import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.UIManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import javax.swing.JPasswordField;
import org.json.simple.*;
import java.net.*;


/**
 * The LogGUI class is used to create the UI for users to log in. 
 * Users can enter IP address, port number and  user name. 
 * 
 * @author Group: FEET Off THE GROUND & LET DALAO TAKE US FLY, 14/08/2018
 */

public class LoginGUI implements ActionListener{

	private JFrame frame;
	private JTextField textField;
	private JTextField textField_1;
	private JButton btnExit;
    private GameClient client;
    private JSONObject sendingMsg, receivedMeg;
    private String myUserName, myPass;
    private JTextField textField_2;
    private JTextField textField_3;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		try {
			UIManager.setLookAndFeel(UIManager
					.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoginGUI window = new LoginGUI();
					window.frame.setTitle("Login");
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public LoginGUI() {
		
		initialize();
		textField_2.setText("127.0.0.1");
		textField_3.setText("8000");
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setAutoRequestFocus(false);
		frame.setBounds(100, 100, 408, 299);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		textField_1 = new JTextField();
		textField_1.setBounds(120, 124, 194, 29);
		frame.getContentPane().add(textField_1);
		textField_1.setColumns(10);
		
		JLabel lblUserName = new JLabel("User Name");
		lblUserName.setBounds(30, 130, 79, 16);
		frame.getContentPane().add(lblUserName);
		
		JButton btnLogin = new JButton("Login");
		btnLogin.setBounds(77, 176, 106, 29);
		frame.getContentPane().add(btnLogin);
		btnLogin.addActionListener(this);
		
		JButton btnExit_1 = new JButton("Exit");
		btnExit_1.setBounds(220, 176, 106, 29);
		frame.getContentPane().add(btnExit_1);
		btnExit_1.addActionListener(this);
		
		textField_2 = new JTextField();
		textField_2.setBounds(120, 42, 194, 29);
		frame.getContentPane().add(textField_2);
		textField_2.setColumns(10);
		
		textField_3 = new JTextField();
		textField_3.setBounds(120, 83, 194, 29);
		frame.getContentPane().add(textField_3);
		textField_3.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("IP address");
		lblNewLabel.setBounds(30, 48, 136, 16);
		frame.getContentPane().add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Port number");
		lblNewLabel_1.setBounds(30, 89, 136, 16);
		frame.getContentPane().add(lblNewLabel_1);

	}
	   public void actionPerformed(ActionEvent e) 
	    {
		   String actionCommand = e.getActionCommand();
		   if(actionCommand.equals("Login")) {
			   
				this.client = new GameClient();
				
				String IP = textField_2.getText();
				String portNumber= textField_3.getText();
				try {
					this.receivedMeg = client.createConnection(IP, Integer.parseInt(portNumber));
				} catch (NumberFormatException e2) {
					e2.printStackTrace();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
				if(this.receivedMeg.get("status").equals("error")) {
					System.out.println(this.receivedMeg.get("message"));
					System.exit(0);
				}
				this.sendingMsg = new JSONObject();
				this.receivedMeg = new JSONObject();
			  
			   	setUserName();
			   	
			    try {
					this.receivedMeg = this.client.readMessage();
				} catch (Exception e1) {
					 ImageIcon icon = new ImageIcon("src/pencil.png");
			    	  JOptionPane.showMessageDialog(null,
								"No connection to server!",
								"Error", JOptionPane.INFORMATION_MESSAGE,icon);
					e1.printStackTrace();
				}
			   	
			    if(this.receivedMeg.get("status").equals("success")) {
			    	System.out.println("UserName is "+this.myUserName);
			    	System.out.println("Password is "+this.myPass);
			    	
			    	ImageIcon icon = new ImageIcon("src/pencil.png");
					JOptionPane.showMessageDialog(null,
							this.myUserName +" successfully login!",
							"Confirm", JOptionPane.INFORMATION_MESSAGE,icon);
			    	
					frame.setVisible(false);
			    	try{
			    		Socket s = client.copySocket();
			    		Lobby lobbyUI = new Lobby(this.myUserName, s);
						lobbyUI.frame.setTitle("Lobby");
						lobbyUI.frame.setVisible(true);
			    	}catch (Exception exception) {
						exception.printStackTrace();
					}
			    }
		   }
		   else if (actionCommand.equals("Sign Up")) {
			    setUserName();
			    if(this.receivedMeg.get("status").equals("success")) {
			    	System.out.println("Set UserName as "+this.myUserName);
			    	System.out.println("Password is "+this.myPass);
			    	JOptionPane.showMessageDialog(null, "Successfully signed up, your username is "+this.myUserName);
			    }
		   }
		   else if (actionCommand.equals("Exit")) {
				frame.setVisible(false);
				System.exit(0);
		   }
	    }
	   
	   private void setUserName(){
		    this.myUserName = textField_1.getText();
		   
		    if (myUserName.equals("")) {
		    	JOptionPane.showMessageDialog(null, "Username can't be empty");
		    	this.receivedMeg = new JSONObject();
		    	this.receivedMeg.put("status", "error");
		    }
		    else {
			    this.sendingMsg = new JSONObject();
			    this.sendingMsg.put("method","setUserName");
			    this.sendingMsg.put("userName", myUserName);
			    this.client.sendMessage(this.sendingMsg);
			   
			}
	   }
}
