
public class ClientPlayer {
	public String userName;
	public String uid;
	public String userState;
	
	public ClientPlayer(String uid, String userName){
		this.uid = uid;
		this.userName = userName;
		this.userState = UserState.FREE;
	}
	
	@Override
	public String toString(){
		return  this.userName;
	}
}
