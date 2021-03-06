package blockChain;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

//import blockChain.block;
//import blockChain.accessedByJSP;

/**
 * Servlet implementation class Log
 */
@WebServlet("/Log")
public class Log extends HttpServlet {
	static String[] option;
	static JSONArray json = new JSONArray();
	private static final long serialVersionUID = 1L;
       
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
		accessedByJSP block;
		try {
			block = new accessedByJSP();
			ArrayList<block> content = block.getChain(fileName);
			ArrayList<String> originalFile = block.readLogFile(fileName);

			String[] line = new String[2];
			String[] splitServ = {""};
			String[] splitBlock;
			String[] str;
			json = new JSONArray();
			int k = 0;
			for(int i = 0; i < content.size(); i++){
				str = content.get(i).content.split("\n");

				for(int j = 0; j < str.length; j++){
					splitBlock = splitLog(str[j]);

					// splitServ 설정 (blockchain 크기가 더 클 때 대비)
					if(k > originalFile.size()) {
						addJson(splitBlock, "4");
					}
					else {
						splitServ = splitLog(originalFile.get(k++));
					}

					if (compareLog(splitServ, splitBlock, option)) {
						addJson(splitServ, "0");
					}

					// blockchain과 server log가 다를 때
					else {
						addJson(splitServ, "1");
						addJson(splitBlock, "2");
					}
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

}
