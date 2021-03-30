package file;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;

import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;

import crypto.AES;

@WebServlet("/fileUpload")
public class FileUpload extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public FileUpload() {
		super();
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=utf-8");
		try {
			String uploadPath = getServletContext().getRealPath("/uploadFile");
			int maxSize = 1024 *1024 *10;// �ѹ��� �ø� �� �ִ� ���� �뷮 : 10M�� ����
			MultipartRequest multi = new MultipartRequest(request, uploadPath, maxSize, "utf-8", new DefaultFileRenamePolicy());
			String option = multi.getParameter("option");
			String pw = multi.getParameter("password");
			Enumeration files = multi.getFileNames();
			JSONObject job = new JSONObject();
			
			PrintWriter out = response.getWriter();
			if(files.hasMoreElements()){
				String name = (String)files.nextElement();	// input type="file"' name :: fileUpload
				String fileName = multi.getFilesystemName(name);
				File file = multi.getFile(name);
				long fileSize = file.length();

				if(option.equals("decrypt") && fileName.substring(fileName.length()-8, fileName.length()-4).equals("_enc") == false){
					job.put("err", "type error");
					out.print(job);
				}
				else{
					FileDAO f = new FileDAO();
					
					// crypto
					String newName = fileName.substring(0, fileName.length()-4);
					if(option.equals("encrypt"))
						newName += "_enc.txt";
					else if(option.equals("decrypt"))
						newName = newName.substring(0, newName.length()-4) + ".txt";
					
					String newPath = uploadPath + "/" + newName;
					File resultFile = new File(newPath);
					resultFile.createNewFile();
					FileWriter fw = new FileWriter(resultFile);
				 
					int index = 0;
					String tmp = "";
					ArrayList<String> Line = f.read(fileName);
					while(Line.size() > index){
						tmp += Line.get(index++) + "\n";
					}
				
					job.put("originalFile", fileName);
					job.put("originalSize", f.fileSize(fileSize));
					job.put("newFile", newName);
					job.put("option", option);
					job.put("tmp", tmp.replaceAll("\\\\", "/").replaceAll("\"", "\'"));
					
					AES aes = new AES();
					if(option.equals("encrypt"))
						tmp = aes.ByteToStr(aes.encrypt(tmp, aes.setKey(pw)));
					else if(option.equals("decrypt"))
						tmp = aes.decrypt(aes.StrToByte(tmp), aes.setKey(pw));
					fw.write(tmp);
					fw.close();
					job.put("newSize", f.fileSize(resultFile.length()));
					job.put("tmp2", tmp.replaceAll("\\\\", "/").replaceAll("\"", "\'"));
					out.print(job);
			 
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
