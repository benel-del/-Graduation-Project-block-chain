package db;

import java.sql.PreparedStatement;

public class DecryptDAO extends DB{

	public DecryptDAO(){
		super();
	}
	
	public int insertDB(String name, String aes, String rsa, String plaintext) {
		String sql = "INSERT INTO decrypt (name, aes, rsa, plaintext) VALUES (?, ?, ?, ?)";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, name);
			pstmt.setString(2, aes);
			pstmt.setString(3, rsa);
			pstmt.setString(4, plaintext);
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
				data.setRsa(rs.getString(3));
				data.setPlaintext(rs.getString(4));
				data.setIsUsed(rs.getInt(5));
				return data;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null; //데이터베이스 오류
	}
}