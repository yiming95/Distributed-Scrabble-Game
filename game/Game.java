
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import com.google.gson.*;

/**
 * The game instance. One thread per instance
 */
public class Game{
    private String id;
    private ArrayList<UserThread> players;
    private Integer turn; // the player taking action
    private Integer round; // Number of rounds
    private char [][]map;
    private int stage;
    private int pass;
    private int scoresToAdd;
    private HashMap<UserThread,Boolean> voted;
    private HashMap<UserThread,Integer> scores;

    public Game(){
        this.id = "1";//UUID.randomUUID().toString();
        this.players = new ArrayList<>();
        this.map = new char[20][20];
        this.turn = 0;
        this.stage = 0;
        this.voted = new HashMap<>();
        this.round = 0;
        this.scores = new HashMap<>();
        this.pass = 0;


    }

    /**
     * Broadcast the game state and notifications to players
     */
    private synchronized void broadcastState(){
        try{
            for (UserThread p : this.players){
                JSONObject not = this.gameState();
                not.put("type",Type.GAME_STATE);

                p.gameNotify(not);
            }
        }catch (Exception e) {
            System.out.println("Err in broadcast: "+ e.getMessage());
        }
    }

    public void broadcastNot(JSONObject not) {
        try{
            for (UserThread p : this.players){
                p.gameNotify(not);
            }
        }catch (Exception e) {
            System.out.println("Err in broadcast: "+ e.getMessage());
        }
    }



    /**
     * Add a player to game when created or someone join
     * 1. check if the game has started
     * 2. add player to the game list
     * 3. register the player and the game
     * 4. Notify other players that someone joined the game
     * 5. broadcast the new game state to each player
     * @param player player to add
     */
    synchronized void addPlayer(UserThread player){
        try{
            if(this.stage == GameStage.HIBERNATED){
                this.players.add(player);
                GameManager.register(player,this);
                JSONObject not = new JSONObject();
                not.put("type",Type.NOTIFICATION);
                not.put("message","Player " + player.getUserName() + " joined the game");
                broadcastNot(not);
                broadcastState();


            } else {
                JSONObject not = new JSONObject();
                not.put("type",Type.GAME_ERR);
                not.put("message","Cannot join started game!");
                player.gameNotify(not);
            }
        } catch (Exception e) {
            System.out.println("Error in addPlayer: " + e.getMessage());
        }


    }


    /**
     * Remove a player when he/she quit or lose connection
     * 1. Remove the player from player list
     * 2. Unregister the player from the game
     * 3. Adjust the turn
     *      - If the current turn player leaves, give turn to its next player.
     *      - If the player left is after the turn, it does not affect anything :)
     *      - If the player left is before the turn, shift the turn by 1 to left
     * 4. Notify other players that someone has left
     * 5. Broadcast new game state
     * @param player player to remove
     */
    synchronized void removePlayer(UserThread player) {
        try{
            int index = this.players.indexOf(player);
            this.players.remove(player);
            GameManager.unregister(player);

            if (this.turn == index) {
                if (this.turn >= this.players.size()) {
                    this.turn = 0;
                    this.round += 1;
                }
                this.stage = GameStage.PLACE;
                this.scoresToAdd = 0;
            } else if (index < this.turn) {
                this.turn -= 1;
            }

            JSONObject not = new JSONObject();
            not.put("type",Type.NOTIFICATION);
            not.put("message","Player " + player.getUserName() + " leaved the game");

            broadcastNot(not);
            broadcastState();

        } catch (Exception e) {
            System.out.println("Error in removePlayer: " + e.getMessage());
        }
    }


    /**
     * Move the game to next turn
     */
    private synchronized void nextTurn(){
        try{
            this.scoresToAdd = 0;
            
            this.stage = GameStage.PLACE;
            this.voted.clear();
            if(this.turn >= this.players.size()-1) {
            	this.pass = 0;
                this.round += 1;
                this.turn = 0;
            } else {
                this.turn += 1;
            }

        } catch (Exception e) {
            System.out.println("Err In next turn: " +e.getMessage());
        }


    }


    /**
     * Player win a score
     */
    private synchronized void score(){
        try{
            Integer thisPlayerScores = this.scores.getOrDefault(this.players.get(this.turn),0);
            thisPlayerScores += this.scoresToAdd;
            this.scoresToAdd = 0;
            System.out.println(this.players.get(this.turn)+"||"+thisPlayerScores);
            this.scores.put(this.players.get(this.turn),thisPlayerScores);

        } catch (Exception e) {
            System.out.println("Err at score: " + e.getMessage());
        }


    }

    /**
     * Place a character
     *  1. Check if pass. Giving negative number for any one of x and y means pass
     *  2. If all pass, end the game
     *  3. If the move is valid, update the map and broadcast the event
     *  4. Broadcast the gamestate
     *
     * @param x x
     * @param y y
     * @param c character
     */
    synchronized void place(int x, int y, char c, UserThread u) throws IOException, ClientDisconnectException{
        /*
        If both x and y are negative numbers, the player passes
        * */
    	if (x < 0  || y < 0){
    		if (this.stage==GameStage.PLACE && this.players.indexOf(u) ==this.turn) {
        		// Skipping 
        		
                this.pass += 1;
                    
                
                if (this.pass >= this.players.size()) {
                    /*
                    If all the players pass, end the game by broadcasting end event
                    * */
                    GameManager.endGame(this);
                    System.out.println("Exit");
                    JSONObject not = new JSONObject();
                    not.put("type",Type.EVENT_GAME_END);

                        for (UserThread p : this.players){
                            p.gameNotify(not);
                        }

                    return;
                }
                
            	this.nextTurn();
            	this.broadcastState();
            	return;
                
        	}
    	} else {
    		
    		
    		char history = this.map[x][y];
            try{

                if (this.stage==GameStage.PLACE && this.map[x][y] == '\u0000' && this.players.indexOf(u) ==this.turn) {
                    System.out.println("User: "+u.getUserName() + " is placing " + c);
                    map[x][y] = c;
                    JSONObject not = new JSONObject();
                    not.put("type",Type.EVENT_PLACE_CHAR);
                    not.put("x",x);
                    not.put("y",y);
                    not.put("c",((Character)c).toString());
                    this.broadcastNot(not);
                    this.stage = GameStage.HIGHLIGHT;


                }
                else {
                    JSONObject not = new JSONObject();
                    not.put("type",Type.GAME_ERR);
                    not.put("message","Invalid move");
                    u.gameNotify(not);
                }

            } catch (Exception e) {
                System.out.println("Err at Game/place: "+e.getMessage());
                this.map[x][y] = history;

            }
            this.broadcastState();
    		
    	}
    	
        
        

    }


    /**
     * One player set highlight and sync it to other players
     * @param highlights
     * @param u
     * @throws ClientDisconnectException
     * @throws IOException
     */
    synchronized void highlight(String highlights, UserThread u) throws ClientDisconnectException,IOException{
        this.scoresToAdd = highlights.length();
            if (this.stage == GameStage.HIGHLIGHT && this.players.indexOf(u) ==this.turn){
                JSONObject not = new JSONObject();
                not.put("highlights",highlights);
                not.put("type",Type.EVENT_HIGHLIGHT);
                for (UserThread p : this.players){
                    p.gameNotify(not);
                }
                this.stage = GameStage.VOTE;
            } else {
                JSONObject not = new JSONObject();
                not.put("type",Type.GAME_ERR);
                not.put("message","Invalid move");
                u.gameNotify(not);
            }


        this.broadcastState();

    }

    /**
     * Vote stage
     * 1. Check if vote is a valid operation in current stage
     * 2. Check if the player has voted already
     * 3. Notify other players that someone has voted
     * 4. Calculate the vote and determine the score
     * 5. If Anything goes wrong, undo the vote and broadcast the game state
     * @param agree
     * @param u
     */
    synchronized void vote(boolean agree, UserThread u) {
        try{

            if (this.stage != GameStage.VOTE) {
                JSONObject not = new JSONObject();
                not.put("type",Type.GAME_ERR);
                not.put("message","Invalid Move");
                u.gameNotify(not);
                return;
            }

            if (this.voted.getOrDefault(u,null) == null) {
                this.voted.put(u,agree);
                JSONObject not = new JSONObject();
                not.put("type",Type.EVENT_VOTE);
                for (UserThread voter : this.voted.keySet()) {
                    not.put(voter.getUserName(),this.voted.get(voter));
                }

                for (UserThread p : this.players){
                    p.gameNotify(not);
                }
                if (this.voted.size() == this.players.size()){
                    // Calculate the scores and go to next turn
                    System.out.println("calculating score...");
                    int agreeNum = 0;
                    for (boolean a : this.voted.values()) {
                        System.out.println(" Agree! ");
                        if (a) agreeNum += 1;
                    }
                    if (agreeNum == this.voted.size()){
                        System.out.println("get score!");
                        this.score();

                    }
                    this.nextTurn();
                }
            } else {
                JSONObject not = new JSONObject();
                not.put("type",Type.GAME_ERR);
                not.put("message","You have voted already");
                u.gameNotify(not);

            }

        } catch (Exception e) {
            System.out.println("Err at Game/vote: "+e.getMessage());
            this.voted.remove(u);
        }
        this.broadcastState();

    }


    /**
     * Get serialized game state
     * @return
     */
    synchronized JSONObject gameState(){
        JSONObject state = new JSONObject();
        Gson gson = new Gson();

        state.put("map",gson.toJson(this.map).toString());
        state.put("turn",this.turn);
        state.put("turnPlayerId",this.players.get(this.turn).getUId());
        state.put("turnPlayerName",this.players.get(this.turn).getUserName());
        state.put("gid",this.id);
        ArrayList<JSONObject> users_ser = new ArrayList<>();
        for (UserThread u : this.players){
            JSONObject usr = u.serializer();
            usr.put("score",this.scores.getOrDefault(u,0));
            users_ser.add(usr);
        }
        state.put("players",users_ser);
        state.put("stage",this.stage);
        state.put("round",this.round);

        return state;

    }

    String getWinnerID(){
        int maxScore = 0;
        String maxPlayer = "null";
        if(this.scores.size() > 0) {
            for (UserThread u : this.scores.keySet()) {
                if (this.scores.get(u) > maxScore) {
                    maxScore = this.scores.get(u);
                    maxPlayer = u.getUId();
                }
            }
        }
        return maxPlayer;
    }

    String getWinnerName(){
        int maxScore = 0;
        String maxPlayer = "null";
        if(this.scores.size() > 0) {
            for (UserThread u : this.scores.keySet()) {
                if (this.scores.get(u) > maxScore) {
                    maxScore = this.scores.get(u);
                    maxPlayer = u.getUserName();
                }
            }
        }
        return maxPlayer;
    }



    /**
     * game logic goes here
     */
    synchronized void startGame() throws IOException, ClientDisconnectException {
        for (UserThread u : this.players) {
            this.scores.put(u,0);
            u.setUserState(UserState.IN_GAME);
        }
        this.stage = GameStage.PLACE;


        System.out.println("Game running... Players:");
        JSONObject not = new JSONObject();
        not.put("type",Type.EVENT_GAME_START);

        this.broadcastNot(not);
        this.broadcastState();

    }

    synchronized ArrayList<UserThread> getPlayers() {
        return this.players;
    }

    synchronized String getGId(){
        return this.id;
    }


}
