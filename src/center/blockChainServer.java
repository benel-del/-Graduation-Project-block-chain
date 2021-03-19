package center;

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
	Connection conn;
	Sockets(Socket client) {
		this.client = client;
	}
	
	public void run() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter pw = new PrintWriter(client.getOutputStream());
			
			String userID = br.readLine();
            String userPW = br.readLine();

            System.out.println(getTime() + " " + userID + ": USER IDENTIFY CHECK");
            int result = userCheck(userID, userPW);
            if(result == 1) {
            	pw.println("complete");
            }
            else if(result == -1) {
            	pw.println("no");
            	System.out.println("fail");
            }
            pw.flush();

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
	
	private int userCheck(String userID, String userPW) {
		String sql = "SELECT userPW FROM USER WHERE userID = ?;";
		try {
			String dbURL = "jdbc:mysql://localhost:3306/center?";
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(dbURL, "root", "Benel&Bende1");
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userID);
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()) {
				if(rs.getString(1).equals(userPW))
					return 1;
				else
					return -1;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return -2;	// db error
	}
	
	private String getTime() {
        String threadName = Thread.currentThread().getName();
        SimpleDateFormat f = new SimpleDateFormat("[hh:mm:ss]");
        return f.format(new Date()) + threadName;
    }
}

public class blockChainServer {
	static private Connection conn;
	static private Set<String> files = new HashSet<>();
	
	static public void main(String[] args) {
		try {
			String dbURL = "jdbc:mysql://localhost:3306/center?";
			String dbID = "root";
			String dbPassword = "Benel&Bende1";
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(dbURL, dbID, dbPassword);
		} catch (ClassNotFoundException | SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	
		
		ArrayList<String> file = getFilesList();
		for(int i = 0; i < file.size(); i++) {
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
						String[] str = file.get(i).split(" ");
						String[] str2 = str[3].split("/");
						if(files.contains(str2[6]))
							updateFile(file.get(i));	// update LOG last_access_time 
						else
							insertFile(file.get(i));	// insert LOG new log file
						readFile(file.get(i));	// read real file, update log_20210000
					}
				} catch (Exception e) {
					e.printStackTrace();
					exec.shutdown();
				}
			}
		}, 0, sleepSec, TimeUnit.SECONDS);
		
		ServerSocket server;
		try {
			server = new ServerSocket(6013);
			while(true) {
				Socket client = server.accept();
				Sockets sockets = new Sockets(client);
				sockets.start();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static private int insertFile(String file) {
		// file: 2O 27 18:42 /usr/local/lib/apache-tomcat-9.0.43/logs/localhost_access_log.2021-02-27.txt
		System.out.println(getTime() + "INSERT INTO LOG - " + file);
		String[] str = file.split(" ");
		String[] str2 = str[3].split("/");
		String sql = "INSERT INTO LOG(f_name, real_name, location, last_update_time) VALUES(?, ?, ?, ?);";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			String name = tableNaming(str2[6]);
			pstmt.setString(1, name);
			pstmt.setString(2, str2[6]);
			String tmp = "";
			for(int i = 0; i < 6; i++)
				tmp += str2[i] + "/";
			pstmt.setString(3, tmp);
			pstmt.setString(4, str[0] + " " + str[1] + " " + str[2]);
			pstmt.executeUpdate();
			
			sql = "CREATE TABLE " + name + " (no int auto_increment primary key, content text not null);";
			pstmt = conn.prepareStatement(sql);
			pstmt.executeUpdate();
			
			sql = "GRANT select ON " + name + " TO user1@localhost";
			pstmt = conn.prepareStatement(sql);
			pstmt.executeUpdate();
			sql = "GRANT select ON " + name + " TO user2@localhost";
			pstmt = conn.prepareStatement(sql);
			pstmt.executeUpdate();
			
			files.add(str2[6]);
			return 1;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return -1;	// db error
	}
	
	static private String tableNaming(String file) {
		// file: localhost_access_log.2021-02-27.txt
		String[] str = file.split("[.]");
		String[] str2 = str[1].split("-");	// 2021-02-27
		
		return "log_" + str2[0] + str2[1] + str2[2];
	}
	
	static private int updateFile(String file) {
		// file: 2O 27 18:42 /usr/local/lib/apache-tomcat-9.0.43/logs/localhost_access_log.2021-02-27.txt
		System.out.println(getTime() + "UPDATE LOG(last_update_time) - " + file);
		String[] str = file.split(" ");
		String[] str2 = str[3].split("/");
		String sql = "UPDATE LOG SET last_update_time=? WHERE real_name=?;";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, str[0] + " " + str[1] + " " + str[2]);
			pstmt.setString(2, str2[6]);
			pstmt.executeUpdate();
			return 1;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return -1;	// db error
	}

	static private void readFile(String file) {
		// file: 2O 27 18:42 /usr/local/lib/apache-tomcat-9.0.43/logs/localhost_access_log.2021-02-27.txt
		String[] str = file.split(" ");
		ArrayList<String> line = readLogFile(str[3]);
		String[] str2 = str[3].split("/");
		String name = tableNaming(str2[6]);
		if(line.size() == 0) {
			files.remove(str2[6]);
			delete(name);
			return ;
		}
		int start = getLastLine(name);
		if(start > -2) {
			String tmp = "";
			for(int i = start + 1; i < line.size(); i++) {
				tmp += line.get(i) + "\n";
			}
			insertLog(name, tmp);
			setLastLine(name, line.size());
		}
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
		System.out.println(getTime() + "INSERT INTO log_2021... - " + file);
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
		String sql = "SELECT last_read_line FROM LOG WHERE f_name = ?;";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, file);
			ResultSet rs = pstmt.executeQuery();
			if(rs.next())
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
		//System.out.println("[readLogFile] readFile path: "  + path);
		try {
			File file = new File(filename);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufReader = new BufferedReader(fileReader);
			String line = "";
			while((line = bufReader.readLine()) != null) {
				if(line.contains("/block/index.jsp") || line.contains("/block/fileUpload.jsp") || line.contains("/block/fileDownload.jsp"))
					Line.add(line);
			}
			bufReader.close();
		}catch(FileNotFoundException e) {
			Line.add("Error:: file not found - " + filename);
		}catch(IOException e) {
			System.out.println(e);
		}
		return Line;
	}
	
	static private String getTime() {
        SimpleDateFormat f = new SimpleDateFormat("[hh:mm:ss]");
        return f.format(new Date());
    }
	
}
