package blockChain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class blockDAO {
	
	static public ArrayList<String> readLogFile_client(String filename) {
		ArrayList<String> Line = new ArrayList<>();
		String path = filename;
		//System.out.println("[readLogFile] readFile path: "  + path);
		try {
			File file = new File(path);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufReader = new BufferedReader(fileReader);
			String line = "";
			while((line = bufReader.readLine()) != null) {
				if(line.contains("/block/index.jsp") || line.contains("/block/fileUpload.jsp") || line.contains("/block/fileDownload.jsp"))
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
	
	static public ArrayList<String> readLogFile_server(String filename) {
		ArrayList<String> Line = new ArrayList<>();
		String path = filename;
		//System.out.println("[readLogFile] readFile path: "  + path);
		try {
			File file = new File(path);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufReader = new BufferedReader(fileReader);
			String line = "";
			while((line = bufReader.readLine()) != null) {
				if(line.contains("/block/index.jsp") || line.contains("/block/fileUpload.jsp") || line.contains("/block/fileDownload.jsp") || line.contains("/block/logView.jsp"))
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
	
	static public ArrayList<String> readAllFile(){
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
			bufReader.close();
		}catch(FileNotFoundException e) {
			Line.add("Error:: file not found - " + path);
		}catch(IOException e) {
			System.out.println(e);
		}
		return Line;
	}
}
