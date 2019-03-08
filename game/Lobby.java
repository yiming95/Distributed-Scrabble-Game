import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;

import java.awt.Color;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import javax.swing.JButton;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;

import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JFrame;

import java.awt.Font;
import java.awt.List;

import javax.swing.JTextArea;

import java.net.*;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

/**
 * The Lobby class is used to create the UI for a lobby which implements 
 * the game membership and user pool requirement. 
 * User can create game, invite other players in the lobby list and start game. 
 * 
 * @author Group: FEET Off THE GROUND & LET DALAO TAKE US FLY, 14/08/2018
 */

public class Lobby implements ActionListener{
	private String userName;
	public JFrame frame;
	private JLabel lblPlayers;
	private JButton btnExit;
	private JTextArea textArea;
	private JTextArea textArea_1;
	private JTextArea textArea_2;
    private GameClient client;
    private JSONObject sentMsg, receivedMsg,systemState;
    private Socket socket;
    private boolean flag;
    private JList<ClientPlayer> playerList;
    private DefaultListModel<ClientPlayer> playerName; 
    private List selectedValuesList;
    private ClientPlayer invitedPlayer;
    private String gameId;
    private boolean vote_result;
    static String con_name;
    private String uid;
    private String invUserName;
	private String invGid; 
	private String errorMessage;
    
    private SocketListener listener;
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
					Lobby window = new Lobby();
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
	public Lobby() {
		this.userName = "Anonymous";
		initialize();
	}
	
	public void hibernate(){
		this.flag = false;
	}
	
	public void wake(){
		this.flag = true;
	}
	
	public Lobby(String name, Socket s) {
		this.flag = true;
		this.userName = name;
		this.socket=s;
		this.client = new GameClient(s);
		this.client.establishCommunication();
		this.sentMsg = new JSONObject();
		this.receivedMsg = new JSONObject();
		initialize();
		wake();
		
		/**
		 * client side socket listener, keep receiving the data from server side.
		 * The input stream data contains multiple types: sys_state type, 
		 * game_state type, event_place_char type, event_highlight type, event_
		 */
		this.listener = new SocketListener(){
			public void run(){
				while(flag) {
					try {
					  //fetch SystemState
					  
					receivedMsg = new JSONObject();
				      try{		
				    	  sentMsg.put("method","fetchSystemState");
				    	  receivedMsg = client.readMessage();
				      } catch (Exception e){
				    	  //textArea.setText("");
				    	  //textArea.append("No connection to server");
				    	  ImageIcon icon = new ImageIcon("src/pencil.png");
				    	  JOptionPane.showMessageDialog(null,
									"No connection to server!",
									"Error", JOptionPane.INFORMATION_MESSAGE,icon);
				    	  System.out.println("No connection to server");
				    	  break;
				      }
				      
					  try{  
						  if(receivedMsg != null && receivedMsg.get("type").equals("PLAIN")) {
							  uid = receivedMsg.get("uid").toString();
							  System.out.println("UID is : " + uid);}
						  }catch(Exception e){
							  System.out.println("Error caught: " + e.getMessage());
						  }
	
					  
					  try{
						  if(receivedMsg != null && receivedMsg.get("type").equals("INVITATION")) {
							  invUserName = receivedMsg.get("fromUsername").toString();
							  invGid = receivedMsg.get("gid").toString();
							  System.out.println("Invitation user name is : " + invUserName);
							  System.out.println("Invitation game id is : " + invGid);
						   inviteWindow();}
						   } catch(Exception e){
							   System.out.println("Error caught: " + e.getMessage());
						   }
						   
					  
					  try{
						  if(receivedMsg != null && receivedMsg.get("type").equals("CONFIG")) {  
						   
						   con_name = receivedMsg.get("userName").toString();
						   if (con_name != null){
						   textArea_2.setFont(new Font("Times New Roman", Font.BOLD, 12));
						   textArea_2.append(con_name + "\n");
						  }}
						   } catch(Exception e){
							   System.out.println("Error caught: " + e.getMessage());
						   }
					  
					  
					  try{
					  if(receivedMsg != null && receivedMsg.get("type").equals("GAME_STATE")) {
						  if (receivedMsg.get("gid").toString() != null ){  
						   gameId = receivedMsg.get("gid").toString();	
						   System.out.println("Game ID is: " + gameId);
						  }}
						  }catch(Exception e){
							  System.out.println("Error caught: " + e.getMessage());
					  }
					 
					  try{
					  if(receivedMsg != null && receivedMsg.get("type").equals("SYS_STATE")) {
						  
						  System.out.println("System state Lobby -----------");
						  userUpdate();
						  gameUpdate();}
						  
						  }catch(Exception e){
							  System.out.println("Error caught: " + e.getMessage());  
					  }
						  
					 try{
					  if(receivedMsg != null && receivedMsg.get("type").equals("EVENT_GAME_START")) {
						  System.out.println("Loading Game Interface");
						  Socket s = client.copySocket();			   
					      hibernate();
						   SwingUtilities.invokeLater(new Runnable() {
							      
							      public void run() {
							    	  ClientSwing game = new ClientSwing(userName, s);
							    	  game.frame.setVisible(true);
							    	  System.out.println("Rendering game view");
							    	  frame.setVisible(false);
							      }
							    });
						  }	}catch (Exception e)
					          {
							  System.out.println("Error caught: " + e.getMessage());  
					          }
					  
					  try{
					  if(receivedMsg != null && receivedMsg.get("type").equals("GAME_NON_EXIST_ERR")) {
						  System.out.println("Starting non-existing game");
						  textArea.setText("");
						  textArea.setText("Startig non-existing game");
					  }
					  }catch( Exception e){
						  System.out.println("Error caught: " + e.getMessage());  
					  }
					  
					  
					}
					catch (Exception e) {
		                e.printStackTrace();
		                wake();
		                }
				}
			}
		};
		this.listener.start();
	}
	
	/**
	 * Parse the data from server and update on the GUI.
	 */
	private void gameUpdate() {
		try{
		textArea_1.setText("");
		textArea_1.setFont(new Font("Times New Roman", Font.BOLD, 12));
		JSONArray games = (JSONArray) receivedMsg.get("games");
		JSONArray users = (JSONArray) receivedMsg.get("users");
		for(int i = 0; i < games.size();i++) {
			JSONObject game = (JSONObject)games.get(i);
			textArea_1.append("Game ID: "+game.get("gid")+"\n");
			
			JSONArray gamePlayers = (JSONArray)game.get("players");
			for(int j = 0; j < gamePlayers.size();j++) {
				JSONObject user = (JSONObject)gamePlayers.get(j);
				textArea_1.append("Player in game"+game.get("gid")+ ":  " +user.get("username")+"\n");
			}
		}
	}catch(Exception e){
		 System.out.println("Error caught: " + e.getMessage());
	    }
	}
	
	/**
	 * Parse the data from server and update on the GUI.
	 */
	private void userUpdate() {	
		playerName.removeAllElements();
		
		try{
			JSONArray users =  (JSONArray) receivedMsg.get("users");
			
			for(int i = 0; i < users.size();i++) {
					JSONObject player = (JSONObject) users.get(i);
					if ((player.get("username").toString())!= null){
						playerName.addElement(new ClientPlayer(player.get("id").toString(),player.get("username").toString()));
					}
				}
			
		}catch(Exception e) {
      	  System.out.println("Error caught: " + e.getMessage());
        }                                           
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 758, 609);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Games");
		lblNewLabel.setFont(new Font("Times New Roman", Font.BOLD, 16));
		lblNewLabel.setBounds(51, 12, 76, 37);
		frame.getContentPane().add(lblNewLabel);
		
		lblPlayers = new JLabel("Players In Lobby");
		lblPlayers.setForeground(new Color(51, 51, 51));
		lblPlayers.setFont(new Font("Times New Roman", Font.BOLD, 14));
		lblPlayers.setBounds(469, 22, 148, 19);
		frame.getContentPane().add(lblPlayers);
		
		textArea = new JTextArea();
		textArea.setBounds(51, 399, 376, 127);
		frame.getContentPane().add(textArea);
		textArea.setFont(new Font("Times New Roman", Font.BOLD, 12));
		textArea.append("Welcome to the game! " + "\n"+"\n"+ "To start a game"+"\n"
		                +"click 'create game' button" +"\n"+"\n"+ "To join the game"+"\n"+ 
				         "please don't click 'create game'."+ "\n");
		
		JButton btnJoinGame = new JButton("Invite");
		btnJoinGame.setBounds(597, 413, 99, 31);
		frame.getContentPane().add(btnJoinGame);
		btnJoinGame.addActionListener(this);
		
		JButton btnCreateGame = new JButton("Create Game");
		btnCreateGame.setBounds(468, 413, 117, 31);
		frame.getContentPane().add(btnCreateGame);
		btnCreateGame.addActionListener(this);
		
		btnExit = new JButton("Exit");
		btnExit.setBounds(597, 455, 99, 31);
		frame.getContentPane().add(btnExit);
		btnExit.addActionListener(this);
		
		textArea_1 = new JTextArea();
		textArea_1.setBounds(51, 57, 376, 296);
		frame.getContentPane().add(textArea_1);
		
		JButton btnNewButton = new JButton("Start Game");
		btnNewButton.setBounds(468, 456, 117, 29);
		frame.getContentPane().add(btnNewButton);
		btnNewButton.addActionListener(this);		
		
		// JList for player to invite other players in lobby
	    playerName = new DefaultListModel<>();
        playerList = new JList(playerName);
		playerList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	
		/*scroll pane
	    JScrollPane playerListScrollPane  = new JScrollPane(playerList,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                //frame.getContentPane().add(playerListScrollPane);
	    */
		playerList.setBounds(468, 56, 227, 297);
		frame.getContentPane().add(playerList);
		
		try{
		playerList.addListSelectionListener(new ListSelectionListener() {
	           @Override
	           public void valueChanged(ListSelectionEvent arg0) {
	               if (arg0.getValueIsAdjusting()) {
	            	 invitedPlayer = playerList.getSelectedValue(); 
	            	 System.out.println("Selected player to invite is : " + invitedPlayer );
	            	 System.out.println("Selected player id : " + invitedPlayer.uid );
	            	 
	               }
	           }
	       });
		}catch(Exception e){
			System.out.println("Error, not click invitation button after selecting player");
		}
		
		JLabel lblNewLabel_1 = new JLabel("Log in as: ");
		lblNewLabel_1.setFont(new Font("Times New Roman", Font.BOLD, 14));
		lblNewLabel_1.setBounds(278, 19, 73, 25);
		frame.getContentPane().add(lblNewLabel_1);
		
		textArea_2 = new JTextArea();
		textArea_2.setBounds(353, 23, 74, 16);
		frame.getContentPane().add(textArea_2);
		
		JLabel lblNewLabel_2 = new JLabel("Information Board");
		lblNewLabel_2.setFont(new Font("Times New Roman", Font.BOLD, 14));
		lblNewLabel_2.setBounds(51, 365, 148, 22);
		frame.getContentPane().add(lblNewLabel_2);
	}
	   @SuppressWarnings("deprecation")
	public synchronized void actionPerformed(ActionEvent e) 
	    {
		   String actionCommand = e.getActionCommand();
		  
		   if (actionCommand.equals("Create Game")) {
			   
			   this.sentMsg = new JSONObject();
			   this.sentMsg.put("method","createGame");
			   client.sendMessage(this.sentMsg);
			   textArea.setText("");
			   textArea.append("To play single game" + "\n" +"click 'Start Game' button "
			   + "\n"+"\n"+ "To play multiple player game" +"\n"
			   		+ "select player and click 'Invite' button "+"\n" +"then click 'start game' button." + "\n");  
		   } 
		   
		   
		   
		   else if (actionCommand.equals("Start Game")) {
			   
			   this.sentMsg = new JSONObject();
			   this.sentMsg.put("method","startGame");
			   client.sendMessage(this.sentMsg);
			   //frame.setVisible(false);
		   }
		   
		   else if (actionCommand.equals("Invite")){
			   
			    try{
			    JSONObject inviteMessage = new JSONObject();
				inviteMessage.put("method","invite");
				inviteMessage.put("gid", gameId);
			    inviteMessage.put("uid", invitedPlayer.uid);
				client.sendMessage(inviteMessage);
			    
			   	System.out.println("Invite-----------------");	   	
	            
		   }catch( Exception e1){
			    textArea.setText("");
			    textArea.setText("Invaild invite action" + "\n" 
			    );
		   }
			    }
			      
		   else if (actionCommand.equals("Exit")) {
			    System.exit(0);
		   }
	    }
	   
	   /**
		 * invite window GUI.
		 */
	   public void inviteWindow() {
			ImageIcon icon = new ImageIcon("src/pencil.png");
			int input = 100;
			input = JOptionPane.showConfirmDialog(null,
					con_name +" have received an invitation from " + invUserName  + ", accept or not?",
					"Invitation", JOptionPane.YES_NO_OPTION,
					JOptionPane.DEFAULT_OPTION, icon);

			if (input == 0) {
				vote_result = true;
				JSONObject joinMessage = new JSONObject();
				joinMessage.put("method","joinGame");
				joinMessage.put("gid", invGid);
				client.sendMessage(joinMessage);
				System.out.println("Accept invitation.");
			} else {
				vote_result = false;
			}
		
		}
}
