
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

class Sockets extends Thread {
	Socket client;
	Sockets(Socket client){
		this.client = client;
	}
	
	public void run() {
		try {
			//System.out.println(getTime() + " Client has accepted...");
			
			BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter pw = new PrintWriter(client.getOutputStream());
			
			String input = br.readLine();
			if(input.equals("register")) {
				String userID = br.readLine();
	            String userPW = br.readLine();
	            String userKey = br.readLine();
	            
	            if(blockChainServer.insertUser(userID, userPW, userKey) == 1) {
	            	// register, grant
	            	addUser(userID, userPW);
	            	
	            	pw.println("complete");
	            	System.out.println(getTime() + "user register complete");
	            }
	            else {
	            	pw.println("db error");
	            }
			}
			else if(input.equals("login")) {
				String userID = br.readLine();
	            String userPW = br.readLine();
	            
	            int result = blockChainServer.isUser(userID, userPW);
	            if(result == 1) {
	            	pw.println("complete");
	            	System.out.println(getTime() + "user indentify");
	            }
	            else if(result == -1) {
	            	pw.println("no");
	            	System.out.println(getTime() + "not registered user");
	            }
	            
			}
            
		} catch (Exception e) {
            e.printStackTrace();
        } finally {
        	try {
				client.close();
			} catch (IOException e) {
				System.out.println(getTime() + "Client close error");
			}
        }
	}
	
	private void addUser(String userID, String userPW) throws ClassNotFoundException, SQLException {
		String dbURL = "jdbc:mysql://localhost:3306/mysql?";
		String dbID = "root";
		String dbPassword = "Benel&Bende1";
		Class.forName("com.mysql.cj.jdbc.Driver");	
		Connection conn = DriverManager.getConnection(dbURL, dbID, dbPassword);
		
		String sql = "CREATE USER ?@localhost identified by ?";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, userID);
		pstmt.setString(2, userPW);
		pstmt.executeUpdate();
		
		sql = "CREATE DATABASE " + userID;
		pstmt = conn.prepareStatement(sql);
		pstmt.executeUpdate();
		
		// grant
		sql = "GRANT select ON center.VIEW_LOG TO " + userID + "@localhost";
		pstmt = conn.prepareStatement(sql);
		pstmt.executeUpdate();
		
		sql = "GRANT select ON center.VIEW_USER TO " + userID + "@localhost";
		pstmt = conn.prepareStatement(sql);
		pstmt.executeUpdate();
	}
	
	public String getTime() {
        String threadName = Thread.currentThread().getName();
        SimpleDateFormat f = new SimpleDateFormat("[hh:mm:ss]");
        return f.format(new Date()) + threadName;
    }
}

public class blockChainServer {
	static private Connection conn;
	static private Set<String> files = new HashSet<>();
	
	static public void main(String[] args) throws Exception {
		String dbURL = "jdbc:mysql://localhost:3306/center?";
		String dbID = "root";
		String dbPassword = "Benel&Bende1";
		Class.forName("com.mysql.cj.jdbc.Driver");	
		conn = DriverManager.getConnection(dbURL, dbID, dbPassword);

		ArrayList<String> file = getFilesList();
		for(int i = 0; i < file.size(); i++) {
			files.add(file.get(i));
			insertFile(file.get(i));
			readFile(file.get(i));
		}

		int sleepSec = 60;
		final ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
		exec.scheduleAtFixedRate(new Runnable(){
			public void run(){
				try {
					ArrayList<String> file = getUpdateList();
					for(int i = 0; i < file.size(); i++) {
						if(files.contains(file.get(i)))
							updateFile(file.get(i));
						else
							insertFile(file.get(i));
						readFile(file.get(i));
					}
				} catch (Exception e) {
					e.printStackTrace();
					exec.shutdown();
				}
			}
		}, 0, sleepSec, TimeUnit.SECONDS);
		
		ServerSocket server = new ServerSocket(6000);
		while(true) {
			Socket client = server.accept();
			Sockets sockets = new Sockets(client);
			sockets.start();
		}

	}
	
	static int insertUser(String userID, String userPW, String userKey) {
		String sql = "INSERT INTO USER(userID, userPW, publicKey) VALUES(?, ?, ?);";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userID);
			pstmt.setString(2, userPW);
			pstmt.setString(3, userKey);
			pstmt.executeUpdate();
			return 1;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return -1;	// db error
	}
	
	static int isUser(String userID, String userPW) {
		String sql = "SELECT userID FROM USER WHERE userID = ? AND userPW = ?;";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userID);
			pstmt.setString(2, userPW);
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()) {
				return 1;
			}
			return -1;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return -2;	// db error
	}

	static private int insertFile(String file) {
		// file: 2¿ù 27 18:42 /usr/local/lib/apache-tomcat-9.0.43/logs/localhost_access_log.2021-02-27.txt
		String[] str = file.split(" ");
		String[] str2 = str[3].split("/");
		String sql = "INSERT INTO LOG(f_name, location, last_update_time) VALUES(?, ?, ?);";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, str2[6]);
			String tmp = "";
			for(int i = 0; i < 6; i++)
				tmp += str2[i] + "/";
			pstmt.setString(2, tmp);
			pstmt.setString(3, str[0].substring(0, str[0].length()-2) + "-" + str[1] + " " + str[2]);
			pstmt.executeUpdate();
			
			sql = "CREATE TABLE " + str2[6] + " (no int auto_increment primary key, content text not null);";
			pstmt = conn.prepareStatement(sql);
			pstmt.executeUpdate();
			return 1;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return -1;	// db error
	}
	
	static private int updateFile(String file) {
		// file: 2¿ù 27 18:42 /usr/local/lib/apache-tomcat-9.0.43/logs/localhost_access_log.2021-02-27.txt
		String[] str = file.split(" ");
		String[] str2 = str[3].split("/");
		String sql = "UPDATE LOG SET last_update_time=? WHERE f_name=?;";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, str[0].substring(0, str[0].length()-2) + "-" + str[1] + " " + str[2]);
			pstmt.setString(2, str2[6]);
			pstmt.executeUpdate();
			return 1;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return -1;	// db error
	}

	static private void readFile(String file) {
		// file: 2¿ù 27 18:42 /usr/local/lib/apache-tomcat-9.0.43/logs/localhost_access_log.2021-02-27.txt
		String[] str = file.split(" ");
		ArrayList<String> line = readLogFile(str[3]);
		if(line == null) {
			delete(str[3]);
			return ;
		}
		String[] str2 = str[3].split("/");
		int start = getLastLine(str2[6]);
		String tmp = "";
		for(int i = start + 1; i < line.size(); i++) {
			tmp += line.get(i) + "\n";
		}
		insertLog(str2[6], tmp);
		setLastLine(str2[6], line.size()-1);
	}
	
	static private int delete(String file) {
		String sql = "DELETE FROM LOG WHERE f_name=?;";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, file);
			pstmt.executeUpdate();
			
			sql = "DROP TABLE " + file + ";";
			pstmt = conn.prepareStatement(sql);
			pstmt.executeUpdate();
			return 1;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return -1;	// db error
	}
	
	static private int insertLog(String file, String content) {
		String sql = "INSERT INTO " + file + "(content) VALUES(?);";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, content);
			pstmt.executeUpdate();
			return 1;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return -1;	// db error
	}
	
	static private int getLastLine(String file) {
		String sql = "SELECT last_read_line FROM LOG WEHRE f_name = ?;";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, file);
			ResultSet rs = pstmt.executeQuery();
			return rs.getInt(1);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return -2;	// db error
	}
	
	static private int setLastLine(String file, int index) {
		String sql = "UPDATE LOG SET last_read_line = " + index + " WHERE f_name = ?;";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, file);
			pstmt.executeUpdate();
			return 1;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return -1;	// db error
	} 
	
	static private ArrayList<String> getFilesList(){
		ArrayList<String> Line = new ArrayList<>();
		String path = "/usr/local/lib/apache-tomcat-9.0.43/webapps/block/files.txt";
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
	
	static private ArrayList<String> getUpdateList(){
		ArrayList<String> Line = new ArrayList<>();
		String path = "/usr/local/lib/apache-tomcat-9.0.43/webapps/block/update.txt";
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
	
	static private ArrayList<String> readLogFile(String filename) {
		ArrayList<String> Line = new ArrayList<>();
		String path = filename;
		//System.out.println("[readLogFile] readFile path: "  + path);
		try {
			File file = new File(path);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufReader = new BufferedReader(fileReader);
			String line = "";
			while((line = bufReader.readLine()) != null) {
				if(line.contains("/block/index.jsp") || line.contains("/block/fileUpload.jsp") || line.contains("/block/fileDownload.jsp"))
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
	
}
