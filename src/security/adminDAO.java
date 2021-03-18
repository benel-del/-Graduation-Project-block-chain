package security;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class adminDAO {
	private Connection conn;
	private ResultSet rs;
	
	public int connect(String userID, String userPW, int option) {
		try{
			Socket soc = new Socket("localhost", 6000);
			//System.out.println(getTime() + " Accept to Server Success...");
			
			BufferedReader br = new BufferedReader(new InputStreamReader(soc.getInputStream()));
			PrintWriter pw = new PrintWriter(soc.getOutputStream());
			
			int result = -1;
			if(option == 0) {	// register
				RSA rsa = new RSA();
				
				pw.println("register");
				pw.println(userID);
				pw.println(userPW);
				pw.println(rsa.KeyToStr(rsa.getPublicKey()));
				pw.flush();

				if(br.readLine().equals("complete")) {
					loginDB(userID, userPW);
					createTable(userID);
					insertKey(userID, rsa.KeyToStr(rsa.getPublicKey()), rsa.KeyToStr(rsa.getPrivateKey()));
					result = 1;
				}
			}
			else if(option == 1) {	// login
				pw.println("login");
				pw.println(userID);
				pw.println(userPW);
				pw.flush();
				
				if(br.readLine().equals("complete")) {
					result = 1;
				}
			}
		
			soc.close();
			return result;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return -2;
	}
	
	public int isUser(String userID) {
		String sql = "SELECT userID FROM view_user WHERE userID = ?;";
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
	
	private void loginDB(String userID, String userPW) throws SQLException, ClassNotFoundException {
		String dbURL = "jdbc:mysql://localhost:3306/" + userID + "?";
		Class.forName("com.mysql.cj.jdbc.Driver");	
		conn = DriverManager.getConnection(dbURL, userID, userPW);
	}
	
	private int createTable(String userID) {
		String sql = "CREATE TABLE " + userID +"_BlockChain(f_name varchar(200) primary key, block_size int not null default 0);";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.executeUpdate();

			sql = "CREATE TABLE " + userID +"_RSAKEY(publicKey varchar(200) not null, privateKey varchar(200) not null);";
			pstmt = conn.prepareStatement(sql);
			pstmt.executeUpdate();
			
			return 1;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return -1;	// db error
	}
	
	private ArrayList<String> getLogList() {
		ArrayList<String> log = new ArrayList<>();
		
		String sql = "SELECT f_name FROM LOG;";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				log.add(rs.getString(1));
			}
			return log;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;	// db error
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
