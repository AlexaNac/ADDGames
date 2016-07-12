package networking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class TransferData implements Serializable{
	public static final long serialVersionUID = 1L;

	private String username;
	private String password;
	private String opponent;
	private String message;
	private String code;
	private ArrayList<String> onlinePlayers;
	private int[] gameStatus;
	private String symbol;
	private int rank;
	
	public TransferData(){
		setGameStatus(new int[9]);
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
	public String getOpponent() {
		return opponent;
	}
	public void setOpponent(String oponent) {
		this.opponent = oponent;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public ArrayList<String> getOnlinePlayers() {
		return onlinePlayers;
	}
	@Override
	public String toString() {
		return "TransferData [username=" + username + ", password=" + password + ", opponent=" + opponent + ", message="
				+ message + ", code=" + code + ", onlinePlayers=" + onlinePlayers + ", gameStatus="
				+ Arrays.toString(gameStatus) + ", symbol=" + symbol + ", rank="+ rank + "]";
	}
	public void setOnlinePlayers(ArrayList<String> onlinePlayers) {
		this.onlinePlayers = onlinePlayers;
	}
	public int[] getGameStatus() {
		return gameStatus;
	}
	public void setGameStatus(int[] gameStatus) {
		this.gameStatus = gameStatus;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
}
