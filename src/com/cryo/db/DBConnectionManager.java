package com.cryo.db;

import java.util.HashMap;

import com.cryo.Website;
import com.cryo.db.impl.AccountConnection;
import com.cryo.db.impl.DisplayConnection;
import com.cryo.db.impl.EmailConnection;
import com.cryo.db.impl.ForumConnection;
import com.cryo.db.impl.HighscoresConnection;
import com.cryo.db.impl.PunishmentConnection;
import com.cryo.db.impl.ReportsConnection;
import com.cryo.db.impl.ShopConnection;
import com.cryo.db.impl.VotingConnection;

/**
 * @author Cody Thompson <eldo.imo.rs@hotmail.com>
 *
 * Created on: Mar 7, 2017 at 7:35:35 PM
 */
public class DBConnectionManager {
	
	private HashMap<Connection, DatabaseConnection> connections;
	
	public DBConnectionManager() {
		loadDriver();
		init();
		Website.LOADED = true;
	}
	
	public DatabaseConnection getConnection(Connection connection) {
		return connections.get(connection);
	}
	
	public void loadDriver() {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void init() {
		connections = new HashMap<>();
		connections.put(Connection.FORUMS, new ForumConnection());
		connections.put(Connection.HIGHSCORES, new HighscoresConnection());
		connections.put(Connection.ACCOUNT, new AccountConnection());
		connections.put(Connection.DISPLAY, new DisplayConnection());
		connections.put(Connection.VOTING, new VotingConnection());
		connections.put(Connection.EMAIL, new EmailConnection());
		connections.put(Connection.SHOP, new ShopConnection());
		connections.put(Connection.REPORTS, new ReportsConnection());
		connections.put(Connection.PUNISH, new PunishmentConnection());
	}
	
	public static enum Connection {
		FORUMS, HIGHSCORES, ACCOUNT, DISPLAY, VOTING, EMAIL, SHOP, REPORTS, PUNISH
	}
	
}
