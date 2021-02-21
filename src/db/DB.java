package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DB {
	protected Connection conn;
	protected ResultSet rs;
	
	public DB() {
		try {
			String dbURL = "jdbc:mysql://localhost:3306/block?";
			String dbID = "root";
			String dbPassword = "Benel&Bende1";
			Class.forName("com.mysql.cj.jdbc.Driver");	
			conn = DriverManager.getConnection(dbURL, dbID, dbPassword);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	protected int next(String table, String name) {
		String sql = "SELECT * FROM " + table + " WHERE name LIKE ?;";
		int count = 1;
		try {
			PreparedStatement pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, name+"%");
			rs = pstmt.executeQuery();
			while(rs.next()) {
				count++;
			}
			return count;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return count; //데이터베이스 오류
	}
}
