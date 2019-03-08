
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;


/**
 * This class handles connected user instances (one thread per connection)
 * -attr ts: The socket instance
 * -attr id: User id. Use UUID to generate unique id. "1" for demo for convenience
 * -attr dis: To recv msg from server
 * -attr dos: To send msg to server
 * -attr userName: Username
 * -attr userState: refer to Utils.UserState
 */
public class UserThread extends Thread{
    private Socket ts;
    private String id; // Unique id
    private DataInputStream dis;
    private DataOutputStream dos;
    private String userName;
    private String userState;

    /**
     * Constructor
     * @param s Socket
     */
    UserThread(Socket s){
        this.ts = s;
        this.id = UUID.randomUUID().toString();
        this.userState = UserState.FREE;
        try{
            this.dis = new DataInputStream(s.getInputStream());
            this.dos = new DataOutputStream(s.getOutputStream());
            Response welcome = new Response("success",Type.PLAIN);
            welcome.put("uid",id);
            welcome.put("message","Welcome!");
            try{
                this.writeMessage(welcome.toJSONString());
            } catch (ClientDisconnectException c) {
                this.ts.close();
                this.dis.close();
                this.dos.close();
                UserManager.remove(this.id);
                System.out.println("closed");
            }

        } catch (IOException e){
            System.out.print(e.getMessage());
        }


    }

    /**
     * Receive notification from game instance
     * @param not: The notification json object
     *
     */
    void gameNotify(JSONObject not) throws IOException,ClientDisconnectException {

        Response un = new Response(not,"success",not.get("type").toString());
        //System.out.println(un.toJSONString());
        this.writeMessage(un.toJSONString());
    }


    /**
     * Write message to Data output stream
     * @param s: The message, which is a json string
     */
    private void writeMessage(String s) throws ClientDisconnectException {

        try{
            ObjectMapper mapper = new ObjectMapper();
            String pret = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(JSONValue.parse(s));
            System.out.println(pret);
            System.out.println(s);
            System.out.println(JSONValue.parse(s));
            this.dos.writeUTF(pret);
            this.dos.flush();
        }  catch (IOException e) {
            throw new ClientDisconnectException("IOExption: " + e.getMessage());

        } catch (Exception e) {
            throw new ClientDisconnectException("Unknown Exception: " + e.getMessage());

        }

    }


    /**
     * Read msg from Data input stream
     */
    private String readMessage() throws ClientDisconnectException {

        try{
            System.out.println("reading msg");
            String msg = this.dis.readUTF();
            System.out.println(msg);

            return msg;
        } catch (IOException e) {
            System.out.print("IOExption: " + e.getMessage());

            throw new ClientDisconnectException("IOException: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unknown Exception: " + e.getMessage());
            throw new ClientDisconnectException("Unknown Exception: " + e.getMessage());
        }

    }


    /**
     *Set username for current user
     * @param name Username
     * @throws ClientDisconnectException End listening when connection loss
     */
    private void setUserName(String name) throws ClientDisconnectException{
        this.userName = name;
        Response r = new Response("success",Type.CONFIG);
        r.put("userName",name);
        this.writeMessage(r.toJSONString());
        broadcastSystemState();
    }


    /**
     * Fetch the state of the system
     */
    private void fetchState() throws ClientDisconnectException{
        JSONObject sysState = SystemManager.systemState();
        Response r = new Response(sysState,"success",Type.SYS_STATE);
        String resp = r.toJSONString();
        this.writeMessage(resp);
    }

    void broadcastSystemState() throws ClientDisconnectException {
        JSONObject sysState = SystemManager.systemState();
        Response r = new Response(sysState,"success",Type.SYS_STATE);
        String resp = r.toJSONString();
        ArrayList<UserThread> ul  = UserManager.getUsers();
        if (ul.size() > 0) {
            for (UserThread u : ul) {
                if (u.userState != UserState.IN_GAME) {
                    u.writeMessage(resp);
                }

            }
        }
    }


    /**
     * Create a game without starting it.
     */
    private void createGame() throws ClientDisconnectException,IOException{

        GameManager.createGame(this);
        updateUserState();
        broadcastSystemState();
    }


    /**
     * Join a non-started game
     * TODO: detect the game status. An on-going game is not available for joining
     * @param gid game id
     */
    private void joinGame(String gid) throws IOException, ClientDisconnectException{
        System.out.println("joining game");
        GameManager.joinGame(this,gid);
        updateUserState();
        broadcastSystemState();

    }


    /**
     * Start the game
     * TODO: Vote for start
     */
    private void startGame() throws IOException,ClientDisconnectException{

        this.userState = UserState.IN_GAME;
        GameManager.startGame(this);
        updateUserState();

    }


    /**
     * Player place a character at a position
     * @param x x int
     * @param y y int
     * @param c char
     */
    private void place(int x, int y, String c) throws ClientDisconnectException,IOException{
        GameManager.place(this,x,y,c);
    }
    
    private void highlightCell(JSONObject highlightMsg) throws ClientDisconnectException,IOException{
    	GameManager.highlightCell(this, highlightMsg );
    }
    
    


    /**
     * Player highlight characters
     * @param h highlight, parsed and encoded on client only
     */
    private void highlight(String h) throws ClientDisconnectException, IOException{
        GameManager.highlight(this,h);
    }


    /**
     * Vote for highlighted word
     * @param agree agree this word or not
     */
    private void vote(boolean agree) throws ClientDisconnectException, IOException{
        GameManager.vote(this,agree);
    }
    private void leaveGame() throws ClientDisconnectException,IOException {
        GameManager.leaveGame(this);
        this.updateUserState();
        broadcastSystemState();
    }

    private void sendInvitation(String uid, String gid) throws ClientDisconnectException, IOException {
        UserThread u = UserManager.getUserById(uid);
        if (u!= null && u.getUserState().equals(UserState.FREE)){
            Response resp = new Response("success",Type.INVITATION);
            resp.put("fromUsername",this.userName);
            resp.put("fromId",this.id);
            resp.put("gid",gid);
            u.writeMessage(resp.toJSONString());
        }
    }


    /**
     * Searialize the user object
     * @return json
     */
    JSONObject serializer() {
        JSONObject json = new JSONObject();
        json.put("id",this.id);
        json.put("username",this.userName);
        json.put("userstate",this.userState);
        Game inGame = GameManager.getGameByUid(this.id);
        if (inGame != null) {
            json.put("gid",inGame.getGId());
        } else {
            json.put("gid","null");
        }

        return json;
    }

    public void updateUserState() throws IOException,ClientDisconnectException{
        Response resp = new Response(this.serializer(),"success",Type.USER_STATE);
        this.writeMessage(resp.toJSONString());
    }


    /**
     * Handle incoming requests
     */
    private void handle() throws Exception{
        /**
         * Handle incoming request based on method field
         */

            String msg = this.readMessage();
            JSONObject parsed = (JSONObject) JSONValue.parse(msg);
            String res;
            switch ((String)parsed.get("method")){
                case "setUserName":
                    this.setUserName((String)parsed.get("userName"));
                    break;
                case "fetchSystemState":
                    this.fetchState();
                    break;
                case "createGame":
                    //System.out.print((ArrayList<String>) JSONValue.parse(parsed.get("players").toString()));
                    this.createGame();
                    break;
                case "joinGame":
                    this.joinGame((String)parsed.get("gid"));
                    break;
                case "startGame":
                    this.startGame();
                    break;
                case "quitGame":
                    this.leaveGame();
                    break;
                case "place":
                    this.place(Integer.parseInt((String)parsed.get("x")),
                            Integer.parseInt((String)parsed.get("y")),
                            (String)parsed.get("c"));
                    break;
                case "highlight":
                    this.highlight((String)parsed.get("highlight"));
                    break;
                case "vote":
                    this.vote((boolean)parsed.get("agree"));
                    break;
                case "invite":
                    this.sendInvitation((String)parsed.get("uid"),(String)parsed.get("gid"));
                    break;
                case "highlightCell":
                	JSONObject resp = parsed;
                	resp.put("type", Type.EVENT_HIGHLIGHT_CELL);
                	parsed.remove("method");
                	this.highlightCell(parsed);
                	
                	break;




                default:
                    Response r = new Response("error",Type.SYS_ERR);
                    r.put("message","unknown format");
                    this.writeMessage(r.toJSONString());
                    break;

            }



    }



    void setUserState(String s){
        this.userState = s;
    }

    public String getUserState(){
        return this.userState;
    }

    String getUId(){
        return this.id;
    }

    String getUserName(){
        return this.userName;
    }


    public void run(){
        try{
            this.updateUserState();
            while(true){

                if(this.ts == null) {
                    // If the socker is null, remove from task manager
                    UserManager.remove(this.id);
                    throw new Exception("connect lost");
                }

                try{
                    this.handle();
                }
                catch (ClientDisconnectException ce){
                    this.disconnHandler();
                    break;
                }
                catch (Exception e) {
                    System.out.println(e.getMessage());
                    Response r = new Response("error",Type.UNKNOWN_ERR);
                    r.put("message",e.getMessage());
                    this.writeMessage(r.toJSONString());



                }

            }

        } catch (Exception e ) {
            UserManager.remove(this.id);
            System.out.println("Exception: " + e.getMessage());
        }
    }


    /**
     * Handles unexpected disconnect
     */
    private void disconnHandler() {
        try{
            this.ts.close();
            this.dis.close();
            this.dos.close();
            GameManager.leaveGame(this);
            UserManager.remove(this.id);
            System.out.println("closed");
        } catch (IOException e) {
            System.out.println("Error in disconnHandler: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Fatal Error");
        }

    }
}

