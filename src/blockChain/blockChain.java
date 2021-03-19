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
	static private Set<String> files = new HashSet<>();
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

		getKey();
	}
	
	public void run() {	// called when registered
		int sleepSec = 60;
		final ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
		exec.scheduleAtFixedRate(new Runnable(){
			public void run(){
				try {
					getCenterFileList();	// center filename list
					getLocalFileList(); 	// local filename
						
					for(String local : localfile) {
						fetchChain(local);	// local db content >> java code chain
					}
					
					for(String center : centerfile) {
						if(!localfile.contains(center)) {
							createTable(center);
						}
						fecthContent(center);
						uploadChain(center);
					}
				} catch (Exception e) {
					e.printStackTrace();
					exec.shutdown();
				}
			}
		}, 0, sleepSec, TimeUnit.SECONDS);
	}
	
	private void createTable(String file) throws SQLException {	// 2) center new file >> insert local_blockChain table
		String sql = "INSERT INTO " + userID +"_BlockChain(f_name)  VALUES(?);";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, file);
		pstmt.executeUpdate();
		
		sql = "CREATE TABLE " + userID +"_" + file +"(no int not null default 0, sign varchar(300) not null, content text not null, state varchar(10));";
		pstmt = conn.prepareStatement(sql);
		pstmt.executeUpdate();
		
		String other = getOtherUser();
		sql = "GRANT select ON " + userID + "_" + file + " TO " + other + "@localhost";
		pstmt = conn.prepareStatement(sql);
		pstmt.executeUpdate();
	}
	
	private void fetchChain(String file) {	// 1) get block chain in local db
		int index = getIndex(file);
		if(index == -1) {
			index = files.size();
			files.add(file);
			chain.add(new ArrayList<block>());
		}
		
		String sql = "SELECT sign, content, state FROM " + userID + "_" + file + " WHERE no >= ?;";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, chain.get(index).size());
			rs = pstmt.executeQuery();
			while(rs.next()) {
				chain.get(index).add(new block(rs.getString(1), rs.getString(2), rs.getString(3)));
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

	private String getOtherUser(){	// user number limitied 2
		String  user = "";
		String sql = "SELECT userID FROM center.VIEW_USER WHERE userID <> ?;";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userID);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				user = rs.getString(1);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return user;
	}
	
	private void fecthContent(String file) {	// 3) center new content >> block chain
		int index = getIndex(file);
		if(index == -1) {
			index = files.size();
			files.add(file);
			chain.add(new ArrayList<block>());
			chain.get(index).add(new block("START", file+"\n"));	// genesis block
		}
		
		String sql = "SELECT content FROM center." + file + " WHERE no >= ?;";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, chain.get(index).size());
			rs = pstmt.executeQuery();
			while(rs.next()) {
				chain.get(index).add(new block(sign(index), rs.getString(1)));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void uploadChain(String file) {	// 4) block chain >> local db
		int index = getIndex(file);
		ArrayList<block> b = chain.get(index);
		
		int start = 0;
		String sql = "SELECT MAX(no) FROM " + userID + "_" + file + ";";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				start = rs.getInt(1);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		String other = getOtherUser();
		ArrayList<block> otherB = new ArrayList<>();
		sql = "SELECT sign, content, state FROM " + other + "." + other + "_" + file + " WHERE no >= ?;";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, start);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				otherB.add(new block(rs.getString(1), rs.getString(2), rs.getString(3)));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		for(int i = start; i < b.size(); i++) {
			sql = "INSERT INTO " + userID +"_" + file + "(no, sign, content, state) VALUES(?, ?, ?, ?);";
			try {
				PreparedStatement pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, i);
				pstmt.setString(2, b.get(i).getSign());
				pstmt.setString(3, b.get(i).getContent());
				String state = "";
				if(i != 0) {	// verification
					int dec = Integer.parseInt(rsa.decrypt(b.get(i).getSign(), rsa.getPublicKey()));
					int hash = b.get(i-1).hashCode();
					if(dec != hash){ // error
						state = "verification error";
					}
					else {	// other block chain compare
						int otherStart = i - start;
						if(!otherB.get(otherStart).getState().equals("verification error") && !otherB.get(otherStart).getContent().equals(b.get(i).getContent()))
							state = "Different from other blockchains";
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

	private String sign(int index) throws Exception {
		int size = chain.get(index).size();
		block b = chain.get(index).get(size-1);
		return rsa.encrypt(b.hashCode() + "", rsa.getPrivateKey());
	}
	
	private int getIndex(String name) {
		for(int i = 0; i < files.size(); i++) {
			if(files.iterator().toString().equals(name)) {
				return i;
			}
		}
		return -1;
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
	
	public Set<String> getList(){
		return files;
	}
	public ArrayList<ArrayList<block>> getChain(){
		return chain;
	}
	
}
