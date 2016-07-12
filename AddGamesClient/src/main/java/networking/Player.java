package networking;

public class Player {
	private String username;
	private String password;
	private int rank;
	private boolean youGoFirst;
	private static Player instance;
    
    
    public static Player getInstance() {
        if (instance == null)
            instance = new Player();
        return instance;
    }
    
	private Player(){
		rank=0;
	}
	
	@Override
	public String toString() {
		return "Player [username=" + username + ", password=" + password + ", rank=" + rank + "]";
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
	public boolean isYouGoFirst() {
		return youGoFirst;
	}
	public void setYouGoFirst(boolean youGoFirst) {
		this.youGoFirst = youGoFirst;
	}
	
}
