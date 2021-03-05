package blockChain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class accessedByJava extends blockDAO {
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
	
	static ArrayList<fileInfo> files = new ArrayList<>();
	static ArrayList<ArrayList<block>> chain = new ArrayList<ArrayList<block>>();
	
	public accessedByJava() throws Exception {
		rsa.setKey();
		
		String path = "/usr/local/lib/apache-tomcat-9.0.43/webapps/blockChain/publicKey.txt";
		new File(path).delete(); new File(path).createNewFile();
		FileWriter fw = new FileWriter(path);
		fw.write(rsa.KeyToStr(rsa.getPublicKey()) + "\n");
		fw.close();
		
		ArrayList<String> files = readAllFile();
		for(int i = 0; i < files.size(); i++) {
			updateChain(files.get(i));
		}
	}
	
	public void updateChain(String file) throws Exception {
		int index = getIndex(file);
		if(index == -1)
			index = newChain(file);
		
		if(index != -1) {
			String str = "";
			ArrayList<String> Line = readLogFile(file);
			for(int i = files.get(index).lastIndex; i < Line.size(); i++)
				str += Line.get(i) + "\n";
			if(!str.equals("")) {
				chain.get(index).add(new block(sign(index), str));	// create block
				files.get(index).setLastIndex(Line.size());
				System.out.println("[updateChain] " + files.get(index).name + " - create new block[" + (chain.get(index).size()-1) + "]");
				writeChain(file);
			}
		}
	}

	private int newChain(String file) {
		int index = files.size();
		files.add(new fileInfo(file, 0));
		chain.add(new ArrayList<block>());
		chain.get(index).add(new block("START", file+"\n"));
		System.out.println("[newChain] create new chain - " + files.get(index).name);
		return index;
	}
	
	private int writeChain(String file) throws NumberFormatException, Exception {
		int index = getIndex(file);
		ArrayList<block> b = chain.get(index);
		
		String[] str = file.split("/");
		String path = "/usr/local/lib/apache-tomcat-9.0.43/webapps/blockChain/" + str[6];
		new File(path).delete();	new File(path).createNewFile();
		FileWriter fw = new FileWriter(path);
		String tmp = b.get(0).sign + "\n" + b.get(0).content + "(block)\n";
		for(int i = 1; i < b.size(); i++){
			int dec = Integer.parseInt(rsa.decrypt(b.get(i).sign, rsa.getPublicKey()));
			int hash = (b.get(i-1).sign + "\n" + b.get(i-1).content).hashCode();
			if(dec != hash){	// verification
				System.out.println("[writeChain] " + files.get(index).name + " - block chain verification error");
				fw.close();
				return -1;
			}
			else {
				tmp += b.get(i).sign + "\n" + b.get(i).content + "(block)\n";	// '(block)\n' : block end
			}
		}
		fw.write(tmp);
		fw.close();
		//System.out.println("[writeChain] " + files.get(index).name + " - block chain store in '.txt' file");
		return 1;
	}
	
	private String sign(int index) throws Exception {
		int size = chain.get(index).size();
		block b = chain.get(index).get(size-1);
		return rsa.encrypt((b.sign + "\n" + b.content).hashCode() + "", rsa.getPrivateKey());
	}
	
	private int getIndex(String name) {
		for(int i = 0; i < files.size(); i++) {
			if(files.get(i).name.equals(name)) {
				return i;
			}
		}
		return -1;
	}

	public ArrayList<String> readUpdateFile(){
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
}
