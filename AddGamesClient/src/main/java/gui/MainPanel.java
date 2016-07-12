package gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.mysql.jdbc.Connection;

import client.ConnectionHandler;
import networking.Hangman;
import networking.Opponent;
import networking.Player;
import networking.TransferData;

public class MainPanel extends JPanel {

	private final MainFrame rootFrame;
	private JPanel profilePanel;
	private JLabel profileLabel;
	private JLabel usernameLabel;
	private JLabel rankLabel;
	private JLabel usernameContentLabel;
	private JLabel rankContentLabel;
	private JList<String> listPlayers;
	private JLabel oponentPickLabel;
	private JLabel multiplayerLabel;
	private JButton hangmanButton;
	private JLabel tictactoeLabel;
	private JLabel hangmanLabel;
	private JButton signoutButton;
	private JScrollPane scrollPlayers;
	
	private ArrayList<String> names;
	private String selectedName;
	private DefaultListModel<String> listModel = new DefaultListModel<String>();
	private boolean listen;
	//private TransferData onlinePlayers;
	private TransferData messageRecieved;
	private TransferData sendObject;
	
	volatile private boolean listenForOpponentResponse;
	volatile boolean timeLeft;
	
	private Timer t;
	private TimerTask task;
	private JLabel lineLabel1;
	private JLabel lineLabel2;
	
	/**
	 * Create the panel.
	 */
	public MainPanel(MainFrame rootFrame) {
		this.rootFrame = rootFrame;
		listenForOpponentResponse = false;
		initComponents();
		sendObject = new TransferData();
		sendObject.setCode("onlineplayers");
		try{
			ConnectionHandler.getInstance().sendToServer(sendObject);
		}catch(IOException e){
			JOptionPane.showConfirmDialog(null,
					"The server did not respond. Close the application." ,
					"The server did not respond. Close the application.",
					JOptionPane.YES_NO_OPTION);
		}
		
		listen = true;
		selectPlayerHandler();
		listenForResponse();
	}
	
	private void listenForResponse() {
		
		SwingWorker<Void,TransferData> worker = new SwingWorker<Void,TransferData>(){

			@Override
			protected Void doInBackground() throws Exception {
				
				while(listen){
					try{
						messageRecieved = ConnectionHandler.getInstance().readFromServer();
						if(messageRecieved == null){
							
							JOptionPane.showConfirmDialog(null,
									"Failed to read from sever. Close the application." ,
									"Failed to read from sever. Close the application.",
									JOptionPane.YES_OPTION);
							break;
						}
						if(messageRecieved.getCode().equals(("gogo"))){
							listen = false;
							break;
						}
						if(listenForOpponentResponse == true){
							if(messageRecieved.getCode().equals("yes")){
								task.cancel();
								t.cancel();
								listenForOpponentResponse = false;
								listen = false;
								Player.getInstance().setYouGoFirst(true);
								rootFrame.changePanel(new TicTacToePanel(rootFrame));
								break;
								
							}else{
								if(messageRecieved.getCode().equals("no")){
									task.cancel();
									t.cancel();
									listenForOpponentResponse = false;
								}
							}
						}
						if(messageRecieved.getCode().equals("game")){
							if(listenForOpponentResponse == true){
								sendObject = new TransferData();
								sendObject.setCode("no");
								ConnectionHandler.getInstance().sendToServer(sendObject);
								
							}else{
								timeLeft = true;
								Timer timer = new Timer();
								TimerTask answerTime = new TimerTask(){

									@Override
									public void run() {
										timeLeft = false;
									}
								};
								timer.schedule(answerTime, 9000);
								
								int n = JOptionPane.showConfirmDialog(null,
										"Do you wanna play with " + messageRecieved.getOpponent(),
										"Do you wanna play with " + messageRecieved.getOpponent(),
										JOptionPane.YES_NO_OPTION);
								if(n == JOptionPane.YES_OPTION && timeLeft){
									
									sendObject = new TransferData();
									sendObject.setCode("yes");
									sendObject.setOpponent(messageRecieved.getOpponent());
									Opponent.getInstance().setUsername(messageRecieved.getOpponent());
									ConnectionHandler.getInstance().sendToServer(sendObject);
									
									listen = false;
									answerTime.cancel();
									timer.cancel();
									Player.getInstance().setYouGoFirst(false);
									rootFrame.changePanel(new TicTacToePanel(rootFrame));
									
									break;
									
								}else{
									sendObject = new TransferData();
									sendObject.setCode("no");
									sendObject.setOpponent(messageRecieved.getOpponent());
									
									ConnectionHandler.getInstance().sendToServer(sendObject);
								}
							}
						}
						publish(messageRecieved);
							
					}catch (IOException | ClassNotFoundException e) {
						ConnectionHandler.getInstance().close();
						Logger.getLogger(SwingWorker.class.getName() + " Failed to read from server "+ e);
	                    System.out.println(" Failed to read from server");
						listen = false;
	                }
					
				}
				return null;
				
			}
			protected void process(List<TransferData> list)
			{
				if(messageRecieved == null){
					
					JOptionPane.showConfirmDialog(null,
							"Failed to read from sever. Close the application." ,
							"Failed to read from sever. Close the application.",
							JOptionPane.YES_OPTION);
					return;
				}
				if(messageRecieved.getCode().equals("onlineplayers")){
					names = new ArrayList<>();
					names.addAll(messageRecieved.getOnlinePlayers());
					updateList(names);
				}else if(messageRecieved.getCode().equals("newplayer"))
				{
					names.add(messageRecieved.getUsername());
					Collections.sort(names);
					updateList(names);
				} else if(messageRecieved.getCode().equals("playeroffline"))
				{
					for(int i = 0; i < names.size(); i++){
						if(names.get(i).equals(messageRecieved.getUsername())){
							names.remove(i);
							break;
						}
					}
					updateList(names);
				}else if(messageRecieved.equals("word")){
					Hangman.getInstance().setWord(messageRecieved.getMessage());
					Hangman.getInstance().setWtb(messageRecieved.getPassword());
				}
				System.out.println((messageRecieved.toString()));
				
			}
			
		};
		try {
            worker.execute();
        } catch (Exception e) {
        	Logger.getLogger(SwingWorker.class.getName() + " Failed to read from server in main panel "+ e);
        	System.out.println(" Failed to read from server in main panel");
        }		
	}

	
	protected void updateList(ArrayList<String> names) {
		listModel.clear();
		for(int i = 0; i < names.size(); i++){
			listModel.addElement(names.get(i));
		}
		
	}
	
	private void initComponents(){
		
		this.rootFrame.setSize(770, 670);
		this.rootFrame.setTitle("ADD Games");
		this.rootFrame.setResizable(false);
		setBounds(100, 100, 757, 650);
		this.setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(null);
		this.setBackground(new Color(0, 0, 51));
		
		profilePanel = new JPanel();
		profilePanel.setBorder(new LineBorder(new Color(100, 149, 237)));
		profilePanel.setBackground(new Color(25, 25, 112));
		profilePanel.setBounds(22, 22, 392, 97);
		this.add(profilePanel);
		profilePanel.setLayout(null);
		
		profileLabel = new JLabel("PROFILE");
		profileLabel.setForeground(new Color(173, 216, 230));
		profileLabel.setFont(new Font("Tahoma", Font.PLAIN, 15));
		profileLabel.setBounds(10, 11, 102, 23);
		profilePanel.add(profileLabel);
		
		usernameLabel = new JLabel("Username:");
		usernameLabel.setForeground(new Color(173, 216, 230));
		usernameLabel.setBounds(30, 40, 102, 14);
		profilePanel.add(usernameLabel);
		
		rankLabel = new JLabel("Rank:");
		rankLabel.setForeground(new Color(173, 216, 230));
		rankLabel.setBounds(30, 61, 102, 14);
		profilePanel.add(rankLabel);
		
		usernameContentLabel = new JLabel("");
		usernameContentLabel.setForeground(Color.WHITE);
		usernameContentLabel.setText(Player.getInstance().getUsername());
		usernameContentLabel.setBounds(127, 40, 183, 14);
		profilePanel.add(usernameContentLabel);
		
		rankContentLabel = new JLabel("");
		rankContentLabel.setForeground(Color.WHITE);
		rankContentLabel.setText(Integer.toString(Player.getInstance().getRank()));
		rankContentLabel.setBounds(127, 61, 183, 14);
		profilePanel.add(rankContentLabel);
		
		signoutButton = new JButton("Sign out");
		signoutButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					signoutButtonActionPerformed(evt);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		signoutButton.setFont(new Font("Tahoma", Font.BOLD, 12));
		signoutButton.setForeground(new Color(0, 0, 51));
		signoutButton.setBackground(new Color(100, 149, 237));
		signoutButton.setBounds(293, 11, 89, 23);
		profilePanel.add(signoutButton);
		
		scrollPlayers = new JScrollPane();
		scrollPlayers.setBounds(443, 194, 279, 185);
		scrollPlayers.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		this.add(scrollPlayers);		
		
		listPlayers = new JList();
		listPlayers.setBorder(new EmptyBorder(5, 8, 5, 5));
		listPlayers.setForeground(new Color(173, 216, 230));
		listPlayers.setFont(new Font("Tahoma", Font.BOLD, 18));
		listPlayers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listPlayers.setBackground(new Color(25, 25, 112));
		listPlayers.setBounds(22, 179, 279, 359);
		listPlayers.setModel(listModel);
		
		scrollPlayers.setViewportView(listPlayers);
		
		oponentPickLabel = new JLabel("Pick your oponent  by double-clicking");
		oponentPickLabel.setFont(new Font("Tahoma", Font.PLAIN, 15));
		oponentPickLabel.setForeground(new Color(173, 216, 230));
		oponentPickLabel.setBounds(458, 156, 250, 28);
		this.add(oponentPickLabel);
		
		multiplayerLabel = new JLabel("MULTIPLAYER GAME");
		multiplayerLabel.setForeground(new Color(173, 216, 230));
		multiplayerLabel.setFont(new Font("Tahoma", Font.BOLD, 20));
		multiplayerLabel.setBounds(22, 154, 228, 29);
		this.add(multiplayerLabel);
		
		hangmanButton = new JButton("Play>>");
		hangmanButton.setForeground(new Color(0, 0, 51));
		hangmanButton.setBackground(new Color(100, 149, 237));
		hangmanButton.setFont(new Font("Tahoma", Font.BOLD, 12));
		hangmanButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				hangmanButtonActionPerformed(evt);
			}
		});
		hangmanButton.setBounds(314, 606, 89, 23);
		this.add(hangmanButton);
		
		tictactoeLabel = new JLabel("");
		tictactoeLabel.setBounds(22, 219, 382, 130);
		tictactoeLabel.setIcon(new ImageIcon(".\\src\\main\\resources\\tictactoe.png"));
		this.add(tictactoeLabel);
		
		
		scrollPlayers.setViewportView(listPlayers);
		
		JLabel singleplayerLabel = new JLabel("SINGLEPLAYER GAME");
		singleplayerLabel.setForeground(new Color(173, 216, 230));
		singleplayerLabel.setFont(new Font("Tahoma", Font.BOLD, 20));
		singleplayerLabel.setBounds(22, 415, 228, 29);
		add(singleplayerLabel);
		
		hangmanLabel = new JLabel("");
		hangmanLabel.setBounds(22, 455, 382, 130);
		hangmanLabel.setIcon(new ImageIcon(".\\src\\main\\resources\\hangman.png")); //--------------------------------
		this.add(hangmanLabel);
		
		lineLabel1 = new JLabel("________________________________________________________________________________________________________");
		lineLabel1.setForeground(new Color(173, 216, 230));
		lineLabel1.setBounds(10, 391, 737, 14);
		add(lineLabel1);
		
		lineLabel2 = new JLabel("_______________________________________________________________________________________________________");
		lineLabel2.setForeground(new Color(173, 216, 230));
		lineLabel2.setBounds(10, 130, 737, 14);
		add(lineLabel2);
		
		
	}
	
	public void selectPlayerHandler()
	{
		
		MouseAdapter mouseListener = new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					selectedName = (String) listPlayers.getSelectedValue();
					System.out.println(selectedName);
					
					
					
					if(listenForOpponentResponse == false){
						Opponent.getInstance().setUsername(selectedName);
						TransferData send = new TransferData();
						send.setCode("game");
						send.setUsername(Player.getInstance().getUsername());
						send.setOpponent(Opponent.getInstance().getUsername());
						try{
							ConnectionHandler.getInstance().sendToServer(send);
						}catch(IOException ioe){
							JOptionPane.showConfirmDialog(null,
									"The server did not respond. Close the application." ,
									"The server did not respond. Close the application.",
									JOptionPane.YES_NO_OPTION);
						}
						
						t = new Timer();
						listenForOpponentResponse = true;
						task = new TimerTask(){

							@Override
							public void run() {
								listenForOpponentResponse = false;
								
								JOptionPane.showMessageDialog(null, "The player did not respond.");
							}
							
						};
						t.schedule(task, 10000);//wait for 10 seconds
					}
				} 			
			}
		};
		listPlayers.addMouseListener(mouseListener);
	}
	
	protected void hangmanButtonActionPerformed(ActionEvent evt) {
		listen = false;
		
		sendObject = new TransferData();
		sendObject.setCode("startgame");
		sendObject.setOpponent(Opponent.getInstance().getUsername());
		try{
			ConnectionHandler.getInstance().sendToServer(sendObject);
		}catch(IOException ioe){
			JOptionPane.showConfirmDialog(null,
					"The server did not respond. Close the application." ,
					"The server did not respond. Close the application.",
					JOptionPane.YES_NO_OPTION);
		}
		
		
	
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rootFrame.changePanel(new HangmanPanel(rootFrame));
	}
	
	protected void tictactoeButtonActionPerformed(ActionEvent evt) throws IOException {
		listen = false;
		
		sendObject = new TransferData();
		sendObject.setCode("startgame");
		sendObject.setOpponent(Opponent.getInstance().getUsername());
		ConnectionHandler.getInstance().sendToServer(sendObject);
		
	
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		rootFrame.changePanel(new TicTacToePanel(rootFrame));
	}
	
	protected void signoutButtonActionPerformed(ActionEvent evt) throws IOException {
		
		ConnectionHandler.getInstance().signout();
		
		rootFrame.changePanel(new LoginPanel(rootFrame));
		
	}
}
