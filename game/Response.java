import org.json.simple.JSONObject;

class Response extends JSONObject {

    Response(String status, String type){
        super();
        this.put("status",status);
        this.put("type", type);
    }

    Response(JSONObject json, String status, String type){
        super();
        for (Object i :json.keySet()){
            this.put(i,json.get(i));
        }
        this.put("status",status);
        this.put("type", type);
    }
}
