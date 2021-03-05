package blockChain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class accessedByJSP extends blockDAO {

	public accessedByJSP() throws Exception {
		String path = "/usr/local/lib/apache-tomcat-9.0.43/webapps/blockChain/publicKey.txt";
		FileReader fileReader = new FileReader(new File(path));
		BufferedReader bufReader = new BufferedReader(fileReader);
		rsa.setPublicKey(bufReader.readLine());
	}
	
	public ArrayList<block> getChain(String file) throws NumberFormatException, Exception{
		String[] str = file.split("/");
		ArrayList<String> s = readBlockFile(str[6]);
		ArrayList<block> b = new ArrayList<>();
		for(int i = 1; i < s.size(); i++){
			String[] strs = s.get(i).split("\n");
			int dec = Integer.parseInt(rsa.decrypt(strs[0], rsa.getPublicKey()));
			int hash = s.get(i-1).hashCode();
			if(dec != hash){	// verification
				b.add(new block("","[hashcode error] " + str[6]));
				b.add(new block("", "pre block: " + s.get(i-1)));
				b.add(new block("", "now block sign: " + strs[0]));
				break;
			}
			else {
				String content = "";
				for(int j = 1; j < strs.length; j++) {
					content += strs[j] + "\n";
				}
				b.add(new block(strs[0], content));
			}
		}
		System.out.println("[getChain] get block chain - " + str[6]);
		return b;
	}
	
	public ArrayList<String> readBlockFile(String filename) {
		ArrayList<String> Line = new ArrayList<>();
		String path = "/usr/local/lib/apache-tomcat-9.0.43/webapps/blockChain/" + filename;
		System.out.println("[readBlockFile] readFile path: "  + path);
		try {
			File file = new File(path);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufReader = new BufferedReader(fileReader);
			String line = "";
			String str = "";
			while((line = bufReader.readLine()) != null) {
				if(line.equals("(block)")) {
					Line.add(str);
					str = "";
				}
				else
					str += line + "\n";
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
