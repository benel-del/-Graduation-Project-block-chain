package file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class FileDAO {
	public ArrayList<String> read(String name) {
		ArrayList<String> Line = new ArrayList<>();
		String path = "/Users/kjs/JSP/.metadata/.plugins/org.eclipse.wst.server.core/tmp1/wtpwebapps/block/uploadFile/" + name;
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

	public String fileSize(long number) {
		if(number < (float)1024)
			return number + "bytes";
		else if(number >= 1024 && number < 1048576)
			return Math.round((number/(float)1024)*10)/(float)10 + "KB";
		else
			return Math.round((number/(float)1048576)*10)/(float)10 + "MB";
	}

}
