package blockChain;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Decoder;
import javax.crypto.Cipher;

public class UserServer {
	Connection conn;
	ResultSet rs;
	String key;
	ArrayList<String> files = new ArrayList<>();
	ArrayList<ArrayList<block>> chain = new ArrayList<ArrayList<block>>();
	
	String userID;
	String userPW;

	public UserServer(String userID, String userPW){
		this.userID = userID;
		this.userPW = userPW;
		
		String dbURL = "jdbc:mysql://localhost:3306/server?";
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(dbURL, userID, userPW);
			
			getKey();
			fetchChain();	// server >> java code chain
			for(String file : files)
				compare(file);
			System.out.println("blockChainServer access [" + userID + "]");
			connect();
		} catch (Exception e1) {
			e1.printStackTrace();
		}	
	}
	
	public void connect() {
		try {
			Socket soc = new Socket("localhost", 5935);

			//BufferedReader brs = new BufferedReader(new InputStreamReader(soc.getInputStream()));
			PrintWriter pw = new PrintWriter(soc.getOutputStream());
			
			System.out.println("[VERIFY] Accept to Server Success...");
			pw.println("verify");	// 1

			String[] str;
			while(true) {
				str = verify(userID);
				if(str == null) {
					pw.println("-1");
					break;
				}	
				pw.println(str[0]);	// no
				fetchContent(str);
				//String file = brs.readLine();
				//System.out.println(file);
			}

			pw.flush();
			soc.close();
            return;
		} catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	int check() {
		return 1;
	}
	
	String[] verify(String userID) {	// 3) server.CAND check. if verify then return record number
		String sql = "SELECT * from server.VIEW_CAND WHERE VIEW_CAND.no NOT IN(SELECT no FROM server.VERIFY WHERE userID = ?) ORDER BY no ASC LIMIT 1";	// no, f_name, sign
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userID);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				String str[] = {""+rs.getInt(1), rs.getString(2)};	//no, f_name
				System.out.println("[VERIFY] " + userID + ": number" + str[0]);

				ArrayList<block> b;
				int index = getIndex(str[1]);
				if(index == -1) {
					b = new ArrayList<block>();
					b.add(new block("START", str[1] + "\n"));	// genesis block
				}
				else {
					b = chain.get(index);
				}
				
				String state = "Y";
				int dec = Integer.parseInt(decrypt(rs.getString(3), getPublicKey(key)));
				int hash = (b.get(b.size()-1).getSign() + b.get(b.size()-1).getContent()).hashCode();
				if(dec != hash){ // error
					state = "N";
				}
				
				sql = "INSERT INTO server.VERIFY(no, f_name, userID, answer) VALUES(?, ?, ?, ?)";
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, Integer.parseInt(str[0]));
				pstmt.setString(2, str[1]);
				pstmt.setString(3, userID);
				pstmt.setString(4, state);
				pstmt.executeUpdate();
				
				return str;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	void compare(String file) {	// 2) compare server blockchain with disk blockchain
		int index = getIndex(file);
		ArrayList<block> local = readForFetch(file);
		ArrayList<block> b = chain.get(index);
		for(int i = 1; i < b.size(); i++) {
			if(i >= local.size()) {	// 寃�利앺뻽�뜕 釉붾줉�씠 濡쒓렇�븘�썐 �씠�썑濡� 異붽��맂 寃�
				writeForFetch(file);
				return;
			}
			else if(b.get(i).getState().equals("SERVER Verification error"))
				continue;
			else if(local.get(i).getState().equals("Verification error")){	// local �떊猶곗꽦 �엪�쓬
				chain.get(index).get(i).setState("LOCAL Verification error");
				writeForFetch(file);
				return;
			}
			else {
				if(!local.get(i).getSign().equals(b.get(i).getSign()) || !local.get(i).getContent().equals(b.get(i).getContent())){	// local != server
					chain.get(index).get(i).setState("Different from local file");
				}
			}
		}
	}

	void fetchChain() {	// 1) get server's blockchain
		System.out.println("[FETCH] " + userID + " - get server's blockchain");
		String sql = "SELECT f_name FROM server.VIEW_LOG;";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			ArrayList<String> f_name = new ArrayList<>();
			while(rs.next()) {
				f_name.add(rs.getString(1));
			}
			
			for(int i = 0; i < f_name.size(); i++) {
				String file = f_name.get(i);
				chain.add(new ArrayList<block>());
				chain.get(chain.size()-1).add(new block("START", file + "\n"));	// genesis block
				files.add(file);
				
				sql = "SELECT sign, content FROM server." + file + ";";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				while(rs.next()) {
					String state = "Secure blockchain";
					int dec = Integer.parseInt(decrypt(rs.getString(1), getPublicKey(key)));
					int hash = (chain.get(chain.size()-1).get(chain.get(chain.size()-1).size()-1).getSign() + chain.get(chain.size()-1).get(chain.get(chain.size()-1).size()-1).getContent()).hashCode();
					if(dec != hash){ // error
						state = "SERVER Verification error";
					}
					chain.get(chain.size()-1).add(new block(rs.getString(1), rs.getString(2), state));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	ArrayList<block> readForFetch(String filename) {		// local blockchain >> code
		String str1 = "";
		String str2 = "";
		boolean sign = true;
		ArrayList<block> b = new ArrayList<>();
		b.add(new block("START", filename+"\n"));
		
		String path = "/usr/local/lib/apache-tomcat-9.0.43/webapps/blockchain/" + userID +" File/" + filename;
		try {
			File file = new File(path);
			if(file.exists()) {
				FileReader fileReader = new FileReader(file);
				BufferedReader bufReader = new BufferedReader(fileReader);
				String line = "";	// sign \n content ,
				while((line = bufReader.readLine()) != null) {
					if(line.equals(",")) {
						sign = true;
						String state = "Secure blockchain";
						int dec = Integer.parseInt(decrypt(str1, getPublicKey(key)));
						int hash = (b.get(b.size()-1).getSign() + b.get(b.size()-1).getContent()).hashCode();
						if(dec != hash){ // error
							state = "Verification error";
						}
						b.add(new block(str1, str2, state));
						str2 = "";
					}
					if(sign) {
						str1 = line;
						sign = false;
					}
					else {
						str2 += line + "\n";
					}
				}
				bufReader.close();
			}
		}catch(Exception e) {
			System.out.println(e);
		}
		return b;
	}
	
	void writeForFetch(String filename) {	// code >> local blockchain
		int index = getIndex(filename);
		ArrayList<block> b = chain.get(index);
		System.out.println("WRITE file " + filename);
		String path = "/usr/local/lib/apache-tomcat-9.0.43/webapps/blockchain/" + userID +" File/" + filename;
		try {
			File file = new File(path);
			if(!file.exists())
				file.createNewFile();
			FileWriter fw = new FileWriter(file);
			
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
	
	void fetchContent(String[] str) {	// 4) add new block
		// str: no, f_name
		String sql = "SELECT no, sign, content FROM server." + str[1] + " ORDER BY no DESC LIMIT 1;";	// 
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				int index = getIndex(str[1]);
				if(index == -1) {
					chain.add(new ArrayList<block>());
					chain.get(chain.size()-1).add(new block("START", str[1] + "\n"));	// genesis block
					files.add(str[1]);
				}
				if(rs.getInt(1) == chain.get(index).size()) {
					String state = "Secure blockchain";
					int dec = Integer.parseInt(decrypt(rs.getString(2), getPublicKey(key)));
					int hash = (chain.get(index).get(chain.get(index).size()-1).getSign() + chain.get(index).get(chain.get(index).size()-1).getContent()).hashCode();
					if(dec != hash){ // error
						state = "SERVER Verification error";
					}
					chain.get(index).add(new block(rs.getString(2), rs.getString(3), state));
					writeForFetch(str[1]);
					System.out.println("[FETCH] add new block");
				}
			}
			else return;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	int getIndex(String name) {
		for(int i = 0; i < files.size(); i++) {
			if(files.get(i).equals(name)) {
				return i;
			}
		}
		return -1;
	}
	
	void getKey() {
		String sql = "SELECT publicKey FROM server.VIEW_KEY;";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				key = rs.getString(1);
			}
			return;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	String decrypt(String data, Key key) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA");
		Decoder decoder = Base64.getDecoder();
		byte[] bCipher = decoder.decode(data.getBytes());
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] bPlain = cipher.doFinal(bCipher);
		return new String(bPlain);
    }
	
	PublicKey getPublicKey(String publicKey) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        Decoder decoder = Base64.getDecoder();
        byte[] decodedKey = decoder.decode(publicKey.getBytes());
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        return keyFactory.generatePublic(keySpec);
    }
	
	public ArrayList<String> getList(){
		return files;
	}
	public ArrayList<block> getChain(String file){	// logView
		int index = getIndex(file);
		if(index == -1)
			return null;
		return chain.get(index);
	}
	public ArrayList<String> getLog(String file){	// logsView
		int index = getIndex(file);
		if(index == -1)
			return null;
		ArrayList<block> b = chain.get(index);
		ArrayList<String> log = new ArrayList<>();
		for(int i = 1; i < b.size(); i++) {
			if(!b.get(i).getState().equals("SERVER Verification error")) {
				String[] str = b.get(i).getContent().split("\n");
				for(int j = 0; j < str.length; j++) 
					log.add(str[j]);
			}
		}
		return log;
	}

}

