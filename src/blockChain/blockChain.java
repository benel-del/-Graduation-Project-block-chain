package blockChain;
// create, upload block chain
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import security.RSA;

public class blockChain extends Thread {
	private Connection conn;
	private ResultSet rs;
	private String userID;
	private static RSA rsa = new RSA();
	
	static private Set<String> localfile = new HashSet<>();
	static private Set<String> centerfile = new HashSet<>();
	static private ArrayList<ArrayList<block>> chain = new ArrayList<ArrayList<block>>();
	
	public blockChain(String userID, String userPW){
		this.userID = userID;
		
		String dbURL = "jdbc:mysql://localhost:3306/" + userID + "?";
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(dbURL, userID, userPW);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		getUserList();
		getKey();
	}
	
	public void run() {	// called when logined
		
		ArrayList<String> chainList = getUserChainList();
		for(int i = 0; i < chainList.size(); i++) {
			fetch(chainList.get(i));
		}
		
		int sleepSec = 60;
		final ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
		exec.scheduleAtFixedRate(new Runnable(){
			public void run(){
				try {
					getCenterFileList();	// center filename list
					getLocalFileList(); 	// local filename
					
					for(String local : localfile) {
						if(!centerfile.contains(local)) {
							createTable(local);
						}
						
					}
					
					for(int i = 0; i < files.size(); i++) {
						updateChain(files.get(i));
					}
				} catch (Exception e) {
					e.printStackTrace();
					exec.shutdown();
				}
			}
		}, 0, sleepSec, TimeUnit.SECONDS);
	}
	
	private void createTable(String file) throws SQLException {
		String sql = "INSERT INTO " + userID +"_BlockChain(f_name)  VALUES(?);";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, file);
		pstmt.executeUpdate();
		
		sql = "CREATE TABLE " + userID +"_" + file +"(no int not null default 0, sign varchar(300) not null, content text not null, state varchar(10));";
		pstmt = conn.prepareStatement(sql);
		pstmt.executeUpdate();
		
		sql = "GRANT select ON VIEW_USER TO " + userID + "@localhost";
		pstmt = conn.prepareStatement(sql);
		pstmt.executeUpdate();
	}
	
	private void fetch(String file) {	// get block chain in user db
		int index = files.size();
		files.add(file);
		chain.add(new ArrayList<block>());
		String sql = "SELECT sign, content FROM " + userID + "_" + file + ";";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				chain.get(index).add(new block(rs.getString(2), rs.getString(3)));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void getCenterFileList() {
		String sql = "SELECT f_name FROM center.VIEW_LOG;";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				centerfile.add(rs.getString(1));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void getLocalFileList() {
		String sql = "SELECT f_name FROM " + userID + "_BlockChain;";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				localfile.add(rs.getString(1));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void login() throws Exception {
		
		ArrayList<String> logs = getLogList();
		for(String log : logs) {
			sql = "INSERT INTO " + userID +"_BlockChain(f_name)  VALUES(?);";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, log);
			pstmt.executeUpdate();
			
			sql = "CREATE TABLE " + userID +"_" + log +"(no int not null default 0, sign varchar(300) not null, content text not null, state varchar(10));";
			pstmt = conn.prepareStatement(sql);
			pstmt.executeUpdate();
			
			sql = "GRANT select ON VIEW_USER TO " + userID + "@localhost";
			pstmt = conn.prepareStatement(sql);
			pstmt.executeUpdate();
		}
		
		ArrayList<String> file = getFileList();
		for(int i = 0; i < file.size(); i++) {
			createChain(file.get(i));
			uploadChain(file.get(i), 0);
		}
	}
	
	private void createChain(String file) {	// init
		int index = files.size();
		files.add(file);
		chain.add(new ArrayList<block>());
		chain.get(index).add(new block("START", file+"\n"));
		String sql = "SELECT content FROM center." + file + ";";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				chain.get(index).add(new block(sign(index), rs.getString(1)));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void uploadChain(String file, int start) {	// db upload
		int index = getIndex(file);
		if(index == -1) {
			index = files.size();
			files.add(file);
			try {
				String sql = "INSERT INTO " + userID +"_BlockChain(f_name) VALUES(?);";
				PreparedStatement pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, file);
				pstmt.executeUpdate();
				
				sql = "CREATE TABLE " + userID +"_" + file +"(no int not null default 0, sign varchar(300) not null, content text not null, state varchar(10));";
				pstmt = conn.prepareStatement(sql);
				pstmt.executeUpdate();
				
				sql = "GRANT select ON center.VIEW_USER TO " + userID + "@localhost";
				pstmt = conn.prepareStatement(sql);
				pstmt.executeUpdate();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		ArrayList<block> ch = chain.get(index);

		for(int i = start; i < ch.size(); i++) {
			String sql = "INSERT INTO " + userID +"_" + file + "(no, sign, content, state) VALUES(?, ?, ?, ?);";
			try {
				PreparedStatement pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, i);
				pstmt.setString(2, ch.get(i).getSign());
				pstmt.setString(3, ch.get(i).getContent());
				String state = "";
				if(i != 0) {	// verification
					int dec = Integer.parseInt(rsa.decrypt(ch.get(i).getSign(), rsa.getPublicKey()));
					int hash = ch.get(i-1).hashCode();
					if(dec != hash){ // error
						state = "verification error";
					}
				}
				pstmt.setString(4, state);
				pstmt.executeUpdate();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return;
	}
	
	
	

	
	private void getKey() {
		String sql = "SELECT publicKey, privateKey FROM " + userID + "_RSAKEY;";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				rsa.setPublicKey(rs.getString(1));
				rsa.setPrivateKey(rs.getString(2));
			}
			return;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private ArrayList<String> getUserChainList() {
		ArrayList<String> log = new ArrayList<>();
		String sql = "SELECT f_name FROM + " + userID + "_BlockChain;";
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
	
	private void updateChain(String file) throws Exception {
		int index = getIndex(file);
		ArrayList<String> server = getLogContent(file);
		int start = chain.get(index).size();
		for(int i = start; i < server.size(); i++) {
			chain.get(index).add(new block(sign(index), server.get(i)));	// create block
			System.out.println("[updateChain] " + files.get(index) + " - create new block[" + (chain.get(index).size()-1) + "]");
		}
		
		uploadChain(file, start);
	}

	private ArrayList<String> getLogContent(String file){
		ArrayList<String> log = new ArrayList<>();
		String sql = "SELECT content FROM center." + file + ";";
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
		return log;
	}
	
	private String sign(int index) throws Exception {
		int size = chain.get(index).size();
		block b = chain.get(index).get(size-1);
		return rsa.encrypt(b.hashCode() + "", rsa.getPrivateKey());
	}
	
	private int getIndex(String name) {
		for(int i = 0; i < files.size(); i++) {
			if(files.get(i).equals(name)) {
				return i;
			}
		}
		return -1;
	}
	
	public ArrayList<String> getFile(){
		return files;
	}
	public ArrayList<ArrayList<block>> getChain(){
		return chain;
	}
	
}
