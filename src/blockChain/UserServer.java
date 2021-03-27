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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Decoder;

import javax.crypto.Cipher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/access")
public class UserServer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection conn;
	private ResultSet rs;
	private Socket soc;
	private String key;
	private ArrayList<String> files = new ArrayList<>();
	static private ArrayList<ArrayList<block>> chain = new ArrayList<ArrayList<block>>();
	
	static String userID;
	static String userPW;
	
	public UserServer(String userID, String userPW){
		super();
		String dbURL = "jdbc:mysql://localhost:3306/" + userID + "?";
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(dbURL, userID, userPW);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("blockChainServer access [" + userID + "]");
		this.userID = userID;
		this.userPW = userPW;
	}
	
	@Override
	public void init() {
		fetchChain();	// server >> java code chain
		
		for(String file : files) {
			compare(file);
		}

		try {
			Socket soc = new Socket("localhost", 6010);

			BufferedReader br = new BufferedReader(new InputStreamReader(soc.getInputStream()));
			PrintWriter pw = new PrintWriter(soc.getOutputStream());

			System.out.println(" Accept to Server Success...");
			
			while(true) {
				String input = br.readLine();	// cand.no
				if(verify(input) != -1) {
					pw.println("ok");


				}
				pw.flush();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void destroy() {
		try {
			soc.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	}

	private void getLocalFileList() {	
		try {
			String sql = "SELECT f_name FROM BlockChain;";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				localfile.add(rs.getString(1));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void compare(String file) {	// 2) compare server blockchain with disk blockchain
		ArrayList<block> local = readForFetch(file);
	}
	
	

	private void fetchChain() {	// 1) get block chain in server
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
						state = "Verification error";
					}
					chain.get(chain.size()-1).add(new block(rs.getString(1), rs.getString(2), state));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	static private ArrayList<block> readForFetch(String filename) {		// local blockchain >> code
		String str1 = "";
		String str2 = "";
		boolean sign = true;
		ArrayList<block> b = new ArrayList<>();
		b.add(new block("START", filename+"\n"));
		String path = "/usr/local/lib/apache-tomcat-9.0.43/webapps/blockchain/" + userID +" File/" + filename;
		try {
			File file = new File(path);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufReader = new BufferedReader(fileReader);
			String line = "";	// sign \n content ,
			while((line = bufReader.readLine()) != null) {
				if(line.equals(",")) {
					sign = true;
					b.add(new block(str1, str2));
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
		}catch(IOException e) {
			System.out.println(e);
		}
		return b;
	}
	
	static private void writeForFetch(String filename) {	// code >> local blockchain
		int index = getIndex(filename);
		ArrayList<block> b = chain.get(index);
		System.out.println("downwrite " + filename);
		String path = "/usr/local/lib/apache-tomcat-9.0.43/webapps/blockchain/" + userID +" File/" + filename;
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
	
	private void fecthContent(String file) {	// 3) server new content >> block chain
		int index = getIndex(file);
		String sql = "";
		PreparedStatement pstmt;
		
		int start = 0;
		if(index == -1) {
			createTable(file);
		}
		else {
			sql ="SELECT * FROM " + file + ";";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				start++;
				
			}
		}
		sql = "SELECT * FROM server.VIEW_CAND;";
		try {
			
			rs = pstmt.executeQuery();
			int pre = chain.get(index).size();
			while(rs.next()) {
				chain.get(index).add(new block(sign(index), rs.getString(1)));
			}
			if(chain.get(index).size() != pre) {	// uploadChain - 4) block chain >> local db
				ArrayList<block> b = chain.get(index);
				ArrayList<block> other = getMyChain(file, pre);
				for(int i = pre; i < b.size(); i++) {
					sql = "INSERT INTO " + file + "(no, sign, content, state) VALUES(?, ?, ?, ?);";
					try {
						pstmt = conn.prepareStatement(sql);
						pstmt.setInt(1, i);
						pstmt.setString(2, b.get(i).getSign());
						pstmt.setString(3, b.get(i).getContent());
						
						// verification
						String state = "Secure blockchain";
						int dec = Integer.parseInt(decrypt(b.get(i).getSign(), getPublicKey(key));
						int hash = (b.get(i-1).getSign() + b.get(i-1).getContent()).hashCode();
						if(dec != hash){ // error
							state = "Verification error";
						}
						pstmt.setString(4, state);
						pstmt.executeUpdate();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
			else return;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private int getIndex(String name) {
		for(int i = 0; i < files.size(); i++) {
			if(files.get(i).equals(name)) {
				return i;
			}
		}
		return -1;
	}
	
	private void getKey() {
		String sql = "SELECT publicKey FROM server.RSA_KEY;";
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
	
	public String decrypt(String data, Key key) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA");
		Decoder decoder = Base64.getDecoder();
		byte[] bCipher = decoder.decode(data.getBytes());
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] bPlain = cipher.doFinal(bCipher);
		return new String(bPlain);
    }
	
	private PublicKey getPublicKey(String publicKey) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        Decoder decoder = Base64.getDecoder();
        byte[] decodedKey = decoder.decode(publicKey.getBytes());
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        return keyFactory.generatePublic(keySpec);
    }
	
	public ArrayList<String> getList(){
		return files;
	}
	public ArrayList<ArrayList<block>> getChain(){
		return chain;
	}
	public ArrayList<block> getChain(String file){
		int index = getIndex(file);
		if(index == -1)
			return null;
		return chain.get(index);
	}
	public ArrayList<String> getLog(String file){
		int index = getIndex(file);
		if(index == -1)
			return null;
		ArrayList<block> b = chain.get(index);
		ArrayList<String> log = new ArrayList<>();
		for(int i = 1; i < b.size(); i++) {
			if(!b.get(i).getState().equals("Verification error")) {
				String[] str = b.get(i).getContent().split("\n");
				for(int j = 0; j < str.length; j++) 
					log.add(str[j]);
			}
		}
		return log;
	}
}
