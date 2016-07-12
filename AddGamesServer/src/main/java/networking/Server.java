package networking;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import database.DatabaseOperations;
import gui.ServerFrame;


public class Server{
	
	private static Server server = new Server();
	protected List<Connection> activeConnections =
	        Collections.synchronizedList(new LinkedList<Connection>());
	public static ServerSocket serverSocket;
	public static Socket clientSocket;
	//public ArrayList<ClientThread> threads = new ArrayList<ClientThread>();
	//public ServerFrame frame;
	private int port;
	private boolean isRunning;
    protected final ExecutorService THREAD_POOL;
    private ServerFrame frame;
    
    public static Server getInstance() {
        return server;
    }
    
    private Server(){
        THREAD_POOL = Executors.newCachedThreadPool();
        
        frame = new ServerFrame();
		frame.setVisible(true);
		frame.stopServerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				isRunning = false;
				try {
					serverSocket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		
    }
    
   
    public boolean start(int port) {
        if (!isRunning) {
            this.port = port;
           
            try {
                serverSocket = new ServerSocket(port);
            } catch (IOException e) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
                return false;
            }
            isRunning = true;
            
            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    addConnection(new Connection(clientSocket));
                    
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return true;
        }

        return false;
    }
    
    public boolean stop() {
        if (isRunning) {
            isRunning = false;
            activeConnections.clear();
            try {
                serverSocket.close();
          
            } catch (IOException e) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
                return false;
            }
            return true;
        }

        return false;
    }


	 public void addConnection(Connection connection) {
        THREAD_POOL.execute(connection);
        activeConnections.add(connection);
        logMessage("The user " + connection.clientSocket.getInetAddress().getHostAddress() + " has connected");
	 }
	 
	 public void logMessage(final String m){
		SwingUtilities.invokeLater(new Runnable(){

			@Override
			public void run() {
				frame.serverLoggerField.append(m + "\n");
				
			}
		});
	}

	public synchronized String logIn(String username, String password) {	
		Player p = new Player();
		p.setUsername(username);
       p.setPassword(password);
		String res = DatabaseOperations.searchPlayer(p);
		logMessage(username + " s-a logat");
        return res;
	}

	public String signup(String username, String password) {
		Player p = new Player();
		p.setUsername(username);
       p.setPassword(password);
		String res = DatabaseOperations.insertPlayer(p);
		
        return res;
	}

	public void sendMessageToOpponent(String opponent, String message) throws IOException {
		for(int i = 0 ; i< activeConnections.size();i++){
			if(activeConnections.get(i).getUsername().equals(opponent)){
				TransferData sendObject = new TransferData();
				sendObject.setCode("messagereceived");
				sendObject.setMessage(message);
				activeConnections.get(i).out.writeObject(sendObject);
				activeConnections.get(i).out.flush();
				break;
			}
		}
		logMessage(opponent + " ar trebui sa primeasca " + message);
	
	}
	
	public ArrayList<String> getOnlinePlayers(String username){
		ArrayList<String> res = new ArrayList<String>();
		for(Connection con : activeConnections){
			if(!con.getUsername().equals(username)){
				res.add(con.getUsername());
			}
		}
		return res;
		
	}

	public void removeConnection(String username) {
		for(Connection con : activeConnections){
			if(con.getUsername().equals(username)){
				logMessage(username + " has disconnected");
				activeConnections.remove(con);
				break;
			}
		}
	}

	public void sendGameResponseToOpponent(String opponent, String code) throws IOException {
		for(int i = 0 ; i< activeConnections.size();i++){
			if(activeConnections.get(i).getUsername().equals(opponent)){
				TransferData sendObject = new TransferData();
				sendObject.setCode(code);
				
				activeConnections.get(i).out.writeObject(sendObject);
				activeConnections.get(i).out.flush();
				break;
			}
		}
		logMessage(opponent + " ar trebui sa primeasca " + code);
	}

	public void sendGameProposal(String opponent,String currentPlayer) throws IOException {
		for(int i = 0 ; i< activeConnections.size();i++){
			if(activeConnections.get(i).getUsername().equals(opponent)){
				TransferData sendObject = new TransferData();
				sendObject.setCode("game");
				sendObject.setOpponent(currentPlayer);
				sendObject.setUsername(opponent);
				activeConnections.get(i).out.writeObject(sendObject);
				activeConnections.get(i).out.flush();
				break;
			}
		}
		logMessage(opponent + " ar trebui sa primeasca " + " game " + currentPlayer);
		
	}

	public void sendToOthers(TransferData responseObject, String username) throws IOException {
		for(int i = 0 ; i< activeConnections.size();i++){
			if(!activeConnections.get(i).getUsername().equals(username)){
				activeConnections.get(i).out.writeObject(responseObject);
				activeConnections.get(i).out.flush();
				
			}
		}
		
	}

	public void sendToOpponent(TransferData responseObject, String opponent) throws IOException {
		for(int i = 0 ; i< activeConnections.size();i++){
			if(activeConnections.get(i).getUsername().equals(opponent)){
				activeConnections.get(i).out.writeObject(responseObject);
				activeConnections.get(i).out.flush();
				break;
			}
		}
		
	}

}
