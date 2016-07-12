package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

import networking.Player;
import networking.TransferData;

public class ConnectionHandler {

	private static ConnectionHandler instance = new ConnectionHandler();
    
	private Socket clientSocket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private String IP = "192.168.43.63";
	private int port = 60110;
	private ConnectionHandler() {
        clientSocket = null;
        out = null;
        in = null;
    }
	
	public static ConnectionHandler getInstance() {
		if(instance == null)
			instance = new ConnectionHandler();
        return instance;
    }
	
	private void connectToServer(){
		try
		{
			clientSocket = new Socket(IP, port);
			
			out = new ObjectOutputStream(clientSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(clientSocket.getInputStream());
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void close()
	{
		try
		{
			if (out != null)
				out.close();
			if (in != null)
				in.close();
			if(clientSocket != null)
				clientSocket.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public TransferData readFromServer() throws ClassNotFoundException, IOException{
		if (clientSocket == null)
			connectToServer();
		TransferData result = null;
		synchronized (in) {
			result = (TransferData) in.readObject();
		}
		
		return result;
	}
	public void sendToServer(TransferData sendObject) throws SocketException, IOException{
		if(clientSocket == null){
			connectToServer();
		}
		
		out.writeObject(sendObject);
		out.flush();
		
		
	}
	
	public void sendLoginRequest() throws IOException {
		if(clientSocket == null){
			connectToServer();
		}
		TransferData sendObject = new TransferData();
		sendObject.setCode("login");
		sendObject.setUsername(Player.getInstance().getUsername());
		sendObject.setPassword(Player.getInstance().getPassword());
		
		out.writeObject(sendObject);
		out.flush();
	}
	
	public void signout() throws IOException {
		if(clientSocket != null){
			TransferData send = new TransferData();
			send.setCode("signout");
			sendToServer(send);
		}
		close();
		instance = null;
	}
	
}
