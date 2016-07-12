package gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import client.ConnectionHandler;
import networking.Opponent;
import networking.Player;
import networking.TransferData;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class TicTacToePanel extends JPanel {

	private final MainFrame rootFrame;
	private JTextArea messageArea;
	private JButton sendButton;
	private JPanel gamePanel;
	private JTextArea chatArea;
	private JLabel playerNameLabel;
	private JLabel opponentNameLabel;
	private JLabel vsLabel;
	private JButton backButton;
	private boolean listen;
	
	private TransferData sendMessage;
	private TransferData sendGameMove;
	private TransferData messageRecieved;
	private JButton button1;
	private JButton button2;
	private JButton button3;
	private JButton button4;
	private JButton button5;
	private JButton button6;
	private JButton button7;
	private JButton button8;
	private JButton button9;
	private boolean yourTurn;
	private String symbol;
	private JLabel infoLabel;
	private int[] gameStatus;
	private JScrollPane scrollChat;
	private JScrollPane scrollMessage;
	private String message;
		
	/**
	 * Create the panel.
	 */
	public TicTacToePanel(MainFrame rootFrame) {
		
		this.rootFrame = rootFrame;
		this.rootFrame.setTitle("Tic Tac Toe Game");
		this.rootFrame.setSize(705,550);
		//this.rootFrame.setResizable(false);
		setBounds(100, 100, 705, 550);
		this.setBackground(new Color(0, 0, 51));
		this.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		this.symbol = (Player.getInstance().isYouGoFirst()?"X":"0");
		
		this.yourTurn = (Player.getInstance().isYouGoFirst()?true:false);
		gameStatus = new int[9];
			
		initComponents();
		chatArea.setEditable(false);
		opponentNameLabel.setText(Opponent.getInstance().getUsername());
		playerNameLabel.setText(Player.getInstance().getUsername());
		
		
		infoLabel = new JLabel("");
		infoLabel.setBounds(33, 100, 274, 29);
		add(infoLabel);
		infoLabel.setFont(new Font("Tahoma", Font.BOLD, 15));
		infoLabel.setForeground(new Color(255, 0, 0));
		infoLabel.setEnabled(false);
		listen = true;
		listenForResponse();

	}
	
	private void listenForResponse() {
		
		SwingWorker<Void,TransferData> worker = new SwingWorker<Void,TransferData>(){

			@Override
			protected Void doInBackground() throws Exception 
			{
				while(listen){
					try{
						messageRecieved = ConnectionHandler.getInstance().readFromServer();
						publish(messageRecieved);
						if(messageRecieved.getCode().equals("gogo")){
							break;
						}
					}catch (IOException | ClassNotFoundException e) {
						ConnectionHandler.getInstance().close();
						Logger.getLogger(SwingWorker.class.getName() + " Failed to read from server "+ e);
	                    System.out.println(" Failed to read from server");
						listen = false;
	                }
					
				}
				return null;
			}
			protected void process(List<TransferData> list){
				if(messageRecieved == null){
					
					JOptionPane.showConfirmDialog(null,
							"Failed to read from sever. Close the application." ,
							"Failed to read from sever. Close the application.",
							JOptionPane.YES_NO_OPTION);
					return;
				}
				if(messageRecieved.getCode().equals("messagereceived"))
					chatArea.append(Opponent.getInstance().getUsername() + ":" + messageRecieved.getMessage() + "\n");
				System.out.println(messageRecieved.toString());
				if (messageRecieved.getCode().equals("youwon")){
					winOrLose("you won.");
				}
				if(messageRecieved.getCode().equals("opponentwon")){
					winOrLose("you lose.");
				}
				if(messageRecieved.getCode().equals("yourturn")){
					yourTurn = true;
					gameStatus = messageRecieved.getGameStatus();
					changeButtons();
					infoLabel.setText("your turn now");
				}
				if(messageRecieved.getCode().equals("tie")){
					winOrLose("It`s a tie.");
				}
				if(messageRecieved.getCode().equals("playeroffline")){
					if(messageRecieved.getUsername().equals(Opponent.getInstance().getUsername())){
						
						Timer t = new Timer();
						TimerTask wait = new TimerTask(){

							@Override
							public void run() {
								JOptionPane.showConfirmDialog(null,
										Opponent.getInstance().getUsername() + " is offline." ,
										Opponent.getInstance().getUsername() + " is offline.",
										JOptionPane.YES_NO_OPTION);
								rootFrame.changePanel(new MainPanel(rootFrame));
							}
							
						};
						t.schedule(wait, 2000);
						
					}
				}
				
			}
			protected void done()
			{
				if(messageRecieved == null){
					
					JOptionPane.showConfirmDialog(null,
							"Failed to read from sever. Close the application." ,
							"Failed to read from sever. Close the application.",
							JOptionPane.YES_NO_OPTION);
					
				}else if(messageRecieved.getCode().equals("messagereceived"))
					chatArea.append(Opponent.getInstance().getUsername() + ":" + messageRecieved.getMessage() + "\n");
				System.out.println(messageRecieved.toString());
			}
			
		};
		try {
        	listen = true;
            worker.execute();
        } catch (Exception e) {
        	Logger.getLogger(SwingWorker.class.getName() + " Failed to read from server "+ e);
        	System.out.println(" Failed to read from server ");
        }	
	}
	

	private void winOrLose(final String message){
		if(message.equals("you won.") || message.equals("you lose.") || message.equals("It`s a tie.") )
			JOptionPane.showMessageDialog(null, message);
		Timer t = new Timer();
		TimerTask wait = new TimerTask(){

			@Override
			public void run() {
				listen = false;
				TransferData sendObject = new TransferData();
				if(message.equals("you won."))
				{
					sendObject.setRank(Player.getInstance().getRank() + 10);
					Player.getInstance().setRank(Player.getInstance().getRank() + 10);
				}else if(message.equals("It`s a tie.")){
					sendObject.setRank(Player.getInstance().getRank() + 5);
					Player.getInstance().setRank(Player.getInstance().getRank() + 5);
				}else
					sendObject.setRank(-1);
				sendObject.setCode("back");
				try{
					ConnectionHandler.getInstance().sendToServer(sendObject);
				}catch(IOException ioe){
					JOptionPane.showConfirmDialog(null,
							"The server did not respond. Close the application." ,
							"The server did not respond. Close the application.",
							JOptionPane.YES_NO_OPTION);
				}
				
				rootFrame.changePanel(new MainPanel(rootFrame));
			}
			
		};
		t.schedule(wait, 3000);
	}

	private void initComponents(){
		backButton = new JButton("<<BACK");
		backButton.setFont(new Font("Tahoma", Font.BOLD, 12));
		backButton.setForeground(new Color(0, 0, 51));
		backButton.setBackground(new Color(100, 149, 237));
		backButton.setBounds(10, 11, 89, 23);
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				listen = false;
				TransferData sendObject = new TransferData();
				sendObject.setCode("back");
				
				sendObject.setRank(-1);
				try{
					ConnectionHandler.getInstance().sendToServer(sendObject);
				}catch(IOException ioe){
					JOptionPane.showConfirmDialog(null,
							"The server did not respond. Close the application." ,
							"The server did not respond. Close the application.",
							JOptionPane.YES_OPTION);
				}
			
				rootFrame.changePanel(new MainPanel(rootFrame));
			}
		});
		setLayout(null);
		add(backButton);
		
		vsLabel = new JLabel("VS.");
		vsLabel.setForeground(new Color(173, 216, 230));
		vsLabel.setFont(new Font("Bradley Hand ITC", Font.BOLD | Font.ITALIC, 35));
		vsLabel.setBounds(317, 45, 64, 29);
		add(vsLabel);
		
		opponentNameLabel = new JLabel("");
		opponentNameLabel.setForeground(new Color(173, 216, 230));
		opponentNameLabel.setFont(new Font("Tahoma", Font.PLAIN, 30));
		opponentNameLabel.setBounds(431, 45, 111, 29);
		add(opponentNameLabel);
		
		playerNameLabel = new JLabel("");
		playerNameLabel.setForeground(new Color(173, 216, 230));
		playerNameLabel.setFont(new Font("Tahoma", Font.PLAIN, 30));
		playerNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		playerNameLabel.setBounds(44, 45, 263, 29);
		add(playerNameLabel);
		
		scrollChat = new JScrollPane();
		scrollChat.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollChat.setBounds(387, 100, 284, 346);
		add(scrollChat);
		
		chatArea = new JTextArea();
		chatArea.setBounds(387, 100, 284, 346);
		chatArea.setBackground(new Color(240, 248, 255));
		chatArea.setEditable(false);
		chatArea.setLineWrap(true);
		chatArea.setWrapStyleWord(true);
		chatArea.setColumns(35);
		
		scrollChat.setViewportView(chatArea);
		
		scrollMessage = new JScrollPane();
		scrollMessage.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollMessage.setBounds(387, 450, 203, 52);
		add(scrollMessage);
		
		messageArea = new JTextArea();
		messageArea.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent evt) {
				if(evt.getKeyCode() == KeyEvent.VK_ENTER){
					message = messageArea.getText();
					if(message.endsWith("\n")){
						message = message.substring(0, message.length()-1);
					}
					if(message.length() != 0){
					
						SendMessageWorker worker = new SendMessageWorker();
						worker.execute();
					}
				}
			}
		});
		messageArea.setBounds(387, 450, 203, 52);
		messageArea.setBackground(new Color(240, 248, 255));
		messageArea.setLineWrap(true);
		messageArea.setWrapStyleWord(true);
		messageArea.setColumns(28);
				
		scrollMessage.setViewportView(messageArea);
		
		sendButton = new JButton("SEND");
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				message = messageArea.getText();
				if(message.endsWith("\n")){
					message = message.substring(0, message.length()-1);
				}
				if(message.length() != 0 || message.equals("\n")){
				
					SendMessageWorker worker = new SendMessageWorker();
					worker.execute();
				}
				
			}
		});
		sendButton.setFont(new Font("Tahoma", Font.BOLD, 12));
		sendButton.setForeground(new Color(0, 0, 51));
		sendButton.setBounds(599, 450, 72, 52);
		add(sendButton);
		
		gamePanel = new JPanel();
		gamePanel.setBounds(33, 140, 274, 362);
		gamePanel.setBackground(new Color(100, 149, 237));
		
		add(gamePanel);
		gamePanel.setLayout(null);
		
		button1 = new JButton("");
		button1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(yourTurn){
					infoLabel.setText("");
					button1.setText(symbol);
					button1.setEnabled(false);
					yourTurn = false;
					sendGameMove = new TransferData();
					sendGameMove.setCode("opponentnow");
					gameStatus[0] = (symbol.equals("X")?1:2);
					sendGameMove.setSymbol(symbol);
					sendGameMove.setGameStatus(gameStatus);
					sendGameMove.setOpponent(Opponent.getInstance().getUsername());
					sendGameMove.setUsername(Player.getInstance().getUsername());
					try{
						ConnectionHandler.getInstance().sendToServer(sendGameMove);
					}catch(IOException ioe){
						JOptionPane.showConfirmDialog(null,
								"The server did not respond. Close the application." ,
								"The server did not respond. Close the application.",
								JOptionPane.YES_OPTION);
					}
					
				}else{
					infoLabel.setText("Wait for your turn");
				}
			}
		});
		button1.setBounds(21, 64, 70, 70);
		gamePanel.add(button1);
		
		button2 = new JButton("");
		button2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(yourTurn){
					infoLabel.setText("");
					button2.setText(symbol);
					button2.setEnabled(false);
					yourTurn = false;
					sendGameMove = new TransferData();
					sendGameMove.setCode("opponentnow");
					gameStatus[1] = (symbol.equals("X")?1:2);
					sendGameMove.setSymbol(symbol);
					sendGameMove.setGameStatus(gameStatus);
					sendGameMove.setOpponent(Opponent.getInstance().getUsername());
					sendGameMove.setUsername(Player.getInstance().getUsername());
					try{
						ConnectionHandler.getInstance().sendToServer(sendGameMove);
					}catch(IOException ioe){
						JOptionPane.showConfirmDialog(null,
								"The server did not respond. Close the application." ,
								"The server did not respond. Close the application.",
								JOptionPane.YES_OPTION);
					}
				}else{
					infoLabel.setText("Wait for your turn");
				}
			}
		});
		button2.setBounds(102, 64, 70, 70);
		gamePanel.add(button2);
		
		button3 = new JButton("");
		button3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(yourTurn){
					infoLabel.setText("");
					button3.setText(symbol);
					button3.setEnabled(false);
					yourTurn = false;
					sendGameMove = new TransferData();
					sendGameMove.setCode("opponentnow");
					gameStatus[2] = (symbol.equals("X")?1:2);
					sendGameMove.setSymbol(symbol);
					sendGameMove.setGameStatus(gameStatus);
					sendGameMove.setOpponent(Opponent.getInstance().getUsername());
					sendGameMove.setUsername(Player.getInstance().getUsername());
					try{
						ConnectionHandler.getInstance().sendToServer(sendGameMove);
					}catch(IOException ioe){
						JOptionPane.showConfirmDialog(null,
								"The server did not respond. Close the application." ,
								"The server did not respond. Close the application.",
								JOptionPane.YES_OPTION);
					}
					
				}else{
					infoLabel.setText("Wait for your turn");
				}
			}
		});
		button3.setBounds(183, 64, 70, 70);
		gamePanel.add(button3);
		
		button4 = new JButton("");
		button4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(yourTurn){
					infoLabel.setText("");
					button4.setText(symbol);
					button4.setEnabled(false);
					yourTurn = false;
					sendGameMove = new TransferData();
					sendGameMove.setCode("opponentnow");
					gameStatus[3] = (symbol.equals("X")?1:2);
					sendGameMove.setSymbol(symbol);
					sendGameMove.setGameStatus(gameStatus);
					sendGameMove.setOpponent(Opponent.getInstance().getUsername());
					sendGameMove.setUsername(Player.getInstance().getUsername());
					try{
						ConnectionHandler.getInstance().sendToServer(sendGameMove);
					}catch(IOException ioe){
						JOptionPane.showConfirmDialog(null,
								"The server did not respond. Close the application." ,
								"The server did not respond. Close the application.",
								JOptionPane.YES_NO_OPTION);
					}
				}else{
					infoLabel.setText("Wait for your turn");
				}
			}
		});
		button4.setBounds(21, 145, 70, 70);
		gamePanel.add(button4);
		
		button5 = new JButton("");
		button5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(yourTurn){
					infoLabel.setText("");
					button5.setText(symbol);
					button5.setEnabled(false);
					yourTurn = false;
					sendGameMove = new TransferData();
					sendGameMove.setCode("opponentnow");
					gameStatus[4] = (symbol.equals("X")?1:2);
					sendGameMove.setSymbol(symbol);
					sendGameMove.setGameStatus(gameStatus);
					sendGameMove.setOpponent(Opponent.getInstance().getUsername());
					sendGameMove.setUsername(Player.getInstance().getUsername());
					try{
						ConnectionHandler.getInstance().sendToServer(sendGameMove);
					}catch(IOException ioe){
						JOptionPane.showConfirmDialog(null,
								"The server did not respond. Close the application." ,
								"The server did not respond. Close the application.",
								JOptionPane.YES_OPTION);
					}
				}else{
					infoLabel.setText("Wait for your turn");
				}
			}
		});
		button5.setBounds(101, 145, 70, 70);
		gamePanel.add(button5);
		
		button6 = new JButton("");
		button6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(yourTurn){
					infoLabel.setText("");
					button6.setText(symbol);
					button6.setEnabled(false);
					yourTurn = false;
					sendGameMove = new TransferData();
					sendGameMove.setCode("opponentnow");
					gameStatus[5] = (symbol.equals("X")?1:2);
					sendGameMove.setSymbol(symbol);
					sendGameMove.setGameStatus(gameStatus);
					sendGameMove.setOpponent(Opponent.getInstance().getUsername());
					sendGameMove.setUsername(Player.getInstance().getUsername());
					try{
						ConnectionHandler.getInstance().sendToServer(sendGameMove);
					}catch(IOException ioe){
						JOptionPane.showConfirmDialog(null,
								"The server did not respond. Close the application." ,
								"The server did not respond. Close the application.",
								JOptionPane.YES_OPTION);
					}
				}else{
					infoLabel.setText("Wait for your turn");
				}
			}
		});
		button6.setBounds(183, 145, 70, 70);
		gamePanel.add(button6);
		
		button7 = new JButton("");
		button7.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(yourTurn){
					infoLabel.setText("");
					button7.setText(symbol);
					button7.setEnabled(false);
					yourTurn = false;
					sendGameMove = new TransferData();
					sendGameMove.setCode("opponentnow");
					gameStatus[6] = (symbol.equals("X")?1:2);
					sendGameMove.setSymbol(symbol);
					sendGameMove.setGameStatus(gameStatus);
					sendGameMove.setOpponent(Opponent.getInstance().getUsername());
					sendGameMove.setUsername(Player.getInstance().getUsername());
					try{
						ConnectionHandler.getInstance().sendToServer(sendGameMove);
					}catch(IOException ioe){
						JOptionPane.showConfirmDialog(null,
								"The server did not respond. Close the application." ,
								"The server did not respond. Close the application.",
								JOptionPane.YES_OPTION);
					}
				}else{
					infoLabel.setText("Wait for your turn");
				}
			}
		});
		button7.setBounds(21, 226, 70, 70);
		gamePanel.add(button7);
		
		button8 = new JButton("");
		button8.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(yourTurn){
					infoLabel.setText("");
					button8.setText(symbol);
					button8.setEnabled(false);
					yourTurn = false;
					sendGameMove = new TransferData();
					sendGameMove.setCode("opponentnow");
					gameStatus[7] = (symbol.equals("X")?1:2);
					sendGameMove.setSymbol(symbol);
					sendGameMove.setGameStatus(gameStatus);
					sendGameMove.setOpponent(Opponent.getInstance().getUsername());
					sendGameMove.setUsername(Player.getInstance().getUsername());
					try{
						ConnectionHandler.getInstance().sendToServer(sendGameMove);
					}catch(IOException ioe){
						JOptionPane.showConfirmDialog(null,
								"The server did not respond. Close the application." ,
								"The server did not respond. Close the application.",
								JOptionPane.YES_OPTION);
					}
				}else{
					infoLabel.setText("Wait for your turn");
				}
			}
		});
		button8.setBounds(101, 226, 70, 70);
		gamePanel.add(button8);
		
		button9 = new JButton("");
		button9.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(yourTurn){
					infoLabel.setText("");
					button9.setText(symbol);
					button9.setEnabled(false);
					yourTurn = false;
					sendGameMove = new TransferData();
					sendGameMove.setCode("opponentnow");
					gameStatus[8] = (symbol.equals("X")?1:2);
					sendGameMove.setSymbol(symbol);
					sendGameMove.setGameStatus(gameStatus);
					sendGameMove.setOpponent(Opponent.getInstance().getUsername());
					sendGameMove.setUsername(Player.getInstance().getUsername());
					try{
						ConnectionHandler.getInstance().sendToServer(sendGameMove);
					}catch(IOException ioe){
						JOptionPane.showConfirmDialog(null,
								"The server did not respond. Close the application." ,
								"The server did not respond. Close the application.",
								JOptionPane.YES_OPTION);
					}
				}else{
					infoLabel.setText("Wait for your turn");
				}
			}
		});
		button9.setBounds(183, 226, 70, 70);
		gamePanel.add(button9);
	}
	
	private class SendMessageWorker extends SwingWorker<Void, String>{
		
		@Override
		protected Void doInBackground() throws Exception 
		{
			
			sendMessage = new TransferData();
			sendMessage.setCode("sendto");
			sendMessage.setOpponent(Opponent.getInstance().getUsername());
			sendMessage.setUsername(Player.getInstance().getUsername());
			
			
			sendMessage.setMessage(message);
			System.out.println(Player.getInstance().getUsername()+ "trimite catre " + Opponent.getInstance().getUsername()+ "ar trebui sa primeasca " + sendMessage.toString());
			messageArea.setText("");
			try{
				ConnectionHandler.getInstance().sendToServer(sendMessage);
			}catch(IOException ioe){
				JOptionPane.showConfirmDialog(null,
						"The server did not respond. Close the application." ,
						"The server did not respond. Close the application.",
						JOptionPane.OK_OPTION);
			}
			return null;
		}
		
		protected void done() {
			String s = Player.getInstance().getUsername()+": "+ sendMessage.getMessage() + "\n";
			System.out.println(s);
			
			chatArea.append(s);
		}
	}
	protected void changeButtons() {
		if(gameStatus[0] != 0){
			button1.setEnabled(true);
			button1.setText((gameStatus[0] == 1)?"X":"0");
			button1.setEnabled(false);
		}
		if(gameStatus[1] != 0){
			button2.setEnabled(true);
			button2.setText((gameStatus[1] == 1)?"X":"0");
			button2.setEnabled(false);
		}
		if(gameStatus[2] != 0){
			button3.setEnabled(true);
			button3.setText((gameStatus[2] == 1)?"X":"0");
			button3.setEnabled(false);
		}
		if(gameStatus[3] != 0){
			button4.setEnabled(true);
			button4.setText((gameStatus[3] == 1)?"X":"0");
			button4.setEnabled(false);
		}
		if(gameStatus[4] != 0){
			button5.setEnabled(true);
			button5.setText((gameStatus[4] == 1)?"X":"0");
			button5.setEnabled(false);
		}
		if(gameStatus[5] != 0){
			button6.setEnabled(true);
			button6.setText((gameStatus[5] == 1)?"X":"0");
			button6.setEnabled(false);
		}
		if(gameStatus[6] != 0){
			button7.setEnabled(true);
			button7.setText((gameStatus[6] == 1)?"X":"0");
			button7.setEnabled(false);
		}
		if(gameStatus[7] != 0){
			button8.setEnabled(true);
			button8.setText((gameStatus[7] == 1)?"X":"0");
			button8.setEnabled(false);
		}
		if(gameStatus[8] != 0){
			button9.setEnabled(true);
			button9.setText((gameStatus[8] == 1)?"X":"0");
			button9.setEnabled(false);
		}
	}
}
