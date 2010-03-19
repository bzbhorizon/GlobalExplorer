package bzb.se;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import bzb.se.meta.Databases;

public class DB {
	
	public Connection con;
	
	public Statement stmt;
	
	public DB () {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			final String url = "jdbc:mysql://" + Databases.Host + ":" + Databases.Port + "/"
					+ Databases.Name;
			con = DriverManager.getConnection(url, Databases.User, Databases.Pass);

			this.stmt = con.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		try {
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void cleanDB() {
		// clean up leftovers in database for a clean run
		try {
			System.out.println("Cleaning DB ...");

			this.stmt.executeUpdate("DELETE FROM monitor");
			this.stmt.executeUpdate("DELETE FROM installations");
			this.stmt.executeUpdate("DELETE FROM visitors");
			this.stmt.executeUpdate("DELETE FROM storylines");
			this.stmt.executeUpdate("DELETE FROM visits");
			
			System.out.println("... done");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
