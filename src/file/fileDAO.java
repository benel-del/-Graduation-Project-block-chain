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
	
	public int insert(String original, String name, String pw, String size, String option) {
		String sql = "INSERT INTO file VALUES(?, ?, ?, ?, ?);";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, original);
			pstmt.setString(2, name);
			pstmt.setString(3, pw);
			pstmt.setString(4, size);
			pstmt.setString(5, option);
			pstmt.executeUpdate();
			return 1;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return -1;	// db error
	}
	
	public file getFileInfo(String name) {
		String sql = "SELECT * FROM file WHERE systemName = ?;";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, name);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				file f = new file();
				f.setOriginalName(rs.getString(1));
				f.setSystemName(rs.getString(2));
				f.setPassword(rs.getString(3));
				f.setFileSize(rs.getString(4));
				f.setFileOption(rs.getString(5));
				return f;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;	// db error
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
	
	public ArrayList<String> readResult(String name) {
		ArrayList<String> Line = new ArrayList<>();
		String path = "C:\\JSP\\projects\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp0\\wtpwebapps\\block\\downloadFile\\" + name;
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
		if(number < 1024)
			return number + "bytes";
		else if(number >= 1024 && number < 1048576)
			return (number/1024) + "KB";
		else
			return (number/1048576) + "MB";
	}
}
