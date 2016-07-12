package networking;

import static org.junit.Assert.assertEquals;

import java.net.ConnectException;

import org.junit.Test;

import client.ConnectionHandler;

public class ConnectionTest {

	@Test
	public void sendMessageToServerTest(){
		TransferData sendObject = new TransferData();
	    sendObject.setCode("login");
		sendObject.setUsername("test");
		sendObject.setPassword("test");
		Boolean result = false;
		
		try {
			ConnectionHandler.getInstance().sendToServer(sendObject);
		} catch (ConnectException e) {
			result = true;
		} catch (Exception e){
			result = true;
		}finally{
			ConnectionHandler.getInstance().close();
		}
		
		assertEquals(result,true);
	}
	
	@Test
	public void readMessageFromServer(){
		TransferData readObject;
	    
		Boolean result = false;
		try {
			readObject = ConnectionHandler.getInstance().readFromServer();
		} catch (ConnectException e) {
			result = true;
		} catch (Exception e){
			result = true;
		}finally{
			ConnectionHandler.getInstance().close();
		}
		assertEquals(result,true);
	}
}
