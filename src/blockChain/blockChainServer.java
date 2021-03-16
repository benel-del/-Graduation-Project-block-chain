package blockChain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import security.RSA;

class Sockets extends Thread {
	Socket client;
	Sockets(Socket client){
		this.client = client;
	}
	
	public void run() {
		try {
			//System.out.println(getTime() + " Client has accepted...");
			
			BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter pw = new PrintWriter(client.getOutputStream());
			
            String input = br.readLine();
            if(input.equals("constructor")) {
            	ArrayList<String> lists = blockChainServer.getFiles();
                pw.println(lists.size());	// block chain number
            	for(int j = 0; j < lists.size(); j++) {
            		pw.println(lists.get(j));	// file name
            		ArrayList<String> list = blockChainServer.getChain(lists.get(j));
            		for(int i = 0; i < list.size(); i++) {
            			if(!list.get(i).contains("logView.jsp"))
            				pw.println(list.get(i));
            		}
            		pw.flush();
            	}
        		
        		System.out.println(getTime() + " to Client > all block chain");
            }
            
            while(true) {
            	if((input = br.readLine()).equals("exit"))
            		break;
            	
            	if(input.equals("update")) {
            		Set<String> isUpdate = blockChainServer.getUpdateFile();
            		pw.println(isUpdate.size());	// update chain number
            		for(String file : isUpdate) {
            			pw.println(file);	// update chain name
            			ArrayList<String> list = blockChainServer.getChain(file);
                		for(int i = 0; i < list.size(); i++) {
                			if(!list.get(i).contains("logView.jsp"))
                				pw.println(list.get(i));
                		}
                		pw.flush();
            		}
            		System.out.println(getTime() + " to Client > update block chain");
            	}
            }
            
    		

		} catch (Exception e) {
            e.printStackTrace();
        } finally {
        	try {
				client.close();
			} catch (IOException e) {
				System.out.println(getTime() + "Client close error");
			}
        }
	}
	
	public String getTime() {
        String threadName = Thread.currentThread().getName();
        SimpleDateFormat f = new SimpleDateFormat("[hh:mm:ss]");
        return f.format(new Date()) + threadName;
    }
}

public class blockChainServer {

	static class fileInfo{
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
	
	static private RSA rsa = new RSA();
	static private ArrayList<fileInfo> files = new ArrayList<>();
	static private ArrayList<ArrayList<block>> chain = new ArrayList<ArrayList<block>>();
	static private Set<String> isUpdate = new HashSet<>();
	static public void main(String[] args) throws Exception {
		rsa.setKey();
		blockChain();
		
		ServerSocket server = new ServerSocket(6000);
		while(true) {
			Socket client = server.accept();
			Sockets sockets = new Sockets(client);
			sockets.start();
		}
	}
	
	static private void blockChain() throws Exception {
		int sleepSec = 60;
		
		ArrayList<String> files = blockDAO.readAllFile();
		for(int i = 0; i < files.size(); i++) {
			updateChain(files.get(i));
		}

		final ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
		exec.scheduleAtFixedRate(new Runnable(){
			public void run(){
				try {
					isUpdate.clear();
					ArrayList<String> files = readUpdateFile();
					for(int i = 0; i < files.size(); i++) {
						updateChain(files.get(i));
					}
				} catch (Exception e) {
					e.printStackTrace();
					exec.shutdown();
				}
			}
		}, 0, sleepSec, TimeUnit.SECONDS);
	}
	
	static public void updateChain(String file) throws Exception {
		int index = getIndex(file);
		if(index == -1)
			index = newChain(file);
		
		if(index != -1) {
			String str = "";
			ArrayList<String> Line = blockDAO.readLogFile_server(file);
			for(int i = files.get(index).lastIndex; i < Line.size(); i++)
				str += Line.get(i) + "\n";
			if(!str.equals("")) {
				isUpdate.add(files.get(index).name);
				chain.get(index).add(new block(sign(index), str));	// create block
				files.get(index).setLastIndex(Line.size());
				System.out.println("[updateChain] " + files.get(index).name + " - create new block[" + (chain.get(index).size()-1) + "]");
			}
		}
	}

	static private int newChain(String file) {
		int index = files.size();
		files.add(new fileInfo(file, 0));
		chain.add(new ArrayList<block>());
		chain.get(index).add(new block("START", file+"\n"));
		System.out.println("[newChain] create new chain - " + files.get(index).name);
		return index;
	}
	
	static ArrayList<String> getChain(String file) throws NumberFormatException, Exception{
		int index = getIndex(file);
		if(index != -1) {
			ArrayList<block> b = chain.get(index);
			ArrayList<String> str = new ArrayList<>();

			for(int i = 1; i < b.size(); i++){
				int dec = Integer.parseInt(rsa.decrypt(b.get(i).getSign(), rsa.getPublicKey()));
				int hash = b.get(i-1).hashCode();
				if(dec != hash){	// verification
					str.add("[hashcode error] " + file);
					break;
				}
				else {
					//System.out.println("[chain content] " + b.get(i).content);
					str.add(b.get(i).getContent());
				}
			}
			//System.out.println("[getChain] get block chain - " + file);
			return str;
		}
		
		System.out.println("[getChain] NOT EXIST block chain - " + file);
		return null;
	}
	
	static ArrayList<String> getFiles() {
		ArrayList<String> file = new ArrayList<>();
		for(int i = 0; i < files.size(); i++)
			file.add(files.get(i).name);
		return file;
	}
	
	static Set<String> getUpdateFile() {
		return isUpdate;
	}
	
	static private String sign(int index) throws Exception {
		int size = chain.get(index).size();
		block b = chain.get(index).get(size-1);
		return rsa.encrypt(b.hashCode() + "", rsa.getPrivateKey());
	}
	
	static int getIndex(String name) {
		for(int i = 0; i < files.size(); i++) {
			if(files.get(i).name.equals(name)) {
				return i;
			}
		}
		return -1;
	}

	static private ArrayList<String> readUpdateFile(){
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
