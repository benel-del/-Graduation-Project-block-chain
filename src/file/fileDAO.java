package file;
import db.DB;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.util.ArrayList;

public class fileDAO extends DB{
	public fileDAO(){
		super();
	}
	
	public int insert(String original, String name, String pw, String originalSize, String option) {
		String sql = "INSERT INTO file(originalName, systemName, password, originalFileSize, fileOption) VALUES(?, ?, ?, ?, ?);";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, original);
			pstmt.setString(2, name);
			pstmt.setString(3, pw);
			pstmt.setString(4, originalSize);
			pstmt.setString(5, option);
			pstmt.executeUpdate();
			return 1;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return -1;	// db error
	}
	
	public file getFileInfo(String name) {
		String sql = "SELECT * FROM file WHERE systemName = ? ORDER BY no DESC;";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, name);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				file f = new file();
				f.setOriginalName(rs.getString(2));
				f.setSystemName(rs.getString(3));
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
	
	public int update(String name, String resultSize, String option) {
		String sql = "UPDATE file SET resultFileSize = ? WHERE systemName = ? AND resultFileSize = NULL AND fileOption = ?;";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, resultSize);
			pstmt.setString(2, name);
			pstmt.setString(3, option);
			pstmt.executeUpdate();
			return 1;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return -1;	// db error
	}
	
	public ArrayList<String> read(String name) {
		ArrayList<String> Line = new ArrayList<>();
		String path = "C:\\JSP\\projects\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp0\\wtpwebapps\\block\\uploadFile\\" + name;
		try {
			File file = new File(path);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufReader = new BufferedReader(fileReader);
			String line = "";
			while((line = bufReader.readLine()) != null) {
				Line.add(line);
			}
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
