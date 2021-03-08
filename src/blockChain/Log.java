package blockChain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

/**
 * Servlet implementation class Log
 */
@WebServlet("/Log")
public class Log extends HttpServlet {
	static String[] option;
	static JSONArray json = new JSONArray();
	private static final long serialVersionUID = 1L;
	ArrayList<String> content;

	private class client extends WebSocketClient {
		private String file = "";
		private client(URI serverUri, String file) {
			super(serverUri);
			this.file = file;
		}
		@Override
		public void onOpen(ServerHandshake handshakedata) {
			send(file);
		    System.out.println("opened connection");
		    // if you plan to refuse connection based on ip or httpfields overload: onWebsocketHandshakeReceivedAsClient
		}

		@Override
		public void onMessage(String message) {
			String str[] = message.split("\n");
			for(int i = 0; i < str.length; i++)
				content.add(str[i]);
		}

		@Override
		public void onClose(int code, String reason, boolean remote) {
			// The codecodes are documented in class org.java_websocket.framing.CloseFrame
		    System.out.println(
		        "Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: "
		            + reason);
		}

		@Override
		public void onError(Exception ex) {
			ex.printStackTrace();
		    // if the error is fatal then onClose will be called additionally
		}
	}
   
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Log() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=utf-8");
		String fileName = request.getParameter("file");
		option = request.getParameterValues("option");

		try {
			WebSocketClient socket = new client(new URI("ws://localhost:8080/block/websockets"), fileName);
			socket.connect();
			ArrayList<String> originalFile = readLogFile(fileName);

			String[] line = new String[2];
			String[] splitServ = {""};
			String[] splitBlock;
			json = new JSONArray();
			int k = 0;
			for(int i = 0; i < content.size(); i++){
				splitBlock = splitLog(content.get(i));
				// splitServ �꽕�젙 (blockchain �겕湲곌� �뜑 �겢 �븣 ��鍮�)
				if(k > originalFile.size()) {
					addJson(splitBlock, "4");
				}
				else {
					splitServ = splitLog(originalFile.get(k++));
				}

				if (compareLog(splitServ, splitBlock, option)) {
					addJson(splitServ, "0");
				}

				// blockchain怨� server log媛� �떎瑜� �븣
				else {
					addJson(splitServ, "1");
					addJson(splitBlock, "2");
				}
			}
			while ((line[0]=originalFile.get(k++))!=null) {
				splitServ = splitLog(line[0]);
				addJson(splitServ, "3");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PrintWriter out = response.getWriter();
		out.print(json);
		//String j = json.t
		//String j = json.toJSONString();
		//System.out.println(j);
	}
	
	private String[] splitLog(String log) {
		String[] result = new String[3];
		result = log.split(" ");
		return result;
	}
	
    private boolean compareLog(String[] log1, String[] log2, String[] option) {
        if (log1.length != log2.length)
                return false;
        for (int i=0; i<option.length; i++) {
                if (!log1[Integer.parseInt(option[i])].equals(log2[Integer.parseInt(option[i])])) 
                        return false;
        }
        return true;
    }
    
    private void addJson(String[] splitLog, String code) {
    	JSONArray temp = new JSONArray();
    	JSONObject temp2 = new JSONObject();
    	for (int i=0; i<option.length; i++) {
    		temp.add(splitLog[Integer.parseInt(option[i])]);
    	}
    	temp2.put(code, temp);
    	json.add(temp2);
    }

    private ArrayList<String> readLogFile(String filename) {
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
}
