package db;

import java.sql.PreparedStatement;

public class DecryptDAO extends DB{

	public DecryptDAO(){
		super();
	}
	
	public int insertDB(String name, String aes, String plaintext) {
		String sql = "INSERT INTO decrypt (name, aes, plaintext) VALUES (?, ?, ?)";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, name);
			pstmt.setString(2, aes);
			pstmt.setString(3, plaintext);
			pstmt.executeUpdate();
			return 1;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return -1;	// db error
	}
	
	public Decrypt isExist() {
		String sql = "SELECT * FROM decrypt WHERE isUsed = 0 ORDER BY name DESC;";
		try {
			PreparedStatement pstmt=conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				Decrypt data = new Decrypt();
				data.setName(rs.getString(1));
				data.setAes(rs.getString(2));
				data.setPlaintext(rs.getString(3));
				data.setIsUsed(rs.getInt(4));
				return data;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null; //데이터베이스 오류
	}
	
	/*public int update(String name) {
		String sql = "UPDATE decrypt SET isUsed = 1 WHERE name = ? && isUsed = 0;";
		try {
			PreparedStatement pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, name);
			pstmt.executeUpdate();
			return 1;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return -1; //데이터베이스 오류
	}*/
}