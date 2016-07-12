package gui;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;

public class GUITest {

	@Test
	public void checkLoginUsernameTest() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		MainFrame mf = MainFrame.getInstance();
		LoginPanel d = new LoginPanel(mf);
        Method m;
		try {
			m = LoginPanel.class.getDeclaredMethod("checkUsername");
			m.setAccessible(true);
	        boolean output=  (boolean) m.invoke(d,"/d=i;mi");
			assertEquals(output,false);
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void checkSignupUsernameTest() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		MainFrame mf = MainFrame.getInstance();
		SignupPanel d = new SignupPanel(mf);
        Method m;
		try {
			m = SignupPanel.class.getDeclaredMethod("checkUsername");
			m.setAccessible(true);
	        boolean output=  (boolean) m.invoke(d,"/d=i;mi");
			assertEquals(false,output);
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
		
	}
}
