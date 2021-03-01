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
				System.out.println("[hashcode] block chain verification - error");
				return null;
			}
			else {
				b.add(new block(strs[0], strs[1] + "\n" + strs[2] + "\n" + strs[3] + "\n" + strs[4] + "\n" + strs[5] + "\n"));
			}
		}
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
			int index = 0;
			String line = "";
			String str = "";
			while((line = bufReader.readLine()) != null) {
				if(index == 6) {
					Line.add(str);
					str = "";
					index = 0;
				}
				str += line + "\n";
				index++;
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
