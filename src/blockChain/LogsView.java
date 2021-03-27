package project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

/**
 * Servlet implementation class LogsView
 */
// @WebServlet("/LogsView")
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PrintWriter out = response.getWriter();
		out.print(json);
	}

	private JSONObject countConnDay () throws Exception { // parameter, name change
		File dir = new File("/usr/local/apache-tomcat-9.0.41/logs"); // ADD
		String[] filelist = dir.list(); // ADD
		String[] temp; // for file content split
		JSONObject jsObj = new JSONObject();
		String[] filename; // ADD
		int day = (filelist.length>6) ? 7 : filelist.length; // size() -> length
		for (int d=day; d>0; d--) { // per day
			HashSet<String> hs = new HashSet<String>();
//			ArrayList<String> f = server.getLog(filelist.get(filelist.size()-d));
			FileReader filerd = new FileReader("/usr/local/apache-tomcat-9.0.41/logs/"+filelist[filelist.length-d]); // ADD
			BufferedReader br = new BufferedReader(filerd); // ADD
			ArrayList<String> f = new ArrayList<String>(); // ADD
			String line = ""; // ADD
			while ((line=br.readLine())!=null) f.add(line); // ADD
			for (int j=0; j<f.size(); j++) {
				temp = f.get(j).split("\\|");
				hs.add(temp[0]);
			}
//			jsObj.put(filelist.get(filelist.size()-i), hs.size());
			filename = filelist[filelist.length-d].split("\\."); // ADD
			jsObj.put(filename[1], hs.size()); // ADD
		}
		return jsObj;
	}
	
	private JSONObject countConnMonth() throws Exception {
		Calendar cal = Calendar.getInstance();
		int recentM = cal.get(Calendar.MONTH)+1;
		int recentY = cal.get(Calendar.YEAR);
		File dir = new File("/usr/local/apache-tomcat-9.0.41/logs/");
		String[] filelist = dir.list();
		String[] temp; // for file name split
		String[] temp2; // for file name split
		JSONObject jsObj = new JSONObject();
		ArrayList<String> monthList[] = new ArrayList[5];
		for (int i=0; i<5; i++) monthList[i]= new ArrayList<String>(); // reset
		for (int i=0; i<filelist.length; i++) {
			temp = filelist[i].split("\\.");
			temp2 = temp[1].split("-");
			int index=0;
			if ((index=(recentY-Integer.parseInt(temp2[0]))*12+recentM-Integer.parseInt(temp2[1]))<6) {
				monthList[index].add(filelist[i]);
			}
		}
		for (int m=4; m>=0; m--) { // per month
			HashSet<String> hs = new HashSet<String>();
			for (int j = 0; j<monthList[m].size(); j++) { // per log file
				FileReader filerd = new FileReader("/usr/local/apache-tomcat-9.0.41/logs/"+monthList[m].get(j));
				BufferedReader br = new BufferedReader(filerd);
				ArrayList<String> f = new ArrayList<String>();
				String line = ""; // ADD
				while ((line=br.readLine())!=null) f.add(line); // reset
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
	
	private JSONObject countConnTime() throws Exception {
		// 0:0:0:0:0:0:0:1|127.0.0.1|11157|HTTP/1.1|GET|[19/Mar/2021:17:13:51 +0900]|200|-|/
		Calendar cal = Calendar.getInstance();
		File dir = new File("/usr/local/apache-tomcat-9.0.41/logs/");
		String[] filenames = dir.list();
		JSONObject jsObj = new JSONObject();
		int[] countTime = new int[24];
		String line = ""; // 
		String[] temp; // for log line split
		for (int i = 0; i < filenames.length; i++) {
		    if (filenames[i].equals("localhost_access_log.2021-03-26.txt")) {
				HashSet<String> hs = new HashSet<String>();
				FileReader filerd = new FileReader("/usr/local/apache-tomcat-9.0.41/logs/localhost_access_log."+cal.get(Calendar.YEAR)+"-"+String.format("%02d", cal.get(Calendar.MONTH) + 1)+"-"+cal.get(Calendar.DAY_OF_MONTH)+".txt"); // ADD
				BufferedReader br = new BufferedReader(filerd); // ADD
				while ((line=br.readLine())!=null) {
					if (line.contains("logView.jsp")) {
						temp = line.split("\\:");
						countTime[Integer.parseInt(temp[8])]++;
					}
				}
		    }
		}
		countTime[1]+=countTime[0];
		for (int i=1; i<24; i++) {
			if (i%3==2) jsObj.put((i/3)+"", countTime[i]);
			else countTime[i+1] += countTime[i];
		}
		return jsObj;
	}
	
	private JSONObject countLoad() throws Exception {
		File dir = new File("/usr/local/apache-tomcat-9.0.41/logs");
		String[] filelist = dir.list(); // ADD
		String[] temp; // for file content split
		JSONObject jsObj = new JSONObject();
		JSONObject jtemp1 = new JSONObject();
		JSONObject jtemp2 = new JSONObject();
		String[] filename;
		int day = (filelist.length>6) ? 7 : filelist.length;
		for (int d=day; d>0; d--) { // per day
			int[] count = new int[2];
			FileReader filerd = new FileReader("/usr/local/apache-tomcat-9.0.41/logs/"+filelist[filelist.length-d]);
			BufferedReader br = new BufferedReader(filerd);
			filename = filelist[filelist.length-d].split("\\.");
			String line = ""; // ADD
			while ((line=br.readLine())!=null) {
				if (line.contains("upload.jsp")) count[0]++;
				else if (line.contains("download.jsp")) count[1]++;
			}
			jtemp1.put(filename[1], count[0]);
			jtemp2.put(filename[1], count[1]);
		}
		jsObj.put("upload", jtemp1);
		jsObj.put("download",jtemp2);
		return jsObj;
	}
	
	private JSONObject countStatus() throws Exception {
		JSONObject jsObj = new JSONObject();
		JSONObject jtemp1 = new JSONObject();
		JSONObject jtemp2 = new JSONObject();
		JSONObject jtemp3 = new JSONObject();
		JSONObject jtemp4 = new JSONObject();
		ArrayList<String> status = new ArrayList<String>();
		int day = (status.size()>6) ? 7 : status.size(); // size() -> length
//		for (int d=day; d>0; d--) { // per day
//			HashSet<String> hs = new HashSet<String>();
//			BufferedReader br = new BufferedReader(filerd);
//			ArrayList<String> f = new ArrayList<String>();
//			String line = ""; // ADD
//			while ((line=br.readLine())!=null) f.add(line);
//			for (int j=0; j<f.size(); j++) {
//				hs.add(temp[0]);
//			}a
////			jsObj.put(filelist.get(filelist.size()-i), hs.size());
//			filename = filelist[filelist.length-d].split("\\.");
//			jsObj.put(filename[1], hs.size());
//		}
		return jsObj;
	}
}
