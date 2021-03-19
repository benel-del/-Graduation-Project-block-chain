package blockChain;
// create, upload block chain
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
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

		getKey();
		fetchChain();	// local db content >> java code chain
	}
	
	public void run() {	// called when logined
		int sleepSec = 60;
		final ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
		exec.scheduleAtFixedRate(new Runnable(){
			public void run(){
				try {
					getCenterFileList();	// center filename list

					for(String center : centerfile) {
						if(!localfile.contains(center)) {
							createTable(center);
						}
						fecthContent(center);
					}
				} catch (Exception e) {
					e.printStackTrace();
					exec.shutdown();
				}
			}
		}, 0, sleepSec, TimeUnit.SECONDS);
	}
	
	private void createTable(String file) throws SQLException {	// 2) center new file >> insert local_blockChain table
		String sql = "INSERT INTO BlockChain(f_name) VALUES(?);";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, file);
		pstmt.executeUpdate();
		
		sql = "CREATE TABLE " + file +"(no int not null default 0, sign varchar(300) not null, content text not null, state varchar(20));";
		pstmt = conn.prepareStatement(sql);
		pstmt.executeUpdate();
		
		String other = getOtherUser();
		sql = "GRANT select ON " + file + " TO " + other + "@localhost";
		pstmt = conn.prepareStatement(sql);
		pstmt.executeUpdate();
		
		int index = localfile.size();
		localfile.add(file);
		chain.add(new ArrayList<block>());
		chain.get(index).add(new block("START", file+"\n"));	// genesis block
	}
	
	private void fetchChain() {	// 1) get block chain in local db
		String sql = "SELECT f_name FROM BlockChain;";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				chain.add(new ArrayList<block>());
				localfile.add(rs.getString(1));
				sql = "SELECT sign, content, state FROM " + rs.getString(1) + ";";
				pstmt = conn.prepareStatement(sql);
				ResultSet rsrs = pstmt.executeQuery();
				while(rsrs.next()) {
					chain.get(chain.size()-1).add(new block(rsrs.getString(1), rsrs.getString(2), rsrs.getString(3)));
				}
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
		String sql = "SELECT content FROM center." + file + " WHERE no >= ?;";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, chain.get(index).size());
			rs = pstmt.executeQuery();
			int pre = chain.get(index).size();
			while(rs.next()) {
				chain.get(index).add(new block(sign(index), rs.getString(1)));
			}
			
			if(chain.get(index).size() != pre) {	// uploadChain - 4) block chain >> local db
				ArrayList<block> b = chain.get(index);
				ArrayList<block> other = getOtherChain(file, pre);
				for(int i = pre; i < b.size(); i++) {
					sql = "INSERT INTO " + file + "(no, sign, content, state) VALUES(?, ?, ?, ?);";
					try {
						pstmt = conn.prepareStatement(sql);
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
								int otherStart = i - pre;
								if(otherStart < other.size())
									if(!other.get(otherStart).getState().equals("verification error") && !other.get(otherStart).getContent().equals(b.get(i).getContent()))
										state = "Different from other blockchains";
							}
						}
						pstmt.setString(4, state);
						pstmt.executeUpdate();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
			else return;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	

	private String sign(int index) throws Exception {
		int size = chain.get(index).size();
		block b = chain.get(index).get(size-1);
		return rsa.encrypt(b.hashCode() + "", rsa.getPrivateKey());
	}
	private ArrayList<block> getOtherChain(String file, int start){
		String other = getOtherUser();
		ArrayList<block> b = new ArrayList<>();
		String sql = "SELECT sign, content, state FROM " + other + "." + file + " WHERE no >= ?;";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, start);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				b.add(new block(rs.getString(1), rs.getString(2), rs.getString(3)));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return b;
	}
	
	private int getIndex(String name) {
		Iterator<String> items = localfile.iterator();
		for(int i = 0; items.hasNext(); i++) {
			if(items.next().equals(name)) {
				return i;
			}
		}
		return -1;
	}
	
	private void getKey() {
		String sql = "SELECT publicKey, privateKey FROM RSA_KEY;";
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
		if(localfile.size() != 0)
			return localfile;
		return null;
	}
	public ArrayList<ArrayList<block>> getChain(){
		if(chain.size() != 0)
			return chain;
		return null;
	}
	
}
