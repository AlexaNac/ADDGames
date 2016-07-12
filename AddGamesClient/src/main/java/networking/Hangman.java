package networking;

public class Hangman {
	
	private  String word;
	private static int lives = 5;
	private String wtb;
	private static Hangman instance;
	
	 public static Hangman getInstance() {
        if (instance == null)
            instance = new Hangman();
        return instance;
    }
	 private Hangman(){
		 
	 }
	 
	public String getWtb() {
		return wtb;
	}
	public void setWtb(String wtb2) {
		wtb = wtb2;
	}
	public String getWord() {
		return word;
	}
	public void setWord(String word2) {
		word = word2;
	}
	public int getLives() {
		return lives;
	}
	public void setLives(int lives2) {
		lives = lives2;
	}
}
