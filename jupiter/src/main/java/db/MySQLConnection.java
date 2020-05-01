package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import entity.Item;
import entity.Item.ItemBuilder;

// save, delete, getFavoriteItem

//CRUD: buld connection, sql statement, statement.setString, statement.executeUpdate();
public class MySQLConnection {
	// connection, connect to database. java and db connection. 
	private Connection conn;
    
	// constructor. aim to be used when external class MySQLConnection, we can use?
	public MySQLConnection() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
			conn = DriverManager.getConnection(MySQLDBUtil.URL); // connection null check: 1. throw exception 2. no instance. so conn will still be null. NPE. so null check in MySQLTableCreation.

		} catch (Exception e) { //SQLException
			e.printStackTrace(); //cache blog, printStackTrace(), stack called stack by stack. trace print, for debugging.
		// we can also customize our actions to do some logical actions.
			// check getConnection, SQLException, java.lang.exception child class. In java, all exception could be caught by java.lang.exception.
		}
	}

	public void close() {
		if (conn != null) {
			try {
				conn.close(); //exception. as long as have interaction with others, might have exception and we should try catch.
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	//business logic,
    // pass data to database and read database

	public void setFavoriteItems(String userId, Item item) {
		// null check only when interacting with DB
		if (conn == null) {
			System.err.println("DB connection failed"); // err.println, err red
			return;
		}
		// save user like history
		saveItem(item);
		String sql = "INSERT INTO history (user_id, item_id) VALUES (?, ?)";
		try {
			//prepared statement could let us put varaible to ?. compared with Statement statement = conn.createStatement(); in MySQLTableCreation
			//String format %s, to connect, could also use it, but not necessary safe. values(insert to, ?) , not safe. function of PreparedStatement: "string"->?
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, item.getItemId());
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// history table has foreign keys, so I might disobey constraint. coz user_id might not exist. we need to pass user_id into when everytime we call it. so sign in, session check each step. item_id might constrain violation, so when it's added history, we should save item, helper method saveItem.
	}
	
	public void unsetFavoriteItems(String userId, String itemId) {
		// itemID rather than id, coz delete no need to save.
		if (conn == null) {
			System.err.println("DB connection failed");
			return;
		}
		String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, itemId);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void saveItem(Item item) {
		// null check
		if (conn == null) {
			System.err.println("DB connection failed");
			return;
		}
		// if item exists in the db, pk violation, so item exists. ignore. do thing.
		String sql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?)";
		// ? num , setString num.
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, item.getItemId());
			statement.setString(2, item.getName());
			statement.setString(3, item.getAddress());
			statement.setString(4, item.getImageUrl());
			statement.setString(5, item.getUrl());
			statement.executeUpdate();
			
			// save item, every time, we need to save keyword into it. 
			sql = "INSERT IGNORE INTO keywords VALUES (?, ?)";
                    statement = conn.prepareStatement(sql);
			statement.setString(1, item.getItemId());
			for (String keyword : item.getKeywords()) {
				statement.setString(2, keyword);
				statement.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	




	
	//get from DB
	public Set<String> getFavoriteItemIds(String userId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return new HashSet<>();
		}

		Set<String> favoriteItems = new HashSet<>();

		try {
			String sql = "SELECT item_id FROM history WHERE user_id = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();
			// reader, each time each row.
			while (rs.next()) {
				String itemId = rs.getString("item_id");
				favoriteItems.add(itemId);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return favoriteItems;
	}
	



	public Set<Item> getFavoriteItems(String userId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return new HashSet<>();
		}
		Set<Item> favoriteItems = new HashSet<>();
		Set<String> favoriteItemIds = getFavoriteItemIds(userId);

		String sql = "SELECT * FROM items WHERE item_id = ?";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			for (String itemId : favoriteItemIds) {
				statement.setString(1, itemId);
				ResultSet rs = statement.executeQuery();

				ItemBuilder builder = new ItemBuilder();
				if (rs.next()) {
					builder.setItemId(rs.getString("item_id"));
					builder.setName(rs.getString("name"));
					builder.setAddress(rs.getString("address"));
					builder.setImageUrl(rs.getString("image_url"));
					builder.setUrl(rs.getString("url"));
					builder.setKeywords(getKeywords(itemId));
					favoriteItems.add(builder.build());
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return favoriteItems;
	}
    // keywrod as a table. keyword is a set. set is not repetitive.
	public Set<String> getKeywords(String itemId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return null;
		}
		Set<String> keywords = new HashSet<>();
		String sql = "SELECT keyword from keywords WHERE item_id = ? ";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, itemId);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				String keyword = rs.getString("keyword");
				keywords.add(keyword);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return keywords;
	}
  

	public String getFullname(String userId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return null;
		}
		String name = "";
		String sql = "SELECT first_name, last_name FROM users WHERE user_id = ? ";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				name = rs.getString("first_name") + " " + rs.getString("last_name");
				
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		
		
		return name;
	}

	public boolean verifyLogin(String userId, String password) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return false;
		}
		String sql = "SELECT user_id FROM users WHERE user_id = ? AND password = ?";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, password);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return false;

	}
	
	public boolean addUser(String userId, String password, String firstname, String lastname) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return false;
		}
  // when duplicate, ignore, skip, duplicate.
		String sql = "INSERT IGNORE INTO users VALUES (?, ?, ?, ?)";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, password);
			statement.setString(3, firstname);
			statement.setString(4, lastname);

			// means update how many lines. we expect 1, coz we only put one piece of info inside.  == 1, true false. 
			return statement.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
}



