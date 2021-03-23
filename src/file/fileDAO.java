package file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class FileDAO {
	protected Connection conn;
	protected ResultSet rs;
	
	public FileDAO(){
		try {
			String dbURL = "jdbc:mysql://localhost:3306/file?";
			String dbID = "root";
			String dbPassword = "Benel&Bende1";
			Class.forName("com.mysql.cj.jdbc.Driver");	
			conn = DriverManager.getConnection(dbURL, dbID, dbPassword);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public int insert(String name, String resultName, String pw, String originalSize, String resultSize, String option) {
		String sql = "INSERT INTO file(originalName, resultName, password, originalFileSize, resultFileSize, fileOption) VALUES(?, ?, ?, ?, ?, ?);";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, name);
			pstmt.setString(2, resultName);
			pstmt.setString(3, pw);
			pstmt.setString(4, originalSize);
			pstmt.setString(5, resultSize);
			pstmt.setString(6, option);
			pstmt.executeUpdate();
			return 1;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return -1;	// db error
	}
	
	public FileInfo getFileInfo(String name) {
		String sql = "SELECT * FROM file WHERE originalName = ? ORDER BY no DESC;";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, name);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				FileInfo f = new FileInfo();
				f.setOriginalName(rs.getString(2));
				f.setResultName(rs.getString(3));
				f.setPassword(rs.getString(4));
				f.setOriginalFileSize(rs.getString(5));
				f.setResultFileSize(rs.getString(6));
				f.setFileOption(rs.getString(7));
				return f;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;	// db error
	}
	
	public ArrayList<String> read(String name) {
		ArrayList<String> Line = new ArrayList<>();
		String path = "/usr/local/lib/apache-tomcat-9.0.43/webapps/block/uploadFile/" + name;
		try {
			File file = new File(path);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufReader = new BufferedReader(fileReader);
			String line = "";
			while((line = bufReader.readLine()) != null) {
				Line.add(line);
			}
			bufReader.close();
		}catch(FileNotFoundException e) {
			Line.add("Error:: file not found - " + path);
		}catch(IOException e) {
			System.out.println(e);
		}
		return Line;
	}

	public String fileSize(long number) {
		if(number < (float)1024)
			return number + "bytes";
		else if(number >= 1024 && number < 1048576)
			return Math.round((number/(float)1024)*10)/(float)10 + "KB";
		else
			return Math.round((number/(float)1048576)*10)/(float)10 + "MB";
	}
	
	public int delete() {
		String sql = "DELETE FROM file;";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.executeUpdate();
			return 1;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return -1;	// db error
	}
}
