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
	private String userID;
	private String userPW;
	
	public adminDAO(String userID, String userPW) throws Exception{
		this.userID = userID;
		this.userPW = userPW;
		
		String dbURL = "jdbc:mysql://localhost:3306/" + userID + "?";
		Class.forName("com.mysql.cj.jdbc.Driver");	
		conn = DriverManager.getConnection(dbURL, userID, userPW);
	}
	
	public int connect() {
		try{
			Socket soc = new Socket("localhost", 6000);
			//System.out.println(getTime() + " Accept to Server Success...");
			
			BufferedReader br = new BufferedReader(new InputStreamReader(soc.getInputStream()));
			PrintWriter pw = new PrintWriter(soc.getOutputStream());
			
			pw.println(userID);
			pw.println(userPW);
			int result = -1;
			if(br.readLine().equals("complete")) {
				result = 1;
				if(keyCheck(userID) == -1) {
					RSA rsa = new RSA();
					insertKey(userID, rsa.KeyToStr(rsa.getPublicKey()), rsa.KeyToStr(rsa.getPrivateKey()));
					pw.println(rsa.KeyToStr(rsa.getPublicKey()));
				}
			}
		
			pw.flush();

			soc.close();
			return result;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return -2;
	}
	
	public int isUser() {
		String sql = "SELECT userID FROM center.VIEW_USER WHERE userID = ?;";
		try {
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

	private int keyCheck(String userID) {
		String sql = "SELECT publicKey FROM center.VIEW_USER where userID = ?;";
		try {
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
		return -2;
	}
	
	private int insertKey(String userID, String publicKey, String privateKey) {
		String sql = "INSERT INTO " + userID +"_RSAKEY(publicKey, privateKey) VALUES(?, ?);";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, publicKey);
			pstmt.setString(2, privateKey);
			pstmt.executeUpdate();
			return 1;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return -1;	// db error
	}

}
