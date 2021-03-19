package security;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class adminDAO {
	private Connection conn;
	private ResultSet rs;

	public int connect(String userID, String userPW) {
		try{	
			Socket soc = new Socket("localhost", 6013);
			
			BufferedReader br = new BufferedReader(new InputStreamReader(soc.getInputStream()));
			PrintWriter pw = new PrintWriter(soc.getOutputStream());
			
			System.out.println(" Accept to Server Success...");
			pw.println(userID);
			pw.println(userPW);
			pw.flush();
			
			int result = -1;
			if(br.readLine().equals("complete"))
				result = 1;
			soc.close();
			return result;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return -2;
	}
	
	public int isUser(String userID) {		
		String sql = "SELECT userID FROM VIEW_USER WHERE userID = ?;";
		try {
			String dbURL = "jdbc:mysql://localhost:3306/center?";
			Class.forName("com.mysql.cj.jdbc.Driver");	
			conn = DriverManager.getConnection(dbURL, "root", "Benel&Bende1");
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userID);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				return 1;
			}
			return -1;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return -2;	// db error
	}
}
