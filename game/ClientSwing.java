import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.Color;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.UIManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Socket;
import java.util.ArrayList;
import java.util.*;
import javax.swing.JTextArea;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.awt.SystemColor;

/**
 * The ClientSwing class implements the game GUI for the clients.
 * Player can play in turn and vote for others selection.
 * The player information is updated in the game information board.
 * There is also a information board to guide user how to play.
 * 
 * @author Group: FEET Off THE GROUND & LET DALAO TAKE US FLY, 14/08/2018
 */

public class ClientSwing implements ActionListener {

	/**
	 * clientSwing class.
	 */
	private static final long serialVersionUID = 1L;

	JFrame frame;
	private JTextArea textArea_2;
	private JTextArea textArea_1;
	JSONObject message;
	private String message_game;
	private String message_user_info;
	private boolean vote_result = true;
	private String message_chat;
	private JTextArea textArea_4;
	private JTextArea textArea_5;
	private JTextArea textArea_3;
	private int pos_x, pos_y;
	private String insert_word;
	private String  highlight_word;
	private boolean voted;
	static JButton[][] jb = new JButton[20][20];
	int num[][];
	char[][] map;
	static private char status = 'a'; // player's game status
	int ap = 1; // activate player; current player with priority
	int flag;
	int fflag = 0;
	int playernum = 1; // total number of players
	String[] aa = new String[2];
	HashMap<List<Integer>,String> word = new HashMap(); // the word selected
	JButton btnNewButton = new JButton("Insert");
	int yesnum = 0;
	int nonum = 0;
	private String userName;
	private GameClient client;
	private JSONObject sentMsg, receivedMsg, systemState, highlightMsg, voteMsg;
	private JSONObject sendingMsg;
	private JSONObject systemState1;
	private String activatePlayer;
	private static JTextArea textArea = new JTextArea();
	private SocketListener listener;
	
	
	public String constructString(){
		String res = "";
		if (this.word.size() > 0) {
			for (List l : this.word.keySet()){
				res = res + this.word.get(l);
			}
		}
		System.out.println("Constructed word: " + res);
		return res;
	}
	
	 // determine the vote result
	public boolean determine() {
		if (yesnum > nonum) {
			yesnum = 0;
			nonum = 0;
			return true;
		} else {
			yesnum = 0;
			nonum = 0;
			return false;
		}
	}
	
	public void LetterAdd(int i, int j, char c) { // add letter to target grim,
													// ' ' to remove
		map[i][j] = c;
	}

	public void setStatus(char c) { // set player status
		status = c;
	}

	static public char getStatus() { // get current status
		return status;
	}

	public static boolean isNumeric(String str) { // determine if the first
													// letter of string is a
													// number
		int chr = str.charAt(0);
		if (chr < 48 || chr > 57)
			return false;
		    return true;
	}

	public static void colorClear() { // clear the color of all buttons
		
		for (int i = 0; i < 20; i++)
			for (int j = 0; j < 20; j++)
				jb[i][j].setBackground(Color.WHITE);
		
	}

	public void playerPriorityChange() { // change the priority of players
		if (ap == playernum)
			ap = 1;
		else
			ap = ap + 1;
	}
	public ClientSwing(String name, Socket s) {
		this.userName = name;
		this.client = new GameClient(s);
		this.client.establishCommunication();	
		this.sentMsg = new JSONObject();
		this.voted = false;
		this.receivedMsg = new JSONObject();
		initialize();
		
		/**
		 * client side socket listener, keep receiving the data from server side.
		 * The input stream data contains multiple types: sys_state type, 
		 * game_state type, event_place_char type, event_highlight type, event_
		 */
		this.listener = new SocketListener(){
			public void run(){
				while (true) {
					 System.out.println("Client Game receiving message---------------------");
					 // game state listen message from the server
					 
					 message = new JSONObject();
					 try{
					  message = client.readMessage();
					 }catch (Exception e){
						 //textArea_2.setText("");
				    	 //textArea_2.append("No connection to server");
				    	 ImageIcon icon = new ImageIcon("src/pencil.png");
				    	  JOptionPane.showMessageDialog(null,
									"No connection to server!",
									"Error", JOptionPane.INFORMATION_MESSAGE,icon);
						 System.out.println("No connection to server");
				    	  break;
				      }
					 
		              try{
		            	  String type = message.get("type").toString();
		  			    switch (type) {
		  			    	case Type.SYS_STATE: 
		  			    		System.out.println("Update the system state");
		  			    		break;	  			    		  
		  			        case Type.GAME_STATE:
		  			        	textArea_3.setText("");
		  			            System.out.println("Update game states here");
		  			            updateGameInfo();
		  			            textArea_3.append(Lobby.con_name + "\n");
		  			            
		  			            userName = message.get("turnPlayerName").toString();
		  			            System.out.println("userName is: "+ userName);
		  			           
		  			            long l = (Long)  message.get("stage");
		  			            int stagenum = (int) l;
		  			            	 
		  			            if (stagenum == 1){  //place stage
		  			            	colorClear(); //clear the color of the board
		  			            	voted = false;
		  							System.out.println("Stage Num is 1.");
		  							
		  							// only turn player can insert and highlight
		  							if( (Lobby.con_name).equals(userName)){
		  							// game status is the user to insert character in yellow color.
		  							setStatus('i'); 
		  							activatePlayer = message.get("turnPlayerName").toString();
		  							System.out.println("activePlayer now is :" + activatePlayer);
		  							}
		  							
		  						//highlight stage
		  						}else if (stagenum == 2){  
		  							voted = false;
		  							if( (Lobby.con_name).equals(userName)){
		  							System.out.println("Stage Num is 2.");
		  							 // status is s then it allows user to highlight word in red color.
		  							setStatus('s'); 
		  						  }
		  						}
		  			            
		  						else if (stagenum == 3 && !voted){ //vote stage
		  							
		  							System.out.println("Stage Num is 3.");
		  							
		  						// the current player is in turn, can not vote	
		  						// the current player is in turn, can not vote
		  							if( (Lobby.con_name).equals(userName)){  
		  								setStatus('v');
			  							if (getStatus() == 'v')
			  							{	
			  								System.out.println("System status is v.");
			  								//assume vote yes for himself
			  								JSONObject messageMVote = new JSONObject();
			  								messageMVote.put("method","vote");
			  								messageMVote.put("agree", true);
			  								client.sendMessage(messageMVote);
			  							}	
		  							}
		  							
		  						// the current player not in turn, can vote
		  							if ( ! (Lobby.con_name).equals(userName)){  
		  								setStatus('v');
			  							if (getStatus() == 'v')
			  							{	
			  								System.out.println("System status is v.");
			  								voteWindow();
			  							}	
		  							}
		  						}
		  			            break;
		  			            
		  			      	case Type.EVENT_PLACE_CHAR:
		  			      		System.out.println("event place char");
		  			 
		  			      		// update the current board of the char inserted,  by reading message from server with type "EVENT_PLACE_CHAR".
		  			    		String x_value = message.get("x").toString();
		  			    		int x1 = Integer.parseInt(x_value);
		  			    		System.out.println("x value is : " + x1);
		  			    	
		  			    		String y_value = message.get("y").toString();
		  			    		int y1 = Integer.parseInt(y_value);
		  			    		System.out.println("y value is : " + y1);
		  			    		
		  			    		String c_value = message.get("c").toString();
		  			    		System.out.println("z value is : " + c_value);
		  			    		
		  			    		//jb[x1][y1].setBackground(Color.YELLOW);
		  			    		jb[x1][y1].setText(c_value);
		  			    		
		  			      		break;
		  			            
		  			        case Type.EVENT_HIGHLIGHT:
		  			        	System.out.println("event highlight");
		  						highlight_word = message.get("highlights").toString();
		  			        	break;
		  			        	
		  			       	
		  			      case Type.EVENT_GAME_END:
		  			        	System.out.println("all players skip game, end the game");
		  			        	System.exit(0);	
		  			        	break; 	
		  			       
		  			        	
		  			        case Type.EVENT_VOTE:
		  			        	System.out.println("event vote");
		  			        	updateVote();
		  			        	break;  
		  			        	
		  			        case Type.EVENT_HIGHLIGHT_CELL:
		  			        	System.out.println("highlight cells" );
		  			        	
		  			        	// set cell color to red
		  			    		String xh_value = message.get("x").toString();
		  			    		int x1h = Integer.parseInt(xh_value);
		  			    		System.out.println("x value is : " + x1h);
		  			    	
		  			    		String yh_value = message.get("y").toString();
		  			    		int y1h = Integer.parseInt(yh_value);
		  			    		System.out.println("y value is : " + y1h);
		  			    		
		  			    		String h_value = message.get("c").toString();
		  			    		System.out.println("z value is : " + h_value);
		  			    		
		  			    		if  (h_value.equals("r"))
		  			    			jb[x1h][y1h].setBackground(Color.RED);
		  			    		
		  			    		if  (h_value.equals("y"))
			  			    		jb[x1h][y1h].setBackground(Color.YELLOW);
		  			    		if  (h_value.equals("w"))
			  			    		jb[x1h][y1h].setBackground(Color.WHITE);
		  			    		
		  			        	break;
		  			    }  
		              }catch(Exception e) {
		            	  System.out.println("Error caught: " + e.getMessage());
		              }                                                                                                                                                                                                                                               
				} 
			}
			
		};
		
		this.listener.start();
	}

	/**
	 * Parse the data from server and update on the GUI.
	 */
	private void updateGameInfo() {
		textArea_1.setText("");
		textArea_5.setText("");
		String gameId = message.get("gid").toString();
        textArea_1.append("Game ID: "+ gameId + "\n");
        
    	long l1 = (Long)  message.get("stage");
        int stageNumber = (int) l1;
        textArea_1.append("stage : "+ stageNumber + "\n");
        
        long l2 = (Long)  message.get("round");
        int roundNumber = (int) l2;
        textArea_1.append("Round : "+ Integer.toString(roundNumber + 1) + "\n");
		
        String turnPlayer = message.get("turnPlayerName").toString();
        textArea_1.append("Current turn player: " + turnPlayer + "\n");
        
        JSONArray players = (JSONArray) message.get("players");
        for(int j = 0; j < players.size();j++) {
			JSONObject player = (JSONObject)players.get(j);
			textArea_1.append("User: "+ player.get("username")+"\n");
			textArea_1.append("Score: "+ player.get("score")+"\n");
		}
        
        textArea_5.append(turnPlayer + "\n");
	}	
	
	/**
	 * Parse the data from server and update on the GUI.
	 */
	private void updateVote(){
		boolean voteResult = (boolean) this.message.get("username1");
		System.out.println("Vote result is:  " + voteResult);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.getContentPane().setFont(new Font("Tahoma", Font.PLAIN, 14));
		frame.getContentPane().setBackground(
				UIManager.getColor("Button.background"));
		frame.setBounds(100, 100, 1013, 770);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		JLabel lblNewLabel = new JLabel(" turn");
		lblNewLabel.setFont(new Font("Times New Roman", Font.BOLD, 18));
		lblNewLabel.setBounds(111, 37, 135, 29);
		frame.getContentPane().add(lblNewLabel);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});

		btnNewButton.setBounds(232, 603, 120, 29);
		frame.getContentPane().add(btnNewButton);

		JLabel lblNewLabel_1 = new JLabel("Game Information");
		lblNewLabel_1.setFont(new Font("Times New Roman", Font.BOLD, 18));
		lblNewLabel_1.setBounds(569, 40, 189, 23);
		frame.getContentPane().add(lblNewLabel_1);

		textArea_2 = new JTextArea();
		textArea_2.setFont(new Font("Times New Roman", Font.BOLD, 12));
		textArea_2.setBackground(UIManager.getColor("TextField.background"));
		textArea_2.setBounds(569, 397, 397, 195);
		frame.getContentPane().add(textArea_2);
		textArea_2.setColumns(10);
		textArea_2.setText("You are in game now" + "\n\n"+"If it is your turn:" +"\n"
							+"1. select a cell in board" +"\n" 
							+"2. insert a character in the box below" +"\n"
							+"3. click 'insert' button" + "\n"
							+"4. highlight character step starts AUTOMATICLY"+"\n"
							+"5. click 'end select and vote' buton"+"\n"
							+"6. ELSE click 'skip' button to skip this turn");

		JLabel lblNewLabel_2 = new JLabel("Information Board");
		lblNewLabel_2.setFont(new Font("Times New Roman", Font.BOLD, 18));
		lblNewLabel_2.setBounds(569, 346, 189, 32);
		frame.getContentPane().add(lblNewLabel_2);

		textArea_1 = new JTextArea();
		textArea_1.setFont(new Font("Times New Roman", Font.BOLD, 12));
		textArea_1.setBackground(UIManager.getColor("TextField.background"));
		textArea_1.setBounds(569, 86, 397, 234);
		frame.getContentPane().add(textArea_1);
		textArea_1.setColumns(10);

		JButton btnNewButton_2 = new JButton("End Select And Vote");
		btnNewButton_2.setBounds(34, 645, 160, 29);
		btnNewButton_2.addActionListener(this);
		frame.getContentPane().add(btnNewButton_2);

		textArea_4 = new JTextArea();
		textArea_4.setBackground(SystemColor.text);
		textArea_4.setLineWrap(true);
		textArea_4.setBounds(34, 605, 160, 28);
		frame.getContentPane().add(textArea_4);
		textArea_4.setColumns(10);

		textArea_5 = new JTextArea();
		textArea_5.setFont(new Font("Monospaced", Font.PLAIN, 16));
		textArea_5.setBackground(SystemColor.text);
		textArea_5.setLineWrap(true);
		textArea_5.setBounds(33, 41, 71, 25);
		frame.getContentPane().add(textArea_5);

		JPanel jp1 = new JPanel();
		jp1.setBounds(23, 85, 509, 507);


		for (int i = 0; i < 20; i++)
			for (int j = 0; j < 20; j++) {

				jb[i][j] = new JButton();
				jb[i][j].setName(i + "_" + j);
				jb[i][j].setBackground(Color.white);
				jb[i][j].setFont(new Font(Font.DIALOG_INPUT, Font.PLAIN, 16));
				jb[i][j].setMargin(new Insets(0, 0, 0, 0));
				jb[i][j].setPreferredSize(new Dimension(20, 20));
				jb[i][j].addActionListener(new ActionListener() {
					
					@SuppressWarnings("unchecked")
					public void actionPerformed(ActionEvent e) {		
						String temp = new String();
						temp = String.valueOf(e);
						temp = temp.substring(temp.length() - 5);
						while (isNumeric(temp) == false)
							temp = temp.substring(1);
						textArea.setText(temp);
						aa = temp.split("_");

						if (getStatus() == 'i' && flag == 0) {
							System.out.println("System status is i.");
							
							jb[Integer.parseInt(aa[0])][Integer.parseInt(aa[1])].setBackground(Color.YELLOW);
							
							JSONObject yellowMessage = new JSONObject();
							yellowMessage.put("method","highlightCell");
							yellowMessage.put("x",Integer.toString(Integer.parseInt(aa[0])));
							yellowMessage.put("y",Integer.toString(Integer.parseInt(aa[1])));
							yellowMessage.put("c","y");
							client.sendMessage(yellowMessage);

							flag++;
							btnNewButton.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent arg0) {
											pos_x = Integer.parseInt(aa[0]);
											pos_y = Integer.parseInt(aa[1]);
											
											// "place" method
											// send message of inserted character to the server
											System.out.println("insert message is called" );
											JSONObject insertMessage = new JSONObject();
											insertMessage.put("method","place");
											insertMessage.put("x",Integer.toString(pos_x));
											insertMessage.put("y",Integer.toString(pos_y));
											insertMessage.put("c",textArea_4.getText());
											client.sendMessage(insertMessage);
											insert_word = textArea_4.getText();
											//jb[Integer.parseInt(aa[0])][Integer.parseInt(aa[1])].setBackground(new Color(220, 220, 220));
											flag = 0;
										}
									});
						}
						
						// select button listener
						if (getStatus() == 's') {
							if (jb[Integer.parseInt(aa[0])][Integer.parseInt(aa[1])].getBackground().equals(Color.RED)){
								
								
								pos_x = Integer.parseInt(aa[0]);
								pos_y = Integer.parseInt(aa[1]);
								
								// send the cells to be highlight to the server
								JSONObject highCellMessage = new JSONObject();
								highCellMessage.put("method","highlightCell");
								highCellMessage.put("x",Integer.toString(pos_x));
								highCellMessage.put("y",Integer.toString(pos_y));
								highCellMessage.put("c","w");
								client.sendMessage(highCellMessage);
								
								
								jb[Integer.parseInt(aa[0])][Integer.parseInt(aa[1])].setBackground(Color.WHITE);
								List<Integer> key = Arrays.asList(Integer.parseInt(aa[0]),Integer.parseInt(aa[1]));
								word.remove(key);
							}
							else {
								System.out.println("System status is s.");
								// user can click the button to highlight words here
								jb[Integer.parseInt(aa[0])][Integer.parseInt(aa[1])].setBackground(Color.RED);
								
								pos_x = Integer.parseInt(aa[0]);
								pos_y = Integer.parseInt(aa[1]);
								
								// send the cells to be highlight to the server
								JSONObject highCellMessage = new JSONObject();
								highCellMessage.put("method","highlightCell");
								highCellMessage.put("x",Integer.toString(pos_x));
								highCellMessage.put("y",Integer.toString(pos_y));
								highCellMessage.put("c","r");
								client.sendMessage(highCellMessage);
								
								// "word" is the selected highlight word.
								List<Integer> key = Arrays.asList(Integer.parseInt(aa[0]),Integer.parseInt(aa[1]));
								word.put(key, jb[Integer.parseInt(aa[0])][Integer.parseInt(aa[1])].getText());
								fflag = 1;
							}
							
							}     
						;}
				});
				jp1.add(jb[i][j]);
				flag = 0;
			}
		frame.getContentPane().add(jp1);
		JButton btnNewButton_4 = new JButton("Skip");
		btnNewButton_4.setBounds(382, 603, 120, 29);
		btnNewButton_4.addActionListener(this);
		frame.getContentPane().add(btnNewButton_4);
		
		JLabel lblNewLabel_3 = new JLabel("Log in as: ");
		lblNewLabel_3.setFont(new Font("Times New Roman", Font.BOLD, 16));
		lblNewLabel_3.setBounds(369, 35, 102, 32);
		frame.getContentPane().add(lblNewLabel_3);
		
		textArea_3 = new JTextArea();
		textArea_3.setBounds(439, 44, 84, 15);
		frame.getContentPane().add(textArea_3);
		
		JButton btnNewButton_1 = new JButton("Exit");
		btnNewButton_1.setBounds(232, 645, 120, 29);
		btnNewButton_1.addActionListener(this);
		frame.getContentPane().add(btnNewButton_1);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String actionCommand = e.getActionCommand();
		
		if (actionCommand.equals("End Select And Vote")) {
			
			// "highlight" method
			// send message of highlight word to the server
		    JSONObject messageH = new JSONObject();
			messageH.put("method","highlight");
			messageH.put("highlight",this.constructString());
			client.sendMessage(messageH);
		}

		if (actionCommand.equals("Skip")) {
			
			JSONObject skipMessage = new JSONObject();
			skipMessage.put("method","place");
			skipMessage.put("x","-1");
			skipMessage.put("y","-1");
			skipMessage.put("c","skip");
			client.sendMessage(skipMessage);
		}
		
		if (actionCommand.equals("Exit")) {
		   System.exit(0);
		}
	}
		
	// vote window for users not current turn to vote
	public void voteWindow() {
		
		ImageIcon icon = new ImageIcon("src/pencil.png");
		int input = 100;
		input = JOptionPane.showConfirmDialog(null,
				highlight_word + " is selected, Please vote now.",
				"Vote", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE, icon);

		if (input == 0) {
			vote_result = true;
			setStatus('a');   //after vote, status back to default value
		} else {
			vote_result = false;
			setStatus('a');  
		}
		//word.delete(0, word.length());
		word.clear();
		// send vote result to server.
		JSONObject messageVote = new JSONObject();
		messageVote.put("method","vote");
		messageVote.put("agree", vote_result);
		client.sendMessage(messageVote);
		this.voted = true;
	}
}