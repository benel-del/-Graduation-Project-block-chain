package blockChain;

import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class blockChainServer {
	public static void main(String[] args) throws Exception {
		accessedByJava dao = new accessedByJava();
		int sleepSec = 60;

		final ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
		exec.scheduleAtFixedRate(new Runnable(){
			public void run(){
				try {					
					ArrayList<String> files = dao.readUpdateFile();
					for(int i = 0; i < files.size(); i++) {
						dao.updateChain(files.get(i));
					}
					
				} catch (Exception e) {
					e.printStackTrace();
					exec.shutdown();
				}
			}
		}, 0, sleepSec, TimeUnit.SECONDS);
	
	}
}
