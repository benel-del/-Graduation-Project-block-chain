package blockChain;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@WebServlet("/access")
public class ServerDao extends HttpServlet {
	static final long serialVersionUID = 1L;
	static UserServer server;
	String[] option;
	
	ServerDao(){
		super();
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=utf-8");
		HttpSession session = request.getSession(true);
		String id = (String) session.getAttribute("userID");
		String pw = (String) session.getAttribute("userPW");
		server = new UserServer(id, pw);
		
		PrintWriter out = response.getWriter();
		JSONObject json = new JSONObject();
		String url = request.getParameter("name");
		if(url.equals("logView")) {
			String fileName = request.getParameter("file");
			option = request.getParameterValues("option");
			ArrayList<block> b = server.getChain(fileName);
			if(b != null) {
				String[] splitBlock;
				for(int i = 1; i < b.size(); i++){
					String str[] = b.get(i).getContent().split("\n");
					String state = b.get(i).getState();
					for(int j = 0; j < str.length; j++) {
						splitBlock = str[j].split("\\|");
						if (state.equals("Secure blockchain"))
							json = addJson(splitBlock, "0");
						else if(state.equals("SERVER Verification error"))
							json = addJson(splitBlock, "1");
						else if(state.equals("Different from local file"))
							json = addJson(splitBlock, "2");
						else if(state.equals("LOCAL Verification error"))	// ???
							json = addJson(splitBlock, "3");
					}
				}
			}
		}
		else if(url.equals("logsView")) {
			String chart = request.getParameter("chart");
			try {
				if (chart.equals("day"))
					json = countConnDay(); // ADD
				else if (chart.equals("month"))
					json = countConnMonth();
				else if (chart.equals("time"))
					json = countConnTime();
				else if (chart.equals("load"))
					json = countLoad();
				else if (chart.equals("err"))
					json = countStatus();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		out.print(json);
	}
	
	@SuppressWarnings("unchecked")
	JSONObject addJson(String[] splitLog, String code) {
    	JSONArray temp = new JSONArray();
    	JSONObject temp2 = new JSONObject();
    	for (int i=0; i< option.length; i++) {
    		temp.add(splitLog[Integer.parseInt(option[i])]);
    	}
    	temp2.put(code, temp);
    	//json.add(temp2);
    	return temp2;
    }
	
	@SuppressWarnings("unchecked")
	JSONObject countConnDay () throws Exception { // parameter, name change
		//File dir = new File("/usr/local/apache-tomcat-9.0.41/logs"); // ADD
		ArrayList<String> filelist = server.getList();
		//String[] filelist = dir.list(); // ADD
		String[] temp; // for file content split
		JSONObject jsObj = new JSONObject();
		String[] filename; // ADD
		int day = (filelist.size()>6) ? 7 : filelist.size(); // size() -> length
		for (int d=day; d>0; d--) { // per day
			HashSet<String> hs = new HashSet<String>();
			ArrayList<String> f = server.getLog(filelist.get(filelist.size()-d));
			//FileReader filerd = new FileReader("/usr/local/apache-tomcat-9.0.41/logs/"+filelist[filelist.length-d]); // ADD
			//BufferedReader br = new BufferedReader(filerd); // ADD
			//ArrayList<String> f = new ArrayList<String>(); // ADD
			//String line = ""; // ADD
			//while ((line=br.readLine())!=null) f.add(line); // ADD
			for (int j=0; j<f.size(); j++) {
				temp = f.get(j).split("\\|");
				hs.add(temp[0]);
			}
//			jsObj.put(filelist.get(filelist.size()-d), hs.size());
			//filename = filelist[filelist.length-d].split("\\."); // ADD
			filename = filelist.get(filelist.size()-d).split("\\."); // ADD
			jsObj.put(filename[1], hs.size()); // ADD
		}
		return jsObj;
	}
	
	@SuppressWarnings("unchecked")
	JSONObject countConnMonth() throws Exception {
		Calendar cal = Calendar.getInstance();
		int recentM = cal.get(Calendar.MONTH)+1;
		int recentY = cal.get(Calendar.YEAR);
		//File dir = new File("/usr/local/apache-tomcat-9.0.41/logs/");
		//String[] filelist = dir.list();
		ArrayList<String> filelist = server.getList();
		String[] temp; // for file name split
		String[] temp2; // for file name split
		JSONObject jsObj = new JSONObject();
		ArrayList<String> monthList[] = new ArrayList[5];
		for (int i=0; i<5; i++) monthList[i]= new ArrayList<String>(); // reset
		for (int i=0; i<filelist.size(); i++) {
			temp = filelist.get(i).split("\\.");
			temp2 = temp[1].split("-");
			int index=0;
			if ((index=(recentY-Integer.parseInt(temp2[0]))*12+recentM-Integer.parseInt(temp2[1]))<6) {
				monthList[index].add(filelist.get(i));
			}
		}
		for (int m=4; m>=0; m--) { // per month
			HashSet<String> hs = new HashSet<String>();
			for (int j = 0; j<monthList[m].size(); j++) { // per log file
				ArrayList<String> f = server.getLog(monthList[m].get(j));
				//FileReader filerd = new FileReader("/usr/local/apache-tomcat-9.0.41/logs/"+monthList[m].get(j));
				//BufferedReader br = new BufferedReader(filerd);
				//ArrayList<String> f = new ArrayList<String>();
				//String line = ""; // ADD
				//while ((line=br.readLine())!=null) f.add(line); // reset
				for (int k=0; k<f.size(); k++) {
					temp = f.get(k).split("\\|");
					hs.add(temp[0]);
				}
			}
			String monthName = (recentM<=m ? (recentY-1)+"."+(recentM+12-m) : recentY+"."+(recentM-m));
			jsObj.put(monthName, hs.size());
		}
		// monthList[1].get();
		return jsObj;
	}

	@SuppressWarnings("unchecked")
	JSONObject countConnTime() throws Exception {
		// 0:0:0:0:0:0:0:1|127.0.0.1|11157|HTTP/1.1|GET|[19/Mar/2021:17:13:51 +0900]|200|-|/
		Calendar cal = Calendar.getInstance();
		int recentD = cal.get(Calendar.DAY_OF_MONTH);
		int recentM = cal.get(Calendar.MONTH)+1;
		int recentY = cal.get(Calendar.YEAR);
		//File dir = new File("/usr/local/apache-tomcat-9.0.41/logs/");
		//String[] filenames = dir.list();
		ArrayList<String> filelist = server.getList();
		JSONObject jsObj = new JSONObject();
		int[] countTime = new int[24]; 
		String[] temp; // for log line split
		for (int i = 0; i < filelist.size(); i++) {
		    if (filelist.get(i).equals("log_"+recentY+String.format("%02d", recentM)+recentD)) {
				ArrayList<String> f = server.getLog(filelist.get(i));
				for (int j=0; j<f.size(); j++) {
					if (f.get(i).contains("logView.jsp")) {
						temp = f.get(i).split("\\:");
						countTime[Integer.parseInt(temp[8])]++;
					}
				}
				//FileReader filerd = new FileReader("/usr/local/apache-tomcat-9.0.41/logs/localhost_access_log."+recentY+"-"+String.format("%02d", recentM)+"-"+recentD+".txt"); // ADD
				//BufferedReader br = new BufferedReader(filerd); // ADD
				//while ((line=br.readLine())!=null) {
				//	if (line.contains("logView.jsp")) {
				//		temp = line.split("\\:");
				//		countTime[Integer.parseInt(temp[8])]++;
				//	}
				//}
		    }
		}
		countTime[1]+=countTime[0]; // 0h is contained 0-2h
		for (int i=1; i<24; i++) { // divide by 3 hour e.g. 0-2h, 3-5h
			if (i%3==2) jsObj.put((i/3)+"", countTime[i]);
			else countTime[i+1] += countTime[i];
		}
		return jsObj;
	}
	
	@SuppressWarnings("unchecked")
	JSONObject countLoad() throws Exception {
		//File dir = new File("/usr/local/apache-tomcat-9.0.41/logs");
		//String[] filelist = dir.list(); // ADD
		ArrayList<String> filelist = server.getList();
		//String[] temp; // for file content split
		JSONObject jsObj = new JSONObject();
		JSONObject jtemp1 = new JSONObject();
		JSONObject jtemp2 = new JSONObject();
		String[] filename;
		int day = (filelist.size()>6) ? 7 : filelist.size();
		for (int d=day; d>0; d--) { // per day
			int[] count = new int[2];
			//FileReader filerd = new FileReader("/usr/local/apache-tomcat-9.0.41/logs/"+filelist[filelist.length-d]);
			///BufferedReader br = new BufferedReader(filerd);
			filename = filelist.get(filelist.size()-d).split("\\.");
			//String line = ""; // ADD
			//while ((line=br.readLine())!=null) {
			//	if (line.contains("upload.jsp")) count[0]++;
			//	else if (line.contains("download.jsp")) count[1]++;
			//}
			ArrayList<String> f = server.getLog(filelist.get(filelist.size()-d));
			for(int i = 0; i < f.size(); i++) {
				if(f.get(i).contains("Upload"))	count[0]++;
				else if(f.get(i).contains("Download"))	count[1]++;
			}
			jtemp1.put(filename[1], count[0]);
			jtemp2.put(filename[1], count[1]);
		}
		jsObj.put("upload", jtemp1);
		jsObj.put("download",jtemp2);
		return jsObj;
	}
	
	@SuppressWarnings("unchecked")
	JSONObject countStatus() throws Exception {
		JSONObject jsObj = new JSONObject();
		//JSONObject jtemp1 = new JSONObject();
		//JSONObject jtemp2 = new JSONObject();
		//JSONObject jtemp3 = new JSONObject();
		//JSONObject jtemp4 = new JSONObject();
		ArrayList<String> filelist = server.getList();
		String[] temp; // for file content split
		String[] filename;
		ArrayList<String> status = new ArrayList<String>();
		int day = (status.size()>6) ? 7 : status.size(); // size() -> length
		for (int d=day; d>0; d--) { // per day
			HashSet<String> hs = new HashSet<String>();
			ArrayList<String> f = server.getLog(filelist.get(filelist.size()-d));
//			BufferedReader br = new BufferedReader(filerd);
//			ArrayList<String> f = new ArrayList<String>();
//			String line = ""; // ADD
//			while ((line=br.readLine())!=null) f.add(line);
			for (int j=0; j<f.size(); j++) {
				temp = f.get(j).split("\\|");
				hs.add(temp[0]);
			}
//			jsObj.put(filelist.get(filelist.size()-i), hs.size());
//			filename = filelist[filelist.length-d].split("\\.");
			filename = filelist.get(filelist.size()-d).split("\\.");
			jsObj.put(filename[1], hs.size());
		}
		return jsObj;
	}
}
