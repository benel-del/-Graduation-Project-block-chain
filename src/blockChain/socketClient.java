package blockChain;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class socketClient {
	ArrayList<String> content;
	public socketClient(String file){
		try{
			Socket soc = new Socket("localhost", 6000);
			// System.out.println(getTime() + " Accept to Server Success...");
			
			BufferedReader br = new BufferedReader(new InputStreamReader(soc.getInputStream()));
			PrintWriter pw = new PrintWriter(soc.getOutputStream());
			
			pw.println(file);
			pw.flush();
			
			String line = "";
			content = new ArrayList<>();
			while((line = br.readLine()) != null) {
				pw.println(line);
				content.add(line);
			}
			
			soc.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<String> getContent(){
		return content;
	}
	
	public String getTime() {
        String threadName = Thread.currentThread().getName();
        SimpleDateFormat f = new SimpleDateFormat("[hh:mm:ss]");
        return f.format(new Date()) + threadName;
    }
	
}
