package blockChain;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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
		
		HttpSession session = request.getSession(true);
		String id = (String) session.getAttribute("userID");
		String pw = (String) session.getAttribute("userPW");
		try {
			blockChain server = new blockChain(id, pw);
			ArrayList<block> b = server.getChain(fileName);

			if(b != null) {
				String[] splitBlock;
				json = new JSONArray();
				for(int i = 1; i < b.size(); i++){
					String str[] = b.get(i).getContent().split("\n");
					String state = b.get(i).getState();
					for(int j = 0; j < str.length; j++) {
						splitBlock = splitLog(str[j]);
						if (state.equals("Secure blockchain"))
							addJson(splitBlock, "0");
						else if(state.equals("Verification error"))
							addJson(splitBlock, "1");
						else if(state.equals("Different from other blockchains"))
							addJson(splitBlock, "2");
						else if(state.equals("No comparison objects"))
							addJson(splitBlock, "3");
					}
				}
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
