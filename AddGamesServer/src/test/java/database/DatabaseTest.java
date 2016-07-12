package database;


import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import org.junit.Test;

import networking.Player;

/**
 *
 * @author root
 */
public class DatabaseTest {
    private final String[] tableNames;
    
    public DatabaseTest() {
        DatabaseHandler.getInstance("root","root");
        tableNames = new String[]{"PLAYER", "GAME", "GAME_HISTORY", "HANGMANWORDS"};
    }
    
    @Test
    public void connectionTest() throws SQLException {
        boolean result = DatabaseHandler.getInstance("root","root").testConnection();
        assertEquals(result, true);
    }
    
    @Test
    public void tableCreationTest() {
        for (String tableName: tableNames)
            assertEquals(DatabaseHandler.getInstance("root","root").tableExists(tableName), true);
    }
    
    @Test
    public void sqlInjectionTest1() {
        String username = "test';DROP TABLE PLAYER;--";
        Player p = new Player();
        p.setUsername(username);
        p.setPassword("test");
        DatabaseOperations.searchPlayer(p);
        assertEquals(DatabaseHandler.getInstance("root","root").tableExists("PLAYER"), true);
    }
    
    @Test
    public void sqlInjectionTest2() {
        String username = "diana' OR 1=1/*";
        assertEquals(DatabaseOperations.findPlayer(username), false);
        
    }
    
 
}