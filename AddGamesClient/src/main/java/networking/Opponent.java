package networking;

public class Opponent {
	private String username;
	private static Opponent instance;
    
    
    public static Opponent getInstance() {
        if (instance == null)
            instance = new Opponent();
        return instance;
    }
	private Opponent(){
		this.username = "mai";
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
}
