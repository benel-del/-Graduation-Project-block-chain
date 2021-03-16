package blockChain;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class socketClient {
	private ArrayList<String> files = new ArrayList<>();
	private ArrayList<ArrayList<String>> chain = new ArrayList<ArrayList<String>>();
	
	public socketClient(){
		try{
			Socket soc = new Socket("localhost", 6000);
			System.out.println(getTime() + " Accept to Server Success...");
			
			BufferedReader br = new BufferedReader(new InputStreamReader(soc.getInputStream()));
			PrintWriter pw = new PrintWriter(soc.getOutputStream());
			
			pw.println("constructor");
			pw.flush();
			
			int loop = Integer.parseInt(br.readLine());	// block chain number
			for(int i = 0; i < loop; i++) {
				files.add(br.readLine());	// file name
				String line = "";
				chain.add(new ArrayList<>());
				while((line = br.readLine()) != null) {
					if(!line.equals("\n"))
						chain.get(i).add(line);
					System.out.println(line);
				}
			}
			
			final ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
			exec.scheduleAtFixedRate(new Runnable(){
				public void run(){
					try {					
						pw.println("update");
						pw.flush();
						
						int loop = Integer.parseInt(br.readLine());	// update chain number
						for(int i = 0; i < loop; i++) {
							String line = br.readLine();	// update chain name
							int index = getIndex(line);
							chain.get(index).clear();
							while((line = br.readLine()) != null) {
								if(!line.equals("\n"))
									chain.get(index).add(line);
								System.out.println(line);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						exec.shutdown();
					}
				}
			}, 0, 60, TimeUnit.SECONDS);
			
			soc.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<String> getAllChainName() {
		return files;
	}
	
	public ArrayList<ArrayList<String>> getAllChainContent() {
		return chain;
	}
	
	private int getIndex(String name) {
		for(int i = 0; i < files.size(); i++) {
			if(files.get(i).equals(name)) {
				return i;
			}
		}
		return -1;
	}
	
	private String getTime() {
        String threadName = Thread.currentThread().getName();
        SimpleDateFormat f = new SimpleDateFormat("[hh:mm:ss]");
        return f.format(new Date()) + threadName;
    }
	
}
