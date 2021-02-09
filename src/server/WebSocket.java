package server;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.spec.SecretKeySpec;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.server.ServerEndpoint;

import security.RSA;
import db.logDAO;

@ServerEndpoint("/websockets")
public class WebSocket {
	private logDAO log = new logDAO();
	private String path = "C:\\JSP\\test.txt";
	private SecretKeySpec AES = null;
	private String client = null;
	private String server = null;
	//private String nextServer = null;


	public WebSocket() throws NoSuchAlgorithmException, InvalidKeySpecException{
		if(log.getKey("server") == null) {
			RSA rsa = new RSA();
			log.register("server", RSA.KeyToStr(rsa.getPublicKey()));
			log.register_private("server", RSA.KeyToStr(rsa.getPrivateKey()));
		}
	}
	
	@OnOpen
	public void handleOpen() {
		System.out.println("client is now connected...");
	}
	
	@OnMessage
	public String handleMessage(String message) throws Exception {
		String msg = "";
		if(client == null) {
			rsaKey(message);
			msg = "OK";
		}
		else if(AES == null) {
			msg = verify(message);
		}
		else if(message.equals("recieve")){
			msg = sendFile();
			System.out.println("send msg to Client");
		}
		return msg;
	}
	
	@OnClose
	public void handleClose() {
		System.out.println("client is now disconnected...");
	}
	
	@OnError
	public void handleError(Throwable t) {
		t.printStackTrace();
	}

	private void rsaKey(String message) throws Exception {
		//RSA rsa = new RSA();
		client = "client" + message;
		//server = "server" + (Integer.parseInt(message)-1);
		server = "server";
		//nextServer = "server" + (Integer.parseInt(message));
		//log.register(nextServer, RSA.KeyToStr(rsa.getPublicKey()));
		//log.register_private(nextServer, RSA.KeyToStr(rsa.getPrivateKey()));
	}
	
	private String verify(String message) throws Exception {
		String str[] = extract(message);
		String signature = str[1];	// Kc-(H(k))
		str = extract(RSA.decrypt(str[0], log.getPrivateKey(server)));	// k, H(signature)
		if(signature.hashCode() == Integer.parseInt(str[1])) {
			if(str[0].hashCode() == Integer.parseInt(RSA.decrypt(signature, log.getKey(client)))) {
				AES = security.AES.setKey(str[0]);
				System.out.println("recieve AES key from Client");
				return "";
			}
			else
				System.out.println("recieve AES key from Client, but the key is not correct");
		}
		else
			System.out.println("recieve message from Client, but the message is not correct");
		return "disconnect";
	}
	
	private String sendFile() throws Exception {
		int index = 0;
		String tmp = "";
		ArrayList<String> Line = readText(path);
		while(Line.size() > index) {
			tmp += Line.get(index++);
		}
		String msg = security.AES.ByteToStr(security.AES.encrypt(tmp, AES));
		msg += "||" + (tmp + security.AES.getKey_st(AES)).hashCode();
		//return RSA.encrypt(msg, log.getKey(client));
		return msg;
	}
	
	private ArrayList<String> readText(String path) {
		ArrayList<String> Line = new ArrayList<>();
		try {
			File file = new File(path);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufReader = new BufferedReader(fileReader);
			Line.add("START file: " + path + "\n");
			String line = "";
			while((line = bufReader.readLine()) != null) {
				// block chain check
				
				Line.add(line + "\n");
			}
			Line.add("END file\n");
		}catch(FileNotFoundException e) {
			Line.add("Error:: file not found - " + path);
		}catch(IOException e) {
			System.out.println(e);
		}
		return Line;
	}
	
	private String[] extract(String st){
		int idx = st.indexOf("||");
        if(idx == -1)
            return null;
        String str[] = new String[2];
        str[0] = st.substring(0, idx);
        str[1] = st.substring(idx + 2);
        return str;
    }
}