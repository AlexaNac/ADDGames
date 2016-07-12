package main;

import database.DatabaseHandler;
import networking.Server;

public class MainClass {

	public static void main(String[] args) {
		DatabaseHandler.getInstance("root", "root");
		
		Server.getInstance().start(60110);
	}

}
