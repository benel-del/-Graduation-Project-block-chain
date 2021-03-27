package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;

import blockChain.block;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.security.Key;
import java.security.KeyFactory;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

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
			String dbURL = "jdbc:mysql://localhost:3306/server?";
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

class Sockets2 extends Thread {
	Socket client;
	Connection conn;
	Sockets2(Socket client) {
		this.client = client;
	}
	
	public void run() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter pw = new PrintWriter(client.getOutputStream());
			while(true) {
				String input = br.readLine();	// (int)no
				String result = Server.verify(Integer.parseInt(input));	
				if(result != null)	// 검증 결과 확인
					pw.println(result);
				else
					pw.println("no");
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

	private String getTime() {
        String threadName = Thread.currentThread().getName();
        SimpleDateFormat f = new SimpleDateFormat("[hh:mm:ss]");
        return f.format(new Date()) + threadName;
    }
}

public class Server {
	static protected Connection conn;
	static protected ResultSet rs;
	static private String[] key;
	static private ArrayList<String> files = new ArrayList<>();
	static private ArrayList<ArrayList<block>> chain = new ArrayList<ArrayList<block>>();
	static int candNo = 0;
	
	static public void main(String[] args) {
		init();
		
		try {
			String dbURL = "jdbc:mysql://localhost:3306/server?";
			String dbID = "root";
			String dbPassword = "Benel&Bende1";
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(dbURL, dbID, dbPassword);
		} catch (ClassNotFoundException | SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	
		
		// local block chain file read
		String path = "/usr/local/lib/apache-tomcat-9.0.43/webapps/blockchain/serverFile";
		String[] fileNameOfPath = new File(path).list();
		System.out.println("********** local file uploading **********");
		for(int i = 0; fileNameOfPath!=null && i < fileNameOfPath.length; i++){
			insertFile2(fileNameOfPath[i]);
		}
		
		System.out.println("********** update **********");
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
						readFile(file.get(i));	// read real file, update db log_20210000
					}
				} catch (Exception e) {
					e.printStackTrace();
					exec.shutdown();
				}
			}
		}, 0, sleepSec, TimeUnit.SECONDS);
		
		ServerSocket server, server2;
		try {
			server = new ServerSocket(6009);
			server2 = new ServerSocket(6010);
			while(true) {
				Socket client = server.accept();
				Sockets sockets = new Sockets(client);
				sockets.start();
				
				Socket client2 = server2.accept();
				Sockets2 sockets2 = new Sockets2(client2);
				sockets2.start();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static private int insertFile(String file) {
		// file: 2O 27 18:42 /usr/local/lib/apache-tomcat-9.0.43/logs/localhost_access_log.2021-02-27.txt
		System.out.println(getTime() + "CREATE NEW TABLE - " + file);
		String[] str = file.split(" ");
		String[] str2 = str[3].split("/");
		String name = tableNaming(str2[6]);
		
		int index = files.size();
		files.add(str2[6]);
		chain.add(new ArrayList<block>());
		chain.get(index).add(new block("START", file+"\n"));
		String sql = "INSERT INTO LOG(f_name, real_name, location, last_update_time) VALUES(?, ?, ?, ?);";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			
			pstmt.setString(1, name);
			pstmt.setString(2, str2[6]);
			String tmp = "";
			for(int i = 0; i < 6; i++)
				tmp += str2[i] + "/";
			pstmt.setString(3, tmp);
			pstmt.setString(4, str[0] + " " + str[1] + " " + str[2]);
			pstmt.executeUpdate();
			
			sql = "CREATE TABLE " + name + " (content text not null, no int PRIMARY KEY auto_increment, sign varchar(350) not null);";
			pstmt = conn.prepareStatement(sql);
			pstmt.executeUpdate();
			
			sql = "GRANT select ON " + name + " TO user1@localhost";
			pstmt = conn.prepareStatement(sql);
			pstmt.executeUpdate();
			sql = "GRANT select ON " + name + " TO user2@localhost";
			pstmt = conn.prepareStatement(sql);
			pstmt.executeUpdate();
	
			return 1;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return -1;	// db error
	}
	
	static private int insertFile2(String file) {
		// file: /usr/local/lib/apache-tomcat-9.0.43/webapps/blockchain/serverFile/localhost_access_log.2021-02-27.txt
		System.out.println(getTime() + "CREATE NEW TABLE - " + file);
		String[] str = file.split("/");
		
		int index = files.size();
		files.add(str[8]);
		chain.add(new ArrayList<block>());
		chain.get(index).add(new block("START", file+"\n"));
		String name = tableNaming(str[8]);
		int last = readForFetch(str[8]);
		String sql = "INSERT INTO LOG(f_name, real_name, location, last_read_line) VALUES(?, ?, ?, ?);";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, name);
			pstmt.setString(2, str[8]);
			pstmt.setString(3, "/usr/local/lib/apache-tomcat-9.0.43/logs/");
			pstmt.setInt(4, last);
			pstmt.executeUpdate();
			
			sql = "CREATE TABLE " + name + " (content text not null, no int PRIMARY KEY auto_increment, sign varchar(350) not null);";
			pstmt = conn.prepareStatement(sql);
			pstmt.executeUpdate();
			
			sql = "GRANT select ON " + name + " TO user1@localhost";
			pstmt = conn.prepareStatement(sql);
			pstmt.executeUpdate();
			sql = "GRANT select ON " + name + " TO user2@localhost";
			pstmt = conn.prepareStatement(sql);
			pstmt.executeUpdate();
			
			dbUpload(str[8]);
			return 1;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return -1;	// db error
	}
	
	static private void dbUpload(String file) {
		int index = getIndex(file);
		ArrayList<block> b = chain.get(index);
		for(int i = 1; i < b.size(); i++)
			insertLog(tableNaming(file), b.get(i).getContent(), b.get(i).getSign());
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
		int index = getIndex(str2[6]);
		String name = tableNaming(str2[6]);
		int start = getLastLine(name);
		if(start > -2) {
			String tmp = "";
			for(int i = start + 1; i < line.size(); i++) {
				tmp += line.get(i) + "\n";
			}
			if(!tmp.equals("")) {
				try {
					String sign = sign(index);
					chain.get(index).add(new block(sign, tmp));
					addViewCand(name, tmp, sign, line.size()-1, str2[6], chain.get(index).size()-1);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	static private void addViewCand(String tablename, String content, String sign, int size, String path, int blockPosition) {
		String sql = "INSERT INTO CAND(f_name, content, sign, set_line, fetch_name, no, position) VALUES(?, ?, ?, ?, ?, ?, ?);";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tablename);
			pstmt.setString(2, content);
			pstmt.setString(3, sign);
			pstmt.setInt(4, size);
			pstmt.setString(5, path);
			pstmt.setInt(6, candNo);
			pstmt.setInt(7, blockPosition);
			candNo++;
			pstmt.executeUpdate();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	static protected String verify(int no) {
		String sql = "SELECT * FROM VERIFY WHERE no = ?;";	// no, f_name, userID, answer
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, no);
			rs = pstmt.executeQuery();
			int count = 0;
			String[] answer = new String[2];
			while(rs.next()) {
				answer[count++] = rs.getString(4);
			}
			if(count == 2) {
				sql = "SELECT f_name, content, sign, set_line, fetch_name, position FROM CAND WHERE no = ?";
				pstmt.setInt(1, no);
				rs = pstmt.executeQuery();
				if(rs.next()) {
					String f_name = rs.getString(1);
					String content = rs.getString(2);
					String sign = rs.getString(3);
					int line = rs.getInt(4);
					String fetch_name = rs.getString(5);
					int p = rs.getInt(6);
					
					sql = "DELETE FROM CAND WHERE no = ?;";
					pstmt = conn.prepareStatement(sql);
					pstmt.setInt(1, no);
					pstmt.executeUpdate();
					
					if(answer[0] == "Y" && answer[1] == "Y") {
						insertLog(f_name, content, sign);
						setLastLine(f_name, line);
						writeForFetch(fetch_name);
						return f_name;
					}
					else {
						//delete block
						int index = getIndex(fetch_name);
						for(int i = p; i < chain.get(index).size();) {
							chain.get(index).remove(i);
						}
						addViewCand(f_name, content, sign, line, fetch_name, p);
					}
				}	
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	static private String sign(int index) throws Exception {
		int size = chain.get(index).size();
		block b = chain.get(index).get(size-1);
		return encrypt((b.getSign() + b.getContent()).hashCode() + "", getPrivateKey(key[1]));
	}

	static private int insertLog(String file, String content, String sign) {
		System.out.println(getTime() + "INSERT new log data INTO " + file);
		String sql = "INSERT INTO " + file + "(content, sign) VALUES(?, ?);";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, content);
			pstmt.setString(2, sign);
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

	static private ArrayList<String> getUpdateList(){
		ArrayList<String> Line = new ArrayList<>();
		String path = "/usr/local/lib/apache-tomcat-9.0.43/webapps/update.txt";
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
				if(line.contains("/block/index.jsp") || line.contains("/block/fileUpload") || line.contains("/block/fileDownload"))
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
	
	static private int readForFetch(String filename) {		// local blockchain >> code
		int index = getIndex(filename);
		String str1 = "";
		String str2 = "";
		boolean sign = true;
		String path = "/usr/local/lib/apache-tomcat-9.0.43/webapps/blockchain/serverFile/" + filename;
		int count = 0;
		try {
			File file = new File(path);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufReader = new BufferedReader(fileReader);
			String line = "";	// sign \n content ,
			while((line = bufReader.readLine()) != null) {
				if(line.equals(",")) {
					sign = true;
					chain.get(index).add(new block(str1, str2));
					str2 = "";
				}
				if(sign) {
					str1 = line;
					sign = false;
				}
				else {
					str2 += line + "\n";
					count++;
				}	
			}
			bufReader.close();
		}catch(IOException e) {
			System.out.println(e);
		}
		return count;
	}
	
	static private void writeForFetch(String filename) {	// code >> local blockchain
		int index = getIndex(filename);
		ArrayList<block> b = chain.get(index);
		System.out.println("downwrite " + filename);
		String path = "/usr/local/lib/apache-tomcat-9.0.43/webapps/blockchain/serverFile/" + filename;
		try {
			File file = new File(path);
			if(!file.exists())
				file.createNewFile();
			FileWriter fw = new FileWriter(filename);
			
			String line = "";
			for(int i = 1; i < b.size(); i++) {
				line += b.get(i).getSign() + "\n" + b.get(i).getContent() + ",\n";
			}
			fw.write(line);
			fw.close();
		}catch(IOException e) {
			System.out.println(e);
		}
	}
	
	static private int getIndex(String name) {
		for(int i = 0; i < files.size(); i++) {
			if(files.get(i).equals(name)) {
				return i;
			}
		}
		return -1;
	}
	
	static private String getTime() {
        SimpleDateFormat f = new SimpleDateFormat("[hh:mm:ss]");
        return f.format(new Date());
    }
	
	static private void init() {
		System.out.println("=====================================BLOCKCAHIN CENTER START=====================================");
		String[] user = {"user1", "user2"};
		String[] server_view = {"VIEW_LOG", "VIEW_KEY", "VIEW_CAND"};
		String[] server = {
				"USE server;",
				"CREATE TABLE USER(userID varchar(200) primary key, userPW varchar(200) not null);",
				"CREATE TABLE LOG(f_name varchar(200) primary key, real_name varchar(200) not null, location varchar(200) not null, last_update_time varchar(20) not null, last_read_line int not null default -1);",
				"CREATE TABLE RSA_KEY(publicKey varchar(400), privateKey varchar(1650));",
				"CREATE TABLE CAND(no int auto_crement, f_name varchar(200), content text, sign varchar(350), set_line int, fetch_name varchar(200));",
				"CREATE TABLE VERIFY(no int not null, f_name varchar(200), userID varchar(200) not null, answer varchar(50) not null);",
				"CREATE VIEW VIEW_LOG as select f_name from LOG;",
				"CREATE VIEW VIEW_KEY as select publicKey from RSA_KEY;",
				"CREATE VIEW VIEW_CAND as select no, f_name, sign from CAND;"
		};
		String[] pw = {"Admin1!", "Admin2!"};
		String sql = "";
		PreparedStatement pstmt = null;
		try {
			String dbURL = "jdbc:mysql://localhost:3306?";
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection conn = DriverManager.getConnection(dbURL, "root", "Benel&Bende1");
			
			sql = "DROP DATABASE server;";
			pstmt = conn.prepareStatement(sql);
			pstmt.executeUpdate();
			sql = "CREATE DATABASE server;";
			pstmt = conn.prepareStatement(sql);
			pstmt.executeUpdate();
			sql = "GRANT create, select, insert, update, delete, drop, grant option ON server.* TO server@localhost;";
			pstmt = conn.prepareStatement(sql);
			pstmt.executeUpdate();
			
			// CREATE server table, view
			for(int i = 0; i < server.length; i++) {
				pstmt = conn.prepareStatement(server[i]);
				pstmt.executeUpdate();
			}
			// INSERT user account
			for(int i = 1; i < user.length; i++) {
				sql = "INSERT INTO USER VALUES(?, ?);";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, user[i]);
				pstmt.setString(2, pw[i-1]);
				pstmt.executeUpdate();
			}
			// INSERT rsa key
			key = setKey();
			sql = "INSERT INTO RSA_KEY VALUES(?, ?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, key[0]);
			pstmt.setString(2, key[1]);
			pstmt.executeUpdate();
			
			// GRANT select on view to user
			for(int i = 0; i < user.length; i++) {
				for(int j = 0; j < server_view.length; j++) {
					sql = "GRANT select ON server." + server_view[j] + " TO " + user[i] + "@localhost;";
					pstmt = conn.prepareStatement(sql);
					pstmt.executeUpdate();	
				}
				sql = "GRANT insert ON server.VERIFY TO " + user[i] + "@localhost;";
				pstmt = conn.prepareStatement(sql);
				pstmt.executeUpdate();
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	static private String[] setKey() throws NoSuchAlgorithmException, InvalidKeySpecException{
		String[] key = new String[2];
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		KeyPair keyPair = keyPairGenerator.genKeyPair();
		key[0] = KeyToStr(keyPair.getPublic());
		key[1] = KeyToStr(keyPair.getPrivate());
		return key;
	}
	
	static private String KeyToStr(Key key) {
		Encoder encoder = Base64.getEncoder();
		return new String(encoder.encode(key.getEncoded()));
	}
	
	static private String encrypt(String data, Key key) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] bCipher = cipher.doFinal(data.getBytes());
		Encoder encoder = Base64.getEncoder();
		String sCipherBase64 = new String(encoder.encode(bCipher));
        return sCipherBase64;
    }
	
	static private PrivateKey getPrivateKey(String privateKey) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        Decoder decoder = Base64.getDecoder();
        byte[] decodedKey = decoder.decode(privateKey.getBytes());
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
        return keyFactory.generatePrivate(keySpec);
    }
}
