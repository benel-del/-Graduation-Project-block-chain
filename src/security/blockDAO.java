package security;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

public class blockDAO {
	class fileInfo{
		String name;
		int lastIndex;
		public fileInfo(String file, int i) {
			this.name = file;
			this.lastIndex = i;
		}
		void setLastIndex(int index) {
			this.lastIndex = index;
		}
	}
	
	static private RSA rsa;
	static ArrayList<fileInfo> files = new ArrayList<>();
	static ArrayList<ArrayList<block>> chain = new ArrayList<ArrayList<block>>();
	
	public void init() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException{
		rsa = new RSA();
		rsa.setKey();
	}
	
	public void updateChain(String file) throws Exception {	// called by test.java
		int index = getIndex(file);
		if(index == -1)
			index = newChain(file);
		
		int i = files.get(index).lastIndex;
		ArrayList<String> Line = readLogFile(file);
		for(int j; i < Line.size(); i++) {
			String str = "";
			for(j = 0; j < 5 && i < Line.size(); j++)
				str += Line.get(i++);
			if(j >= 5) {
				chain.get(index).add(new block(sign(index), str));	// create block
				files.get(index).setLastIndex(i);
			}
		}
	}

	private int newChain(String file) {
		int index = files.size();
		files.add(new fileInfo(file, 0));
		chain.add(new ArrayList<block>());
		chain.get(index).add(new block("START", file));
		return index;
	}
	
	public ArrayList<block> getChain(String file) throws NumberFormatException, Exception{	// called by test.jsp
		int index = getIndex(file);
		ArrayList<block> b = chain.get(index);
		for(int i = 1; i < b.size(); i++){
			int dec = Integer.parseInt(rsa.decrypt(b.get(i).sign, rsa.getPublicKey()));
			int hash = (b.get(i-1).sign + "||" + b.get(i-1).content).hashCode();
			if(dec != hash){	// verification
				System.out.println("[hashcode] block chain verification - error");
				return null;
			}
		}
		return b;
	}
	
	public int isFile(String file) {	// called by test.jsp
		int index = getIndex(file);
		//System.out.println("[blockDAO.java] check isFile: " + file + " - " + index);
		return index;
	}
	
	private int getIndex(String name) {
		for(int i = 0; i < files.size(); i++) {
			if(files.get(i).name.equals(name)) {
				//System.out.println("[blockDAO.java] getIndex: " + files.get(i).name + " and " + i);
				return i;
			}
		}
		return -1;
	}

	private String sign(int index) throws Exception {
		int size = chain.get(index).size();
		block b = chain.get(index).get(size-1);
		return rsa.encrypt((b.sign + "||" + b.content).hashCode() + "", rsa.getPrivateKey());
	}
	
	public ArrayList<String> readUpdateFile(){	// called by test.java
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
		}catch(FileNotFoundException e) {
			Line.add("Error:: file not found - " + path);
		}catch(IOException e) {
			System.out.println(e);
		}
		return Line;
	}
	
	public ArrayList<String> readAllFile(){	// called by test.jsp
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
		}catch(FileNotFoundException e) {
			Line.add("Error:: file not found - " + path);
		}catch(IOException e) {
			System.out.println(e);
		}
		return Line;
	}
	
	public ArrayList<String> readLogFile(String filename) {
		ArrayList<String> Line = new ArrayList<>();
		String path = filename;
		System.out.println("[blockDAO] readFile path: "  + path);
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
	
}
