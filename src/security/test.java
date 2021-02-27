package security;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class test {
	public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		blockDAO dao = new blockDAO();
		int sleepSec = 60;
		//String file  = "catalina.2021-02-26.log";

		dao.init();
		
		final ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
		exec.scheduleAtFixedRate(new Runnable(){
			public void run(){
				try {
					//System.out.println("[test.java] execute createBlock");
					//dao.createBlock(file);
					
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
