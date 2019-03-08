import java.util.ArrayList;
import java.util.HashMap;

public class UserManager{


    private static HashMap<String,UserThread> instance = new HashMap<>();

    public static synchronized HashMap<String, UserThread> get_thread() {
        return instance;
    }

    public static UserThread getUserById(String id){
        return instance.get(id);
    }

    public static ArrayList<UserThread> getUsers() {
        ArrayList<UserThread> users = new ArrayList<>();
        if (instance.values().size() > 0) {
            for( UserThread u :instance.values()){
                users.add(u);
            }
        }

        return users;

    }

    public static synchronized void add(String id, UserThread task) {
        instance.put(id,task);
    }

    public static synchronized void remove(String id) {
        instance.remove(id);
    }

    public static void dispTasks(){
        for(String s:instance.keySet()){
            System.out.println(s);
        }
    }

}