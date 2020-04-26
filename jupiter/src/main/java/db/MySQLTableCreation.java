package db;

import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.Connection;

public class MySQLTableCreation {
	// Run this as Java application to reset the database.
	public static void main(String[] args) { // one time thing. table only needs to be created for one time.
		// Connection conn = 
		try {
			// Step 1 create connection to database. Connect to MySQL.
			System.out.println("Connecting to " + MySQLDBUtil.URL);
			// driver helps us to connect to database.
			Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
			// automatically, in getConnection, the instance, registeredDrivers, get connection would be automatically connected to Driver. 
			/* Driver driver = new Driver();
			 * 
			 */
			Connection conn = DriverManager.getConnection(MySQLDBUtil.URL);
            
			//corner case
			if (conn == null) {
				System.out.print("connection failed");
				return;
			}
			
			// Step 2 Drop tables in case they exist. one time thing. when run one more time, need to be first dropped.
			Statement statement = conn.createStatement();
			String sql = "DROP TABLE IF EXISTS keywords";
			statement.executeUpdate(sql);

			sql = "DROP TABLE IF EXISTS history";
			statement.executeUpdate(sql);

			sql = "DROP TABLE IF EXISTS items";
			statement.executeUpdate(sql);

			sql = "DROP TABLE IF EXISTS users";
			statement.executeUpdate(sql);

			// Step 3 Create new tables
			// varchar 255, string changable length; not null, primary key. 
			sql = "CREATE TABLE items ("
					+ "item_id VARCHAR(255) NOT NULL,"
					+ "name VARCHAR(255),"
					+ "address VARCHAR(255),"
					+ "image_url VARCHAR(255),"
					+ "url VARCHAR(255),"
					+ "PRIMARY KEY (item_id)"
					+ ")";
			statement.executeUpdate(sql);

			sql = "CREATE TABLE users ("
					+ "user_id VARCHAR(255) NOT NULL,"
					+ "password VARCHAR(255) NOT NULL,"
					+ "first_name VARCHAR(255),"
					+ "last_name VARCHAR(255),"
					+ "PRIMARY KEY (user_id)"
					+ ")";
			statement.executeUpdate(sql);

			sql = "CREATE TABLE keywords ("
					+ "item_id VARCHAR(255) NOT NULL,"
					+ "keyword VARCHAR(255) NOT NULL,"
					+ "PRIMARY KEY (item_id, keyword),"
					+ "FOREIGN KEY (item_id) REFERENCES items(item_id)"
					+ ")";
			statement.executeUpdate(sql);

			sql = "CREATE TABLE history ("
					+ "user_id VARCHAR(255) NOT NULL,"
					+ "item_id VARCHAR(255) NOT NULL,"
					+ "last_favor_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
					+ "PRIMARY KEY (user_id, item_id),"
					+ "FOREIGN KEY (user_id) REFERENCES users(user_id),"
					+ "FOREIGN KEY (item_id) REFERENCES items(item_id)"
					+ ")";
			statement.executeUpdate(sql);

			// Step 4: insert fake user 1111/3229c1097c00d497a0fd282d586be050 for testing
			sql = "INSERT INTO users VALUES('1111', '3229c1097c00d497a0fd282d586be050', 'John', 'Smith')";
			statement.executeUpdate(sql);
			
			// if put inside, mistake before , conn would not be closed. absolute correct is to put in finally. but complex to write, need to write conn outside and try catch below. trade-off for us.
			conn.close();
			System.out.println("Import done successfully");

		} catch (Exception e) {
			e.printStackTrace();
		}
//		} finally {
//			try {
//				conn.close();
//			} catch
//		}
	}
}
