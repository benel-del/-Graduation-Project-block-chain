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
import java.util.Calendar;
import java.util.HashSet;
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
		JSONObject json = new JSONObject();
		String url = request.getParameter("name");
		if(url.equals("logView")) {
			String fileName = request.getParameter("file");
			option = request.getParameterValues("option");
			ArrayList<block> b = getChain(fileName);
			if(b != null) {
				String[] splitBlock;
				for(int i = 1; i < b.size(); i++){
					String str[] = b.get(i).getContent().split("\n");
					String state = b.get(i).getState();
					for(int j = 0; j < str.length; j++) {
						splitBlock = str[j].split("\\|");
						if (state.equals("Secure blockchain"))
							json = addJson(splitBlock, "0");
						else if(state.equals("SERVER Verification error"))
							json = addJson(splitBlock, "1");
						else if(state.equals("Different from local file"))
							json = addJson(splitBlock, "2");
						else if(state.equals("LOCAL Verification error"))	// ???
							json = addJson(splitBlock, "3");
					}
				}
			}
		}
		else if(url.equals("logsView")) {
			String chart = request.getParameter("chart");
			try {
				if (chart.equals("day"))
					json = countConnDay(); // ADD
				else if (chart.equals("month"))
					json = countConnMonth();
				else if (chart.equals("time"))
					json = countConnTime();
				else if (chart.equals("load"))
					json = countLoad();
				else if (chart.equals("err"))
					json = countStatus();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		out.print(json);
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
	private JSONObject addJson(String[] splitLog, String code) {
    	JSONArray temp = new JSONArray();
    	JSONObject temp2 = new JSONObject();
    	for (int i=0; i< option.length; i++) {
    		temp.add(splitLog[Integer.parseInt(option[i])]);
    	}
    	temp2.put(code, temp);
    	//json.add(temp2);
    	return temp2;
    }
	
	@SuppressWarnings("unchecked")
	private JSONObject countConnDay () throws Exception { // parameter, name change
		//File dir = new File("/usr/local/apache-tomcat-9.0.41/logs"); // ADD
		ArrayList<String> filelist = getList();
		//String[] filelist = dir.list(); // ADD
		String[] temp; // for file content split
		JSONObject jsObj = new JSONObject();
		String[] filename; // ADD
		int day = (filelist.size()>6) ? 7 : filelist.size(); // size() -> length
		for (int d=day; d>0; d--) { // per day
			HashSet<String> hs = new HashSet<String>();
			ArrayList<String> f = getLog(filelist.get(filelist.size()-d));
			//FileReader filerd = new FileReader("/usr/local/apache-tomcat-9.0.41/logs/"+filelist[filelist.length-d]); // ADD
			//BufferedReader br = new BufferedReader(filerd); // ADD
			//ArrayList<String> f = new ArrayList<String>(); // ADD
			//String line = ""; // ADD
			//while ((line=br.readLine())!=null) f.add(line); // ADD
			for (int j=0; j<f.size(); j++) {
				temp = f.get(j).split("\\|");
				hs.add(temp[0]);
			}
//			jsObj.put(filelist.get(filelist.size()-d), hs.size());
			//filename = filelist[filelist.length-d].split("\\."); // ADD
			filename = filelist.get(filelist.size()-d).split("\\."); // ADD
			jsObj.put(filename[1], hs.size()); // ADD
		}
		return jsObj;
	}
	
	@SuppressWarnings("unchecked")
	private JSONObject countConnMonth() throws Exception {
		Calendar cal = Calendar.getInstance();
		int recentM = cal.get(Calendar.MONTH)+1;
		int recentY = cal.get(Calendar.YEAR);
		//File dir = new File("/usr/local/apache-tomcat-9.0.41/logs/");
		//String[] filelist = dir.list();
		ArrayList<String> filelist = getList();
		String[] temp; // for file name split
		String[] temp2; // for file name split
		JSONObject jsObj = new JSONObject();
		ArrayList<String> monthList[] = new ArrayList[5];
		for (int i=0; i<5; i++) monthList[i]= new ArrayList<String>(); // reset
		for (int i=0; i<filelist.size(); i++) {
			temp = filelist.get(i).split("\\.");
			temp2 = temp[1].split("-");
			int index=0;
			if ((index=(recentY-Integer.parseInt(temp2[0]))*12+recentM-Integer.parseInt(temp2[1]))<6) {
				monthList[index].add(filelist.get(i));
			}
		}
		for (int m=4; m>=0; m--) { // per month
			HashSet<String> hs = new HashSet<String>();
			for (int j = 0; j<monthList[m].size(); j++) { // per log file
				ArrayList<String> f = getLog(monthList[m].get(j));
				//FileReader filerd = new FileReader("/usr/local/apache-tomcat-9.0.41/logs/"+monthList[m].get(j));
				//BufferedReader br = new BufferedReader(filerd);
				//ArrayList<String> f = new ArrayList<String>();
				//String line = ""; // ADD
				//while ((line=br.readLine())!=null) f.add(line); // reset
				for (int k=0; k<f.size(); k++) {
					temp = f.get(k).split("\\|");
					hs.add(temp[0]);
				}
			}
			String monthName = (recentM<=m ? (recentY-1)+"."+(recentM+12-m) : recentY+"."+(recentM-m));
			jsObj.put(monthName, hs.size());
		}
		// monthList[1].get();
		return jsObj;
	}
	
	// ? 
	private JSONObject countConnTime() throws Exception {
		// 0:0:0:0:0:0:0:1|127.0.0.1|11157|HTTP/1.1|GET|[19/Mar/2021:17:13:51 +0900]|200|-|/
		Calendar cal = Calendar.getInstance();
		//File dir = new File("/usr/local/apache-tomcat-9.0.41/logs/");
		//String[] filenames = dir.list();
		ArrayList<String> filelist = getList();
		JSONObject jsObj = new JSONObject();
		int[] countTime = new int[24];
		String line = ""; // 
		String[] temp; // for log line split
		for (int i = 0; i < filelist.size(); i++) {
		    if (filelist.get(i).equals("log_20210326")) {
				HashSet<String> hs = new HashSet<String>();
				FileReader filerd = new FileReader("/usr/local/apache-tomcat-9.0.41/logs/localhost_access_log."+cal.get(Calendar.YEAR)+"-"+String.format("%02d", cal.get(Calendar.MONTH) + 1)+"-"+cal.get(Calendar.DAY_OF_MONTH)+".txt"); // ADD
				BufferedReader br = new BufferedReader(filerd); // ADD
				while ((line=br.readLine())!=null) {
					if (line.contains("logView.jsp")) {
						temp = line.split("\\:");
						countTime[Integer.parseInt(temp[8])]++;
					}
				}
		    }
		}
		countTime[1]+=countTime[0];
		for (int i=1; i<24; i++) {
			if (i%3==2) jsObj.put((i/3)+"", countTime[i]);
			else countTime[i+1] += countTime[i];
		}
		return jsObj;
	}
	
	@SuppressWarnings("unchecked")
	private JSONObject countLoad() throws Exception {
		//File dir = new File("/usr/local/apache-tomcat-9.0.41/logs");
		//String[] filelist = dir.list(); // ADD
		ArrayList<String> filelist = getList();
		//String[] temp; // for file content split
		JSONObject jsObj = new JSONObject();
		JSONObject jtemp1 = new JSONObject();
		JSONObject jtemp2 = new JSONObject();
		String[] filename;
		int day = (filelist.size()>6) ? 7 : filelist.size();
		for (int d=day; d>0; d--) { // per day
			int[] count = new int[2];
			//FileReader filerd = new FileReader("/usr/local/apache-tomcat-9.0.41/logs/"+filelist[filelist.length-d]);
			///BufferedReader br = new BufferedReader(filerd);
			filename = filelist.get(filelist.size()-d).split("\\.");
			//String line = ""; // ADD
			//while ((line=br.readLine())!=null) {
			//	if (line.contains("upload.jsp")) count[0]++;
			//	else if (line.contains("download.jsp")) count[1]++;
			//}
			ArrayList<String> f = getLog(filelist.get(filelist.size()-d));
			for(int i = 0; i < f.size(); i++) {
				if(f.get(i).contains("Upload"))	count[0]++;
				else if(f.get(i).contains("Download"))	count[1]++;
			}
			jtemp1.put(filename[1], count[0]);
			jtemp2.put(filename[1], count[1]);
		}
		jsObj.put("upload", jtemp1);
		jsObj.put("download",jtemp2);
		return jsObj;
	}
	
	@SuppressWarnings("unchecked")
	private JSONObject countStatus() throws Exception {
		JSONObject jsObj = new JSONObject();
		//JSONObject jtemp1 = new JSONObject();
		//JSONObject jtemp2 = new JSONObject();
		//JSONObject jtemp3 = new JSONObject();
		//JSONObject jtemp4 = new JSONObject();
		ArrayList<String> filelist = getList();
		String[] temp; // for file content split
		String[] filename;
		ArrayList<String> status = new ArrayList<String>();
		int day = (status.size()>6) ? 7 : status.size(); // size() -> length
		for (int d=day; d>0; d--) { // per day
			HashSet<String> hs = new HashSet<String>();
			ArrayList<String> f = getLog(filelist.get(filelist.size()-d));
//			BufferedReader br = new BufferedReader(filerd);
//			ArrayList<String> f = new ArrayList<String>();
//			String line = ""; // ADD
//			while ((line=br.readLine())!=null) f.add(line);
			for (int j=0; j<f.size(); j++) {
				temp = f.get(j).split("\\|");
				hs.add(temp[0]);
			}
//			jsObj.put(filelist.get(filelist.size()-i), hs.size());
//			filename = filelist[filelist.length-d].split("\\.");
			filename = filelist.get(filelist.size()-d).split("\\.");
			jsObj.put(filename[1], hs.size());
		}
		return jsObj;
	}
}
