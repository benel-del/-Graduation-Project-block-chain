package security;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
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
		String path = "C:\\JSP\\projects\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp0\\wtpwebapps\\block\\publicKey.txt";
		FileWriter fw = new FileWriter(path);
		fw.write(rsa.KeyToStr(rsa.getPublicKey()) + "\n");
		fw.close();
	}

	public block createBlock(String file) throws Exception {
		block b = null;
		int index = getIndex(file);
		if(index != -1) {
			String str = content(file);
			if(!str.equals("")) {
				b = new block(sign(index), str);
				System.out.println("[blockDAO.java] add chain: " + file);
			}
			else
				return b;
		}
		else {
			index = files.size();
			files.add(new fileInfo(file, 0));
			System.out.println("[blockDAO.java] new files, size: " + files.size());
			System.out.println("[blockDAO.java] files(" + index + ") : " + files.get(index).name);
			System.out.println("[blockDAO.java] create new chain: " + file);
			chain.add(new ArrayList<block>());
			b = new block("START", file);
		}
		chain.get(index).add(b);
		System.out.println("[blockDAO.java] chain size: " + chain.get(index).size());
		return b;
	}
	
	public ArrayList<block> getChainContents(String file){
		int index = getIndex(file);
		return chain.get(index);
	}
	
	public int isFile(String file) {
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
	
	private String content(String file) {
		int index = getIndex(file);
		int lastIndex = files.get(index).lastIndex;
		ArrayList<String> strs = readFile(file);
		String str = "";
		/*for(int i = 0; i < 5 && lastIndex < strs.size(); i++) {	// 한 블록 당 5문장
			str += strs.get(lastIndex++);
		}*/
		if(lastIndex < strs.size())	// 한 블록 당 1줄
			str = strs.get(lastIndex++);
		files.get(index).setLastIndex(lastIndex);
		return str;
	}
	
	public ArrayList<String> readFile(String filename) {
		ArrayList<String> Line = new ArrayList<>();
		String path = "C:\\JSP\\Tomcat 9.0\\logs\\" + filename;	// 경로 어디?
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
	
	public String readKey() throws IOException {
		String path = "C:\\JSP\\projects\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp0\\wtpwebapps\\block\\publicKey.txt";
		File key = new File(path);
		BufferedReader bufReader = new BufferedReader(new FileReader(key));
		return bufReader.readLine();
		
	}
}
