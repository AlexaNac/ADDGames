package gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.MatteBorder;

import client.ConnectionHandler;
import networking.Player;
import networking.TransferData;

public class LoginPanel extends JPanel {

	private final MainFrame rootFrame;
	
	private JTextField usernameField;
	private JPasswordField passwordField;
	private JLabel wrongLabel;
	private JLabel signUpLabel;
	private JLabel logoLabel;
	private JButton signupButton;
	
	
	/**
	 * Create the panel.
	 * @param rootFrame
	 */
	public LoginPanel(MainFrame rootFrame) {
		setBackground(Color.WHITE);
		this.rootFrame = rootFrame;
 		initComponents();
		
	}
	
	private void initComponents(){
		rootFrame.setSize(560,430);
		setBounds(100, 100, 562, 426);
		this.setBorder(new MatteBorder(1, 0, 0, 0, (Color) new Color(0, 0, 0)));
		
		setLayout(null);
		
		logoLabel = new JLabel("");
		logoLabel.setIcon(new ImageIcon(".\\src\\main\\resources\\logo.png"));
		logoLabel.setBounds(62, 11, 212, 189);
		this.add(logoLabel);
		
		signUpLabel = new JLabel("You don't have an account? ");
		signUpLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
		signUpLabel.setForeground(new Color(0, 0, 51));
		signUpLabel.setBounds(310, 75, 175, 20);
		this.add(signUpLabel);
		
		signupButton = new JButton("Sign Up");
		signupButton.setFont(new Font("Tahoma", Font.BOLD, 12));
		signupButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rootFrame.changePanel(new SignupPanel(rootFrame));
			}
		});
		signupButton.setForeground(new Color(0, 0, 51));
		
		signupButton.setBounds(352, 105, 91, 23);
		this.add(signupButton);
		
		JLabel usernameLabel = new JLabel("Username");
		usernameLabel.setForeground(new Color(0, 0, 51));
		usernameLabel.setFont(new Font("Tahoma", Font.BOLD, 15));
		usernameLabel.setBounds(82, 239, 83, 17);
		this.add(usernameLabel);
		
		usernameField = new JTextField();
		usernameField.setBounds(239, 239, 246, 20);
		this.add(usernameField);
		usernameField.setColumns(10);
		
		wrongLabel = new JLabel("");
		wrongLabel.setForeground(Color.RED);
		wrongLabel.setBounds(82, 214, 403, 14);
		this.add(wrongLabel);
		
		JLabel passwordLabel = new JLabel("Password");
		passwordLabel.setForeground(new Color(0, 0, 51));
		passwordLabel.setFont(new Font("Tahoma", Font.BOLD, 15));
		passwordLabel.setBounds(82, 296, 83, 17);
		this.add(passwordLabel);
		
		passwordField = new JPasswordField();
		passwordField.setBounds(240, 296, 246, 20);
		this.add(passwordField);
		
		JButton loginButton = new JButton("Log in");
		loginButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				loginButtonActionPerformed(arg0);
			}
		});
		loginButton.setForeground(new Color(0, 0, 51));
		loginButton.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 15));
		this.add(wrongLabel);
		loginButton.setBounds(195, 352, 141, 23);
		this.add(loginButton);
		
		JRootPane rootPane = rootFrame.getRootPane();
	    rootPane.setDefaultButton(loginButton);
	}
	
	
	
	protected void loginButtonActionPerformed(ActionEvent arg0) {
		wrongLabel.setText("");
		LoginWorker worker = new LoginWorker();
        try {
            worker.execute();
            boolean b = worker.get();
            if(b){
               
            	worker.cancel(true);
                rootFrame.changePanel(new MainPanel(rootFrame));
            }
        } catch (InterruptedException | ExecutionException ex) {
        	ex.printStackTrace();
        }
		
	}



	private class LoginWorker extends SwingWorker<Boolean, String>{

        @Override
        protected Boolean doInBackground() {
            //This should start false
            boolean success = false;
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            
            if(checkUsername(username)){
            	passwordField.setText("");
                Player.getInstance().setUsername(username);
                Player.getInstance().setPassword(password);
                success = true;
                //String res = ConnectionHandler.getInstance().DatabaseOperations.searchPlayer(Player.getInstance());
                TransferData sendObject = new TransferData();
                sendObject.setCode("login");
        		sendObject.setUsername(Player.getInstance().getUsername());
        		sendObject.setPassword(Player.getInstance().getPassword());
        		boolean flag = true;
        		try{
        			ConnectionHandler.getInstance().sendToServer(sendObject);
        		}catch(Exception e){

        			flag = false;
        		}
                
                String res = "flag";
                TransferData responseObject;
                while(flag){
                	try {
						responseObject = ConnectionHandler.getInstance().readFromServer();
						if(responseObject == null){
							
							JOptionPane.showConfirmDialog(null,
									"Failed to read from sever. Close the application." ,
									"Failed to read from sever. Close the application.",
									JOptionPane.YES_OPTION);
							break;
						}
	                	Player.getInstance().setRank(responseObject.getRank());
	                	
	                	res = responseObject.getCode();
	                	if(!res.equals("flag"))
	                		flag = false;
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                	
                }
                if (res.equals("WRONG PASSWORD") || res.equals("WRONG USERNAME")){
					wrongLabel.setText("WRONG USERNAME OR PASSWORD, TRY AGAIN!");
					usernameField.setText("");
					passwordField.setText("");
					success = false;
                }
                if(res.equals("flag")){
                	success = false;
                	wrongLabel.setText("Failed to send the message to sever. Try again later.");
					usernameField.setText("");
					passwordField.setText("");
                }
            }
            
            return success;
        }
      
        private boolean checkUsername(String username){
            char[] unacceptebleChars = {'\'','=','+',';','\"'};
            for(char c:unacceptebleChars){
                if(username.indexOf(c) >= 0){
                	wrongLabel.setText("Username can't contain these characters: ' = + ; \"");
                	usernameField.setText("");
					passwordField.setText("");
                    return false;
                }
            }
            return true;
        }
        
    }
}
