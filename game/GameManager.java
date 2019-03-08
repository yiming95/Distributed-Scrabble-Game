import org.json.simple.JSONObject;
import java.io.IOException;
import java.util.HashMap;


/**
 * This class is added to handle player-game interaction
 * -attr games: Record the games by their id
 * -attr registration: Register user to game when join the game
 */
public class GameManager {
    private static HashMap<String,Game> games = new HashMap<>(); // game list <gid, game>
    private static HashMap<String,Game> registration = new HashMap<>(); // register players <uid, game>

    static synchronized void createGame(UserThread u)throws ClientDisconnectException,IOException {

        Game game = new Game();
        System.out.println("creating game instance");
        if (games.get(game.getGId()) != null) {
            JSONObject not = new JSONObject();
            not.put("type",Type.GAME_ERR);
            not.put("message","Game id exists!");
            u.gameNotify(not);
            return;
        }
        games.put(game.getGId(),game);
        System.out.println("Registering game");

        System.out.println("registering players: " + u.getUId());

        register(u,game);
        game.addPlayer(u);
        u.setUserState(UserState.WAITING);

    }

    /**
     * Join an existing game
     * Registering -> adding player
     * @param player who send join request
     * @param gid game id
     */
    static synchronized void joinGame(UserThread player, String gid) throws ClientDisconnectException, IOException{
        Game game = games.getOrDefault(gid, null);

        System.out.println("Game to join: " + game);
        System.out.println("Player: " + player);
        if (registration.get(player.getUId()) != null) {
            if (registration.get(player.getUId()).equals(game)){
                System.out.println("validating player...");
                player.setUserState(UserState.WAITING);
                System.out.println("Player already joined this game");
                JSONObject not = new JSONObject();
                not.put("type",Type.GAME_ERR);
                not.put("message","Player already joined this game");
                player.gameNotify(not);
                return;
            }

        }

        if (game != null) {

            leaveGame(player);
            player.setUserState(UserState.WAITING);
            System.out.println("Setting player status");

            System.out.println("Adding Player to the game");
            game.addPlayer(player);
            register(player,game);
            System.out.println("Registering player for the game");

        } else {
            System.out.println("Joining non existing game");
            player.setUserState(UserState.FREE);
            System.out.println("Setting user to free");
            JSONObject not = new JSONObject();
            not.put("type",Type.GAME_ERR);
            not.put("message","Non-existing room");
            player.gameNotify(not);
        }

    }

    static synchronized void leaveGame(UserThread player)throws IOException,ClientDisconnectException{
        Game game = getGameByUid(player.getUId());
        if (game != null) {
            game.removePlayer(player);
            unregister(player);
            if (game.getPlayers().size() <= 0) {
                endGame(game);
            }
            player.setUserState(UserState.FREE);
        } else {
            player.setUserState(UserState.FREE);
        }


    }

    static synchronized void startGame(UserThread player) throws ClientDisconnectException, IOException{
        Game game = getGameByUid(player.getUId());
        if (game != null) {
            game.startGame();
            if (game.getPlayers().size() > 0) {
                for (UserThread p : game.getPlayers()){
                    p.setUserState(UserState.IN_GAME);
                }
            }


        } else {
        	player.setUserState(UserState.FREE);
            System.out.println("Starting non existing game");
            JSONObject not = new JSONObject();
            not.put("type",Type.GAME_NON_EXIST_ERR);
            not.put("message","Starting non existing game");
            player.gameNotify(not);
        }


    }

    static synchronized Game getGameByUid(String uid){
        return registration.get(uid);

    }
    public static synchronized Game getGameByGid(String gid){
        return games.get(gid);

    }

    public static synchronized HashMap<String,Game> getReg(){
        return registration;
    }

    static synchronized HashMap<String,Game> getGames() {return games;}

    static synchronized void endGame(Game game)throws IOException,ClientDisconnectException{

        JSONObject not = new JSONObject();
        not.put("type",Type.EVENT_GAME_RESULT);
        not.put("winnerID",game.getWinnerID());
        not.put("winnerName",game.getWinnerName());
        games.remove(game.getGId());
        if (game.getPlayers().size()>0) {
            for (UserThread u : game.getPlayers()) {
                u.gameNotify(not);
                registration.remove(u.getUId());
                u.setUserState(UserState.FREE);
            }
        }
    }

    static synchronized void register(UserThread u, Game g) {
        registration.put(u.getUId(),g);

    }

    static synchronized void unregister(UserThread u) {
        try{
            registration.remove(u.getUId());
        } catch (Exception e) {

        }

    }

    static synchronized void place(UserThread u, int x, int y, String c) throws ClientDisconnectException,IOException{
        System.out.println(u.getUId() + GameManager.getGameByUid(u.getUId()));
        Game g = GameManager.getGameByUid(u.getUId());
        if (g != null) {
            g.place(x,y,c.charAt(0),u);
        } else {
            JSONObject resp = new JSONObject();
            resp.put("error",Type.GAME_EXITED_ERR);
            u.gameNotify(resp);
        }
    }
    
    static synchronized void highlightCell(UserThread u, JSONObject highlightMsg) throws ClientDisconnectException,IOException{
    	Game g = GameManager.getGameByUid(u.getUId());
    	if (g != null) {
    		g.broadcastNot(highlightMsg);
    	} else {
            JSONObject resp = new JSONObject();
            resp.put("error",Type.GAME_EXITED_ERR);
            u.gameNotify(resp);
        }
    }

    static synchronized void highlight(UserThread u, String h) throws ClientDisconnectException,IOException{
        Game g = GameManager.getGameByUid(u.getUId());
        if (g != null) {
            g.highlight(h, u);
        } else {
            JSONObject resp = new JSONObject();
            resp.put("error",Type.GAME_EXITED_ERR);
            u.gameNotify(resp);
        }
    }

    static synchronized void vote(UserThread u, boolean agree) throws ClientDisconnectException,IOException{
        Game g = GameManager.getGameByUid(u.getUId());
        if (g != null) {
            g.vote(agree, u);
        } else {
            JSONObject resp = new JSONObject();
            resp.put("error",Type.GAME_EXITED_ERR);
            u.gameNotify(resp);
        }
    }
}
