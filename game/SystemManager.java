import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

class SystemManager {

    static JSONObject systemState(){
        JSONObject state = new JSONObject();
        ArrayList<UserThread> users = UserManager.getUsers();
        Collection<Game> games = GameManager.getGames().values();
        ArrayList<JSONObject> users_ser = new ArrayList<>();
        ArrayList<JSONObject> games_ser = new ArrayList<>();
        if (users.size()>0){
            for (UserThread u : users){
                users_ser.add(u.serializer());
            }
        }

        if (games.size() > 0 ){
            for (Game g : games){
                JSONObject gsr = g.gameState();
                gsr.remove("map");
                games_ser.add(gsr);
            }
        }

        state.put("users",users_ser);
        state.put("games",games_ser);

        return state;

    }
}
