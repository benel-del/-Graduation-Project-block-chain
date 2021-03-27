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
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@WebServlet("/access")
public class UserServer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	JSONArray json;
	
	private Connection conn;
	private ResultSet rs;
	private Socket soc;
	private String key;
	private ArrayList<String> files = new ArrayList<>();
	private ArrayList<ArrayList<block>> chain = new ArrayList<ArrayList<block>>();
	
	String userID;
	String userPW;
	String[] option;
	
	public UserServer(String userID, String userPW){
		super();
		String dbURL = "jdbc:mysql://localhost:3306/" + userID + "?";
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(dbURL, userID, userPW);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		System.out.println("blockChainServer access [" + userID + "]");
		this.userID = userID;
		this.userPW = userPW;
	}
	
	@Override
	public void init() {
		getKey();
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
				if(verify() == 1)		///asfdasd
					pw.println("ok");
				String file = br.readLine();
				if(!file.equals("no")) {	// add block
					fetchContent(file);
				}
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
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=utf-8");
		PrintWriter out = response.getWriter();
		
		String url = request.getParameter("name");
		if(url.equals("logView")) {
			String fileName = request.getParameter("file");
			option = request.getParameterValues("option");

			ArrayList<block> b = getChain(fileName);
			if(b != null) {
				String[] splitBlock;
				json = new JSONArray();
				for(int i = 1; i < b.size(); i++){
					String str[] = b.get(i).getContent().split("\n");
					String state = b.get(i).getState();
					for(int j = 0; j < str.length; j++) {
						splitBlock = str[j].split("\\|");
						if (state.equals("Secure blockchain"))
							addJson(splitBlock, "0");
						else if(state.equals("SERVER Verification error"))
							addJson(splitBlock, "1");
						else if(state.equals("Different from local file"))
							addJson(splitBlock, "2");
						else if(state.equals("LOCAL Verification error"))	// ???
							addJson(splitBlock, "3");
					}
				}
			}
			out.print(json);
		}
		else if(url.equals("logsView")) {
			//String fileName = request.getParameter("file");
			//String[] option = request.getParameterValues("option");
			logsView(fileName, option);
		}
	}
	
	private int verify() {	// 3) server.CAND check. if verify then return 1
		String sql = "SELECT * from server.VIEW_CAND";	// no, f_name, sign
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				int no = rs.getInt(1);
				String f_name = rs.getString(2);
				String sign = rs.getString(3);
				
				ArrayList<block> b;
				int index = getIndex(f_name);
				if(index == -1) {
					b = new ArrayList<block>();
					b.add(new block("START", f_name + "\n"));	// genesis block
				}
				else {
					b = chain.get(index);
				}
				
				String state = "Y";
				int dec = Integer.parseInt(decrypt(sign, getPublicKey(key)));
				int hash = (b.get(b.size()-1).getSign() + b.get(b.size()-1).getContent()).hashCode();
				if(dec != hash){ // error
					state = "N";
				}
				
				sql = "INSERT INTO server.VERIFY(no, f_name, userID, answer) VALUES(?, ?, ?, ?)";
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, no);
				pstmt.setString(2, f_name);
				pstmt.setString(3, userID);
				pstmt.setString(4, state);
				pstmt.executeUpdate();
				
				return no;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	private void compare(String file) {	// 2) compare server blockchain with disk blockchain
		int index = getIndex(file);
		ArrayList<block> local = readForFetch(file);
		ArrayList<block> b = chain.get(index);
		for(int i = 1; i < b.size(); i++) {
			if(i >= local.size()) {	// 검증했던 블록이 로그아웃 이후로 추가된 것
				writeForFetch(file);
				return;
			}
			else if(b.get(i).getState().equals("SERVER Verification error"))
				continue;
			else if(local.get(i).getState().equals("Verification error")){	// local 신뢰성 잃음
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
						state = "SERVER Verification error";
					}
					chain.get(chain.size()-1).add(new block(rs.getString(1), rs.getString(2), state));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private ArrayList<block> readForFetch(String filename) {		// local blockchain >> code
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
		}catch(Exception e) {
			System.out.println(e);
		}
		return b;
	}
	
	private void writeForFetch(String filename) {	// code >> local blockchain
		int index = getIndex(filename);
		ArrayList<block> b = chain.get(index);
		System.out.println("WRITE file " + filename);
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
	
	private void fetchContent(String file) {	// 4) add new block
		int index = getIndex(file);
		if(index == -1) {
			chain.add(new ArrayList<block>());
			chain.get(chain.size()-1).add(new block("START", file + "\n"));	// genesis block
			files.add(file);
		}

		String sql = "SELECT no, sign, content FROM server." + file + " ORDER BY no DESC LIMIT 1;";	// 
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				if(rs.getInt(1) == chain.get(index).size()) {
					String state = "Secure blockchain";
					int dec = Integer.parseInt(decrypt(rs.getString(2), getPublicKey(key)));
					int hash = (chain.get(index).get(chain.get(index).size()-1).getSign() + chain.get(index).get(chain.get(index).size()-1).getContent()).hashCode();
					if(dec != hash){ // error
						state = "SERVER Verification error";
					}
					chain.get(index).add(new block(rs.getString(2), rs.getString(3), state));
					writeForFetch(file);
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
	
	private String decrypt(String data, Key key) throws Exception {
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
	
	@SuppressWarnings("unchecked")
	private void addJson(String[] splitLog, String code) {
    	JSONArray temp = new JSONArray();
    	JSONObject temp2 = new JSONObject();
    	for (int i=0; i< option.length; i++) {
    		temp.add(splitLog[Integer.parseInt(option[i])]);
    	}
    	temp2.put(code, temp);
    	json.add(temp2);
    }
}
