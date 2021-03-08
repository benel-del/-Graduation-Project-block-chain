package blockChain;

import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/websockets")
public class blockChainServer {
	accessedByJava dao;
	
	public blockChainServer() throws Exception {
		dao = new accessedByJava();
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
	
	@OnMessage
	public String handleMessage(String message) throws Exception {
		ArrayList<String> list = dao.getChain(message);
		String str = "";
		for(int i = 0; i < list.size(); i++)
			str += list.get(i);
		return str;
	}
	
	@OnError
	public void handleError(Throwable t) {
		t.printStackTrace();
	}
}
