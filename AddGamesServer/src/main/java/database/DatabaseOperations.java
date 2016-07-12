package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

import networking.Player;

public class DatabaseOperations {
	private static final String DB_NAME = "ADDGames_DB";
	// JDBC driver name and database URL
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost/"+DB_NAME;
    
    //  Database attributes
    private static String USER = "root";
    private static String PASS = "root";
    private static Connection conn;
    
    public DatabaseOperations() {
      
    }
    
    private static void closeConnection() {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }
    
    private static void preliminaries() {
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (SQLException se) {
            // Handle errors for JDBC   
            se.printStackTrace();
        } catch (Exception e) {
            // Handle errors for Class.forName
            e.printStackTrace();
        }
    }
    
    public static String insertPlayer(Player p){
    	preliminaries();
        String result = "OK";
    	java.sql.PreparedStatement preparedStatement = null;
        try {
           
            String sql = "INSERT INTO player"
            		+ " (username, password, rank) "
            		+ " values ( ? , ? , ? );";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, p.getUsername());
            preparedStatement.setString(2, p.getPassword());
            preparedStatement.setInt(3, p.getRank());
            preparedStatement.execute(); 
          

        }catch(SQLIntegrityConstraintViolationException e){
        	//e.printStackTrace();
        	result = "Username already exists.";
        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (preparedStatement != null) {
                	 preparedStatement.close();
                }
            } catch (SQLException se2) {
                closeConnection();
                return result;
            }
        }
        closeConnection();
        return result;
	}
   
    public static String searchPlayer(Player p){
		preliminaries();
		ResultSet res = null;
    	java.sql.PreparedStatement preparedStatement = null;
    	String result=null;
        try {
           
            String sql = "select username,password from player where username=?";
            		
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, p.getUsername());
            res = preparedStatement.executeQuery(); 
            
			int i = 0;
			while(res.next()){
				i++;
				if(res.getString(2).trim().toLowerCase().equals(p.getPassword())){
					result = "OK";
				}else{
					result = "WRONG PASSWORD";	
				}
			}
			if(i == 0){
				result = "WRONG USERNAME";
			}
			  

        }  catch (Exception e) {
           e.printStackTrace();
        } finally {
            try {
                if (preparedStatement != null) {
                	 preparedStatement.close();
                }
            } catch (SQLException se2) {
                closeConnection();
            }
        }
        closeConnection();
        return result;
    }
    public static int setRank(String username, int rank){
		preliminaries();
		
    	java.sql.PreparedStatement preparedStatement = null;
    	int result = 0;
        try {
           
            String sql = "update addgames_db.player set rank = " + rank + " where username = ?";
            		
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, username);
            result = preparedStatement.executeUpdate(); 
            
			

        }  catch (Exception e) {
           e.printStackTrace();
        } finally {
            try {
                if (preparedStatement != null) {
                	 preparedStatement.close();
                }
            } catch (SQLException se2) {
                closeConnection();
            }
        }
        closeConnection();
        return result;
    }
    
    public static int getRank(String username){
		preliminaries();
		ResultSet res = null;
    	java.sql.PreparedStatement preparedStatement = null;
    	int result = 0;
        try {
           
            String sql = "select rank from player where username=?";
            		
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, username);
            res = preparedStatement.executeQuery(); 
            
			int i = 0;
			while(res.next()){
				i++;
				result = res.getInt(1);
			}
			if(i == 0){
				result = -1;
			}
			  

        }  catch (Exception e) {
           e.printStackTrace();
        } finally {
            try {
                if (preparedStatement != null) {
                	 preparedStatement.close();
                }
            } catch (SQLException se2) {
                closeConnection();
            }
        }
        closeConnection();
        return result;
    }
    
    public static String[] getWordForHangman(int id){
    	String[] word = new String[2];
    	preliminaries();
		ResultSet res = null;
    	java.sql.PreparedStatement preparedStatement = null;
    	
        try {
           
            String sql = "select word,wordshown from HANGMANWORDS where id=?";
            		
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            res = preparedStatement.executeQuery(); 
            
			int i = 0;
			while(res.next()){
				i++;
				word[0] = res.getString(1);
				word[1] = res.getString(2);
			}
			if(i == 0){
				word = null;
			}
			  

        }  catch (Exception e) {
           e.printStackTrace();
        } finally {
            try {
                if (preparedStatement != null) {
                	 preparedStatement.close();
                }
            } catch (SQLException se2) {
                closeConnection();
            }
        }
        closeConnection();
        return word;
    	
    }
    public static Boolean findPlayer(String username) {
        preliminaries();
        ResultSet result = null;
        java.sql.PreparedStatement preparedStatement = null;
        Boolean res = false;
        try {
            String  sqlQuery = "select username from player where username=?";
            preparedStatement = conn.prepareStatement(sqlQuery);

            preparedStatement.setString(1, username);

            result = preparedStatement.executeQuery();

            if (result.next() == true)
            	res = true;

        } catch (SQLException ex) {
            closeConnection();
        } finally {
            //finally block used to close resources
            closeConnection();
            try {
                if (preparedStatement != null) 
                	preparedStatement.close();
                
            } catch (SQLException se) {
                closeConnection();
                
            }
        }
        return res;
    }
    
}
