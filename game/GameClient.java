import java.net.*;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import java.io.*;
import org.json.simple.*;

/**
 * The GameClient class is to process IO data to the server.
 * @author Group: FEET Off THE GROUND & LET DALAO TAKE US FLY, 14/08/2018
 */

public class GameClient extends Thread {

		
		private static Socket s;
	    private static DataInputStream dis = null;
	    private static DataOutputStream dos = null;
		private JSONObject receivedMessage,systemState;
		
		public GameClient(Socket socket) {
			this.s = socket;
		}
		
		public GameClient() {
			this.s = null;
		}
		
		
		public JSONObject createConnection(String address, int port) throws Exception{
				this.receivedMessage = new JSONObject();
				try {
					this.s = new Socket(address,port);
					establishCommunication();
				    readMessage();
					//try to connect to server
					return this.receivedMessage;
					}
				catch (IOException e) {
					 ImageIcon icon = new ImageIcon("src/pencil.png");
			    	  JOptionPane.showMessageDialog(null,
								"No connection to server!",
								"Error", JOptionPane.INFORMATION_MESSAGE,icon);
					System.out.println("Error:Couldn't connect to server");
					this.receivedMessage.put("status","error");
					this.receivedMessage.put("message", "Error:Couldn't connect to server");
					return this.receivedMessage;
				  }
			}
		
		public Socket copySocket() {
			Socket socketCpy = this.s;
			return socketCpy;
		}
		
		public void establishCommunication() {
			this.receivedMessage = new JSONObject();
		 	try {
	            this.dis = new DataInputStream(this.s.getInputStream());
	            this.dos = new DataOutputStream(this.s.getOutputStream());
		 	}
			catch (Exception e) {
				System.out.println("Error: have trouble when establishing I/O connection with server");
				this.receivedMessage.put("status","error");
				this.receivedMessage.put("message","Error: have trouble when establishing I/O connection with server");
		 	}
		}
				   
		
		public void sendMessage(JSONObject message) {
       // send and read
				    	try
				    		{	
				               this.dos.writeUTF(message.toJSONString());	
				    		   this.dos.flush();
				    		}
				    	catch (Exception e) {
				    		this.receivedMessage = new JSONObject();
				    		this.receivedMessage.put("status","error");
				    		this.receivedMessage.put("message","Error: have trouble when sending message to server");
				    	}
				    }
		

	    public JSONObject readMessage() throws Exception{
	            System.out.println("reading msg");
	            try {
		            String msg = this.dis.readUTF();
		            System.out.println(msg);
	            	this.receivedMessage = (JSONObject)JSONValue.parse(msg);
	            	return this.receivedMessage;
	            }
	            catch(Exception e) {
	            	System.out.println("Caught Exception: ");
	            	throw new Exception("Connection lost");
	            	//this.receivedMessage = new JSONObject();
	            	//this.receivedMessage.put("status","error");
	            	//this.receivedMessage.put("message","Error: have trouble when parsing received message to JSONObj");
	            	//return this.receivedMessage;
	            }
	    }
	    
	    
		public void closeConnection() {
			 		try {
					    this.dis.close();
					    this.dos.close();
					    this.s.close();
					    }
			 		catch(IOException e) {
			 			System.out.println("Fail to close server-client I/O");
			 		}
				}
			}




