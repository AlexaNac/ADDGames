package gui;

import java.awt.Font;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import client.ConnectionHandler;
import networking.Hangman;
import networking.Opponent;
import networking.Player;
import networking.TransferData;

import java.awt.Color;
import javax.swing.JScrollPane;

public class HangmanPanel extends JPanel {

	private MainFrame rootFrame;
	private JTextField letterField;
	private JLabel letterInstrLabel;
	private JLabel wordLabel;
	private JLabel wordInstrLabel ;
	private JLabel hangmanImage;
//	private JPanel finnishPane;
//	private JLabel winLabel;
//	private JLabel guessLabel;
//	private JLabel lblTheWordWas;
	private JLabel livesLabel;
	private TransferData messageRecieved;
	private TransferData sendObject;
	private boolean listen;
	
	/**
	 * Create the panel.
	 */
	public HangmanPanel(MainFrame rootFrame) {
		this.rootFrame = rootFrame;
		sendObject = new TransferData();
		sendObject.setCode("word");
		try{
			ConnectionHandler.getInstance().sendToServer(sendObject);
		}catch(IOException ioe){
			JOptionPane.showConfirmDialog(null,
					"The server did not respond. Close the application." ,
					"The server did not respond. Close the application.",
					JOptionPane.YES_NO_OPTION);
		}
		
		
		listen = true;
		listenForResponse();
		
		initComponents();

	}
	
private void listenForResponse() {
		
		SwingWorker<Void,TransferData> worker = new SwingWorker<Void,TransferData>(){

			@Override
			protected Void doInBackground() throws Exception {
				
				while(listen){
					try{
						messageRecieved = ConnectionHandler.getInstance().readFromServer();
						if(messageRecieved.getCode().equals(("gogo"))){
							listen = false;
							break;
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
					JOptionPane.OK_OPTION);
					return;
				}
				if(!messageRecieved.getCode().equals("word")){
					sendObject = new TransferData();
					sendObject.setCode("busy");
					try{
						ConnectionHandler.getInstance().sendToServer(sendObject);
					}catch(IOException ioe){
						JOptionPane.showConfirmDialog(null,
								"The server did not respond. Close the application." ,
								"The server did not respond. Close the application.",
								JOptionPane.OK_OPTION);
					}
					
				} else if(messageRecieved.getCode().equals("word"))
				{
					Hangman.getInstance().setWord(messageRecieved.getMessage());
					Hangman.getInstance().setWtb(messageRecieved.getPassword());
					wordLabel.setText(Hangman.getInstance().getWtb());
					
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
	
	
	private void initComponents(){
		this.rootFrame.setTitle("Hangman Game");
		this.rootFrame.setSize(880,540);
		//this.rootFrame.setResizable(false);
		setBounds(100, 100, 876, 525);
		this.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.setBackground(new Color(0, 0, 51));
		setLayout(null);
		
		JButton backButton = new JButton("<<BACK");
		backButton.setForeground(new Color(0, 0, 51));
		backButton.setBackground(new Color(100, 149, 237));
		backButton.setFont(new Font("Tahoma", Font.BOLD, 12));
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				listen = false;
				TransferData sendObject = new TransferData();
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
		});
		backButton.setBounds(10, 11, 89, 23);
		add(backButton);
		
		hangmanImage = new JLabel("");
		hangmanImage.setIcon(new ImageIcon(".\\src\\main\\resources\\5.png"));
		hangmanImage.setBounds(54, 109, 362, 322);
		add(hangmanImage);
		
		JLabel livesInstrLabel = new JLabel("Lives:");
		livesInstrLabel.setForeground(new Color(173, 216, 230));
		livesInstrLabel.setFont(new Font("Tahoma", Font.PLAIN, 15));
		livesInstrLabel.setBounds(446, 127, 76, 33);
		add(livesInstrLabel);
		
		livesLabel = new JLabel("5");
		/*String lives = "";
		lives+=Hangman.getLives();
		livesLabel.setText(lives);*/
		livesLabel.setFont(new Font("Tahoma", Font.PLAIN, 20));
		livesLabel.setForeground(new Color(173, 216, 230));
		livesLabel.setBounds(507, 127, 141, 33);
		add(livesLabel);
		
		wordInstrLabel = new JLabel("Here is your word to guess, good luck!");
		wordInstrLabel.setForeground(new Color(173, 216, 230));
		wordInstrLabel.setFont(new Font("Tahoma", Font.PLAIN, 15));
		wordInstrLabel.setBounds(446, 190, 384, 28);
		add(wordInstrLabel);
		
		wordLabel = new JLabel();
		wordLabel.setText(Hangman.getInstance().getWtb());
		wordLabel.setForeground(new Color(0, 255, 255));
		wordLabel.setFont(new Font("Tahoma", Font.PLAIN, 35));
		wordLabel.setBounds(446, 239, 397, 70);
		add(wordLabel);
		
		letterInstrLabel = new JLabel("Choose your letter, only one at a time, then submit");
		letterInstrLabel.setForeground(new Color(173, 216, 230));
		letterInstrLabel.setFont(new Font("Tahoma", Font.PLAIN, 15));
		letterInstrLabel.setBounds(446, 339, 384, 26);
		add(letterInstrLabel);
		
		letterField = new JTextField();
		letterField.setBounds(446, 386, 157, 20);
		add(letterField);
		letterField.setColumns(10);
		
		JButton submitButton = new JButton("SUBMIT");
		submitButton.setBounds(666, 385, 120, 23);
		add(submitButton);	
		
		submitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				SendLetterWorker worker = new SendLetterWorker();
				worker.execute();
			}
		});	
		JRootPane rootPane = rootFrame.getRootPane();
	    rootPane.setDefaultButton(submitButton);	
	}
	private class SendLetterWorker extends SwingWorker<Void, String>{
		
		@Override
		protected Void doInBackground() throws Exception 
		{
				String letter = letterField.getText();
				letterField.setText("");
				if(letter.length()!= 1 ||( (letter.charAt(0)>'z' || letter.charAt(0)<'a') && (letter.charAt(0)>'Z' || letter.charAt(0)<'A'))){
					letterInstrLabel.setText("You can insert only one LETTER , please try again!");
					letterInstrLabel.setForeground(Color.RED);
				}
				else{
					String word = Hangman.getInstance().getWord(); 
					
					String[] wordToBe = new String[12];
					String wtb = wordLabel.getText();
				
					wordToBe = wtb.split(" ");
					
					if(word.toLowerCase().indexOf(letter.toLowerCase()) != -1){
						int ok=1;
						
						for(int i=0; i<wordToBe.length; i++){	//verificae daca a mai introdus litera o data
							if(wordToBe[i].toLowerCase().equals(letter.toLowerCase())){
								ok=0;
							}
						}
						
						if(ok==1){	//daca nu a mai introdus o e ok
							for(int i=0; i<word.length(); i++)
								if(word.toLowerCase().charAt(i) == letter.toLowerCase().charAt(0)){
									wordToBe[i]=letter.toUpperCase();
								}
							wordInstrLabel.setText("Very good! Keep up with the good work!");
			
							wtb = "";
							String aux="";
							for(int i=0; i<wordToBe.length; i++){
								wtb+=wordToBe[i];
								aux+=wordToBe[i];
								wtb+=" ";
							}
							wordLabel.setText(wtb);
							
							if(aux.equals(word)==true){	//in cazul in care a completat tot cuvantul
								letterField.setEditable(false);
								JOptionPane.showMessageDialog(null, "YOU WON!");
								TransferData sendObject = new TransferData();
								sendObject.setCode("back");
								sendObject.setRank(Player.getInstance().getRank() + 10);
								Player.getInstance().setRank(Player.getInstance().getRank() + 10);
								try{
									ConnectionHandler.getInstance().sendToServer(sendObject);
								}catch(IOException ioe){
									JOptionPane.showConfirmDialog(null,
											"The server did not respond. Close the application." ,
											"The server did not respond. Close the application.",
											JOptionPane.OK_OPTION);
								}
								rootFrame.changePanel(new MainPanel(rootFrame));
							}
						}
						else{	//cazul in care a mai introdus litera asta
							Hangman.getInstance().setLives(Hangman.getInstance().getLives()-1);
							String lives = "";
							lives+=Hangman.getInstance().getLives();
							livesLabel.setText(lives);
							if(Hangman.getInstance().getLives() == 0){
								hangmanImage.setIcon(new ImageIcon(".\\src\\main\\resources\\fire.png"));
								letterField.setEditable(false);
								JOptionPane.showMessageDialog(null, "YOU LOSE!\n The word was: "+Hangman.getInstance().getWord());
								TransferData sendObject = new TransferData();
								sendObject.setCode("back");
								try{
									ConnectionHandler.getInstance().sendToServer(sendObject);
								}catch(IOException ioe){
									JOptionPane.showConfirmDialog(null,
											"The server did not respond. Close the application." ,
											"The server did not respond. Close the application.",
											JOptionPane.OK_OPTION);
								}
								rootFrame.changePanel(new MainPanel(rootFrame));
							}
							else {
								hangmanImage.setIcon(new ImageIcon(".\\src\\main\\resources\\"+Hangman.getInstance().getLives()+".png"));
								wordInstrLabel.setText("You already choose this letter, are you out of ideas?");
							}
						}
					}
					else{	//cazul in care nu nimereste litera
						Hangman.getInstance().setLives(Hangman.getInstance().getLives()-1);
						String lives = "";
						lives+=Hangman.getInstance().getLives();
						livesLabel.setText(lives);
						if(Hangman.getInstance().getLives() == 0){
							hangmanImage.setIcon(new ImageIcon(".\\src\\main\\resources\\fire.png"));
							letterField.setEditable(false);
							JOptionPane.showMessageDialog(null, "YOU LOSE!\n The word was: "+Hangman.getInstance().getWord());
							TransferData sendObject = new TransferData();
							sendObject.setCode("back");
							try{
								ConnectionHandler.getInstance().sendToServer(sendObject);
							}catch(IOException ioe){
								JOptionPane.showConfirmDialog(null,
										"The server did not respond. Close the application." ,
										"The server did not respond. Close the application.",
										JOptionPane.OK_OPTION);
							}
							rootFrame.changePanel(new MainPanel(rootFrame));
						}
						else{
							hangmanImage.setIcon(new ImageIcon(".\\src\\main\\resources\\"+Hangman.getInstance().getLives()+".png"));
							wordInstrLabel.setText("That's a shame! We know this time you will get it right!");
						}
					}					
				}
				wordInstrLabel.setText("Here is your word to guess, good luck!");		
			return null;

		}
	}		
}











