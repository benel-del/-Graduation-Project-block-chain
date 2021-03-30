package file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
import javax.servlet.http.HttpSession;

@WebServlet("/fileDownload")
public class FileDownload extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public FileDownload() {
		super();
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=utf-8");
		try {
			int maxSize = 1024 *1024 *10;
			String file = request.getParameter("file");
			String sDownPath = getServletContext().getRealPath("/uploadFile");
			String sFilePath = sDownPath + "/" + file;
			System.out.println(sFilePath);
			File outFile = new File(sFilePath);
			String userAgent = request.getHeader("User-Agent");

			String sEncoding = new String(file.getBytes("utf-8"), "8859_1");
			sEncoding = URLEncoder.encode(sEncoding, "utf-8");
			String sMimeType = getServletContext().getMimeType(sFilePath);
			if(sMimeType == null)
				sMimeType = "application.octec-stream";
			response.setContentType(sMimeType);
			//response.setHeader("Content-Disposition", "attachment;filename="+sEncoding);
			response.setHeader("Content-Disposition","attachment;filename=\"" +sEncoding+"\";");
			
			FileInputStream fis = new FileInputStream(outFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			ServletOutputStream sos = response.getOutputStream();
			BufferedOutputStream bos = new BufferedOutputStream(sos);
			
			byte[] temp = new byte[maxSize];
			int numRead = 0;
			while((numRead = bis.read(temp))!=-1){
				bos.write(temp, 0, numRead);
				bos.flush();
			}
			  
			if(bos != null) bos.close();
			if(bis != null) bis.close();
			if(sos != null) sos.close();
			if(fis != null) fis.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
