package blockChain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Servlet implementation class LogsView
 */
@WebServlet("/LogsView")
public class LogsView extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LogsView() {
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
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=utf-8");
		String chart = request.getParameter("chart");
		JSONObject json = new JSONObject();
		
		HttpSession session = request.getSession(true);
		String id = (String) session.getAttribute("userID");
		String pw = (String) session.getAttribute("userPW");
		try {
			UserServer server = new UserServer(id, pw);
			json = countConn(server);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PrintWriter out = response.getWriter();
		out.print(json);
	}

	@SuppressWarnings("unchecked")
	private JSONObject countConn (UserServer server) {
		ArrayList<String> filelist = server.getList();
		String[] temp;
		JSONObject jsObj = new JSONObject();
		int day = (filelist.size()>6) ? 7 : filelist.size();
		for (int i=day; i>0; i--) { // one day
			HashSet<String> hs = new HashSet<String>();
			ArrayList<String> f = server.getLog(filelist.get(filelist.size()-i));
			for (int j=0; j<f.size(); j++) {
				temp = f.get(j).split("\\|");
				hs.add(temp[0]);
			}
			jsObj.put(filelist.get(filelist.size()-i), hs.size());
		}
		return jsObj;
	}

}
