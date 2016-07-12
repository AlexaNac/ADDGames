package gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import com.mysql.fabric.xmlrpc.Client;

import client.ConnectionHandler;
import networking.Player;
import networking.TransferData;

public class SignupPanel extends JPanel {

	private final MainFrame rootFrame;
	
	private JTextField usernameField;
	private JPasswordField passwordField;
	private JPasswordField confirmPasswordField;
	//private JLabel logoLabel;
	private JLabel wrongUsernameLabel;
	private JLabel emptyUsernameLabel;
	private JLabel wrongPasswordLabel;
	private JLabel emptyPasswordLabel;
	
	private SwingWorker<Void,String> singupReader;
	private TransferData receivedObject;
	
	/**
	 * Create the panel.
	 * @param rootFrame
	 */
	public SignupPanel(MainFrame rootFrame) {
		this.rootFrame = rootFrame;
		initComponents();
		
	}
	private void initComponents(){
		rootFrame.setSize(560,430);
		setBounds(100, 100, 560, 425);
		this.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.setBackground(new Color(255, 255, 255));
		this.setBorder(new MatteBorder(1, 0, 0, 0, (Color) new Color(0, 0, 0)));
		//this.rootFrame.setResizable(false);
		setLayout(null);
		
		JButton backButton = new JButton("<<Back");
		backButton.setFont(new Font("Tahoma", Font.BOLD, 12));
		backButton.setForeground(new Color(0, 0, 51));
		backButton.setBounds(10, 11, 89, 23);
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rootFrame.changePanel(new LoginPanel(rootFrame));
			}
		});
		setLayout(null);
		add(backButton);
		
		JLabel usernameLabel = new JLabel("Username");
		usernameLabel.setForeground(new Color(0, 0, 51));
		usernameLabel.setFont(new Font("Tahoma", Font.BOLD, 15));
		usernameLabel.setBounds(112, 169, 83, 17);
		this.add(usernameLabel);
		
		usernameField = new JTextField();
		usernameField.setBounds(239, 169, 246, 20);
		this.add(usernameField);
		usernameField.setColumns(10);
		
		JLabel passwordLabel = new JLabel("Password");
		passwordLabel.setForeground(new Color(0, 0, 51));
		passwordLabel.setFont(new Font("Tahoma", Font.BOLD, 15));
		passwordLabel.setBounds(112, 219, 83, 19);
		this.add(passwordLabel);
		
		passwordField = new JPasswordField();
		passwordField.setBounds(239, 219, 246, 20);
		this.add(passwordField);
		
		JLabel passwordConfirmLabel = new JLabel("Confirm password");
		passwordConfirmLabel.setForeground(new Color(0, 0, 51));
		passwordConfirmLabel.setFont(new Font("Tahoma", Font.BOLD, 15));
		passwordConfirmLabel.setBounds(48, 270, 147, 17);
		this.add(passwordConfirmLabel);
		
		confirmPasswordField = new JPasswordField();
		confirmPasswordField.setBounds(239, 270, 246, 20);
		this.add(confirmPasswordField);
		
		JButton signupButton = new JButton("Sign Up");
		signupButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				registerButtonActionPerformed(e);
			}
		});
		signupButton.setFont(new Font("Tahoma", Font.BOLD, 15));
		signupButton.setForeground(new Color(0, 0, 51));
		
		signupButton.setBounds(206, 349, 147, 27);
		this.add(signupButton);
		
		JRootPane rootPane = rootFrame.getRootPane();
	    rootPane.setDefaultButton(signupButton);
		
		JLabel lblWelcomePleaseSign = new JLabel("WELCOME!");
		lblWelcomePleaseSign.setForeground(new Color(0, 0, 51));
		lblWelcomePleaseSign.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblWelcomePleaseSign.setBounds(227, 71, 95, 46);
		this.add(lblWelcomePleaseSign);
		
		wrongPasswordLabel = new JLabel("");
		wrongPasswordLabel.setForeground(Color.RED);
		wrongPasswordLabel.setBounds(239, 250, 246, 14);
		add(wrongPasswordLabel);
		
		emptyUsernameLabel = new JLabel("");
		emptyUsernameLabel.setForeground(Color.RED);
		emptyUsernameLabel.setBounds(239, 141, 246, 14);
		add(emptyUsernameLabel);
		
		wrongUsernameLabel = new JLabel("");
		wrongUsernameLabel.setForeground(Color.RED);
		wrongUsernameLabel.setBounds(112, 141, 373, 17);
		add(wrongUsernameLabel);
		
		emptyPasswordLabel = new JLabel("");
		emptyPasswordLabel.setForeground(Color.RED);
		emptyPasswordLabel.setBounds(239, 200, 246, 14);
		add(emptyPasswordLabel);
	}
	
	private void registerButtonActionPerformed(ActionEvent e){
		wrongUsernameLabel.setText("");
		wrongPasswordLabel.setText("");
		emptyPasswordLabel.setText("");
		passwordField.setBorder(BorderFactory.createLineBorder(Color.black));
		confirmPasswordField.setBorder(BorderFactory.createLineBorder(Color.black));
		if (checkFields()){
			SwingWorker<Void,String> signupSender = new SwingWorker<Void,String>(){
				boolean flag = true;
				@Override
				protected Void doInBackground() throws Exception {
					
		            String username = usernameField.getText();
		            String password = new String(passwordField.getPassword());
		            
		            if(checkUsername(username)){
		            	passwordField.setText("");
		                confirmPasswordField.setText("");
		                Player.getInstance().setUsername(username);
		                Player.getInstance().setPassword(password);
		                
		                TransferData sendObject = new TransferData();
		                sendObject.setCode("signup");
		        		sendObject.setUsername(Player.getInstance().getUsername());
		        		sendObject.setPassword(Player.getInstance().getPassword());
		        		
		        		try{
		        			ConnectionHandler.getInstance().sendToServer(sendObject);
		        		}catch(Exception e){
	
		        			flag = false;
		        		}
		                
		                
		            }
					return null;
				}
				 @Override
		        protected void done(){
					 if(flag == true)
						 signupResponse();
					 else{
						 wrongUsernameLabel.setText("Failed to send the message to sever. Try again later.");
		                	usernameField.setText("");
							passwordField.setText("");
							confirmPasswordField.setText("");
					 }
		        }
		        
		        private boolean checkUsername(String username){
		            char[] unacceptebleChars = {'\'','=','+',';','\"'};
		            for(char c:unacceptebleChars){
		                if(username.indexOf(c) >= 0){
		                    wrongUsernameLabel.setText("Username can't contain these characters: ' = + ; \"");
		                	usernameField.setText("");
							passwordField.setText("");
							confirmPasswordField.setText("");
		                    return false;
		                }
		            }
		            return true;
		        }
			};
			signupSender.execute();
		}      
	
	}
	protected void signupResponse() {
		singupReader = new SwingWorker<Void,String>(){
	
			@Override
			protected Void doInBackground() throws Exception {
				while(true)
				{
					try
					{
						
						receivedObject = (TransferData) ConnectionHandler.getInstance().readFromServer();
						if(receivedObject == null){
							
							JOptionPane.showConfirmDialog(null,
									"Failed to read from sever. Close the application." ,
									"Failed to read from sever. Close the application.",
									JOptionPane.YES_OPTION);
							break;
						}
						if (receivedObject.getCode().equals("OK"))
							break;
						if (receivedObject.getCode().equals("Username already exists."))
							wrongUsernameLabel.setText("Username already exists.");
							
					}
					catch(ClassNotFoundException e)
					{
						e.printStackTrace();
						ConnectionHandler.getInstance().close();
						break;
					}
					catch(IOException ioe)
					{
						ioe.printStackTrace();
						ConnectionHandler.getInstance().close();
						break;
					}
				}
				return null;
			}
			protected void done()
			{
				 rootFrame.changePanel(new MainPanel(rootFrame));
			}
			
		};
		singupReader.execute();
		
	}
	
	private boolean checkFields(){
		boolean res = true;
		String conf = new String(confirmPasswordField.getPassword());
		String password = new String(passwordField.getPassword());
		String user = new String(usernameField.getText());
		if (user.equals("")){
			usernameField.setBorder(BorderFactory.createLineBorder(Color.red));
			emptyUsernameLabel.setText("This field can not be empty.");
			res = false;
		}
		if (password.equals("")){
			passwordField.setBorder(BorderFactory.createLineBorder(Color.red));
			emptyPasswordLabel.setText("This field can not be empty.");
			res = false;
		}
		if (conf.equals("")){
				confirmPasswordField.setBorder(BorderFactory.createLineBorder(Color.red));
				wrongPasswordLabel.setText("This field can not be empty.");
				res = false;
		}
		if (!password.equals("") && !conf.equals("") && !conf.equals(password)){
					res = false;
					wrongPasswordLabel.setText("Password does not match.");
					emptyPasswordLabel.setText("");
					wrongUsernameLabel.setText("");
					confirmPasswordField.setText("");
		}
				
		return res;
	}

}


