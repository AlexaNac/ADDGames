package networking;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import database.DatabaseOperations;

public class Connection extends Thread{
	
	private String username;
	protected Socket clientSocket;
	private ObjectInputStream in = null;
	public ObjectOutputStream out = null;

    protected volatile boolean threadRunning;
    protected boolean activeConnection;
    private boolean identityConfirmed;
    
    public TransferData requestObject;

	public Connection(Socket clientSocket) {
		this.clientSocket = clientSocket;
		this.username = "Anonymous";
        this.identityConfirmed = false;
	}

	
	public void run() {
		threadRunning = true;
		String request = null;
		TransferData responseObject;
		try{
			in = new ObjectInputStream(clientSocket.getInputStream());
			out = new ObjectOutputStream(clientSocket.getOutputStream());
			
			while(!identityConfirmed){
				
				requestObject = (TransferData) in.readObject();
				String password = null;
				String username = null;
				username = requestObject.getUsername();
				password = requestObject.getPassword();
				responseObject = new TransferData();
				if (requestObject.getCode().equals("login")){
										
					String loginResult = Server.getInstance().logIn(username,password);
					if(loginResult == "OK"){
						identityConfirmed = true;
						this.username = username;
					}
					
					responseObject.setCode(loginResult);
					responseObject.setRank(DatabaseOperations.getRank(username));
					out.reset();
					out.writeObject(responseObject);
					out.flush();
				}
				if(requestObject.getCode().equals("signup")){
					
					String signupResult = Server.getInstance().signup(username,password);
					if(signupResult == "OK"){
						identityConfirmed = true;
						this.username = username;
					}
					responseObject.setCode(signupResult);
					
					out.reset();
					out.writeObject(responseObject);
					out.flush();
				}
				
				if(identityConfirmed){
					
					responseObject = new TransferData();
					responseObject.setCode("newplayer");
					responseObject.setUsername(this.username);
					Server.getInstance().sendToOthers(responseObject,this.username);
				}
			}
			
			while(true){
				
				//request = (String) in.readObject();
				requestObject = (TransferData) in.readObject();
				
				if(requestObject.getCode().equals("word")){
					responseObject = new TransferData();
					
					responseObject.setCode("word");
					String[] word = DatabaseOperations.getWordForHangman((int) Math.floor(Math.random() * (5 - 1 + 1)) + 1);
					responseObject.setMessage("WRONG");
					responseObject.setPassword("W _ _ _ G");
					if(word != null){
						responseObject.setMessage(word[0]);
						responseObject.setPassword(word[1]);
					}
					out.reset();
					out.writeObject(responseObject);
					out.flush();
				}
				
				if(requestObject.getCode().equals("back")){
					responseObject = new TransferData();
					
					responseObject.setCode("gogo");
					if(requestObject.getRank() > 0)
						System.out.println(DatabaseOperations.setRank(this.username, requestObject.getRank()));
					out.reset();
					out.writeObject(responseObject);
					out.flush();
				}
				if(requestObject.getCode().equals("opponentnow")){
					System.out.println(requestObject);
					String gameResult = gameStatus(requestObject.getGameStatus(),requestObject.getSymbol().equals("X")?1:2);
					if(gameResult.equals("youwon")){
						//send message to opponent, send message to this
						responseObject = new TransferData();
						responseObject.setCode("youwon");
						out.reset();
						out.writeObject(responseObject);
						out.flush();
						responseObject.setCode("opponentwon");
						Server.getInstance().sendToOpponent(responseObject,requestObject.getOpponent());
					}else if(gameResult.equals("continue")){
						//the game is not over yet
						//send yourturn to opponent
						
						responseObject = new TransferData();
						responseObject.setCode("yourturn");
						responseObject.setGameStatus(requestObject.getGameStatus());
						Server.getInstance().sendToOpponent(responseObject,requestObject.getOpponent());
					}else{
						//is a tie
						//send to both
						responseObject = new TransferData();
						responseObject.setCode("tie");
						out.reset();
						out.writeObject(responseObject);
						out.flush();
						Server.getInstance().sendToOpponent(responseObject,requestObject.getOpponent());
					}
				}
				
				if(requestObject.getCode().equals("onlineplayers")){
					ArrayList<String> onlinePlayers = new ArrayList<String>();
					onlinePlayers = Server.getInstance().getOnlinePlayers(username);
					responseObject = new TransferData();
					responseObject.setOnlinePlayers(onlinePlayers);
					responseObject.setCode("onlineplayers");
					out.reset();
					out.writeObject(responseObject);
					out.flush();
				}
				
				if(requestObject.getCode().equals("game")){
					//trebuie sa i trimitem oponentului mesaj de "vrei sa joci cu this?
					Server.getInstance().sendGameProposal(requestObject.getOpponent(),this.username);
				}
				if(requestObject.getCode().equals("signout")){
					break;
				}
				if(requestObject.getCode().equals("sendto")){
					Server.getInstance().sendMessageToOpponent(requestObject.getOpponent(),requestObject.getMessage());
				}
				if(requestObject.getCode().equals("startgame")){
					out.reset();
					responseObject = new TransferData();
					responseObject.setCode("gogo");
					
					out.writeObject(responseObject);
					out.flush();
				}
				if(requestObject.getCode().equals("no")){
					Server.getInstance().sendGameResponseToOpponent(requestObject.getOpponent(),requestObject.getCode());
				}
				if(requestObject.getCode().equals("yes")){
					Server.getInstance().sendGameResponseToOpponent(requestObject.getOpponent(),requestObject.getCode());
				}
				

			}
			responseObject = new TransferData();
			responseObject.setCode("playeroffline");
			responseObject.setUsername(username);
			Server.getInstance().sendToOthers(responseObject,username);
			Server.getInstance().removeConnection(username);
			signout();
		}catch(IOException | ClassNotFoundException e){
			e.printStackTrace();
		}
		
		
	}

	private String gameStatus(int[] gs,int s) {
		//calculam daca a castigat
		String res = null;
		if((gs[0] == gs[1] && gs[1] == gs[2] && gs[2] == s) || 
				(gs[3] == gs[4] && gs[4] == gs[5] && gs[5] == s) || 
				(gs[6] == gs[7] && gs[7] == gs[8] && gs[8] == s) ){
			res = "youwon";
		}else if((gs[0] == gs[3] && gs[3] == gs[6] && gs[6] == s) || 
				(gs[1] == gs[4] && gs[4] == gs[7] && gs[7] == s) || 
				(gs[2] == gs[5] && gs[5] == gs[8] && gs[8] == s)){
			res = "youwon";
		}else if((gs[0] == gs[4] && gs[4] == gs[8] && gs[8] == s) || 
				(gs[2] == gs[4] && gs[4] == gs[6] && gs[6] == s)){
			res = "youwon";
		}
		//same thing for opponent
		s = (s==1)?2:1;
		if((gs[0] == gs[1] && gs[1] == gs[2] && gs[2] == s) || 
				(gs[3] == gs[4] && gs[4] == gs[5] && gs[5] == s) || 
				(gs[6] == gs[7] && gs[7] == gs[8] && gs[8] == s) ){
			res = "opponentwon";
		}else if((gs[0] == gs[3] && gs[3] == gs[6] && gs[6] == s) || 
				(gs[1] == gs[4] && gs[4] == gs[7] && gs[7] == s) || 
				(gs[2] == gs[5] && gs[5] == gs[8] && gs[8] == s)){
			res = "opponentwon";
		}else if((gs[0] == gs[4] && gs[4] == gs[8] && gs[8] == s) || 
				(gs[2] == gs[4] && gs[4] == gs[6] && gs[6] == s)){
			res = "opponentwon";
		}
		if(res == null)
		{for(int i = 0; i < 9; i++){
				if(gs[i] == 0){//not completed yet
					res = "continue";
					break;
				}
			}
		}
		if(res == null){
			res = "tie";
		}
		return res;
		
	}


	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public void signout()
	{
		try{
			in.close();
			out.close();
			clientSocket.close();
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}

}
