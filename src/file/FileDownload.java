package file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/fileDownload")
public class FileDownload extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public FileDownload() {
		super();
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=utf-8");
		try {
			//out.clear();
			//out = pageContext.pushBody();
			
			int maxSize = 1024 *1024 *10;
			String file = request.getParameter("file");
			String sDownPath = getServletContext().getRealPath("/uploadFile");
			String sFilePath = sDownPath + "/" + file;
			
			File outputFile = new File(sFilePath);
			FileInputStream in = new FileInputStream(outputFile);
			byte[] temp = new byte[maxSize];
			
			String sMimeType = getServletContext().getMimeType(sFilePath);
			if(sMimeType == null)
				sMimeType = "application.octec-stream";
			response.setContentType(sMimeType);
			
			String sEncoding = new String(file.getBytes("utf-8"), "8859_1");
			sEncoding = URLEncoder.encode(sEncoding, "utf-8");
			
			response.setHeader("Content-Disposition", "attachment;filename="+sEncoding);
			ServletOutputStream out2 = response.getOutputStream();
			
			int numRead = 0;
			while((numRead = in.read(temp, 0, temp.length)) != -1)
				out2.write(temp, 0, numRead);
				
			out2.flush();
			out2.close();
			in.close();	
						
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
