package db;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.PreparedStatement;
import security.RSA;

public class logDAO extends DB{
	public logDAO(){
		super();
	}

	public int register(String name, String key) {
		String sql = "INSERT INTO LOG(name, publicKey) VALUES(?, ?);";
		int next = 1;
		if(name.equals("client")) {
			next = next("log", name);
			name = name+next;
		}
		try {
			PreparedStatement pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, name);
			pstmt.setString(2, key);
			pstmt.executeUpdate();
			return next;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return -1;	//db error
	}
	
	public int register_private(String name, String key) {
		String sql = "INSERT INTO LOG_private(name, privateKey) VALUES(?, ?);";
		try {
			PreparedStatement pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, name);
			pstmt.setString(2, key);
			pstmt.executeUpdate();
			return 1;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return -1;	//db error
	}
	
	public PublicKey getKey(String name) {
		String sql = "SELECT publicKey FROM LOG WHERE name = ?;";
		try {
			PreparedStatement pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, name);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				PublicKey publicKey = RSA.getPublicKey(rs.getString(1));
				return publicKey;
			}
			return null;	// name is not exist
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;	//db error
	}
	
	public PrivateKey getPrivateKey(String name) {
		String sql = "SELECT privateKey FROM LOG_private WHERE name = ?;";
		try {
			PreparedStatement pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, name);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				PrivateKey privateKey = RSA.getPrivateKey(rs.getString(1));
				return privateKey;
			}
			return null;	// name is not exist
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;	//db error
	}
	
	public int delete(String name) {
		String sql = "delete from log WHERE name = ?;";
		try {
			PreparedStatement pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, name);
			pstmt.executeUpdate();
			if(name.contains("server")) {
				sql = "delete from log_private WHERE name = ?;";
				try {
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, name);
					pstmt.executeUpdate();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			return 1;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return -1; //데이터베이스 오류
	}
}
