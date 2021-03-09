<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.net.URLEncoder"%>
<%@page import="java.io.*" %>
<%@page import="java.util.ArrayList" %>
<%@page import="java.util.Enumeration" %>
<%@page import="com.oreilly.servlet.multipart.DefaultFileRenamePolicy"%>
<%@page import="com.oreilly.servlet.MultipartRequest"%>
<%@page import="file.fileDAO" %>
<%@page import="security.AES"  %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
</head>
<%
	PrintWriter script = response.getWriter();
	request.setCharacterEncoding("UTF-8");
	String uploadPath = getServletContext().getRealPath("/uploadFile");
	int maxSize = 1024 *1024 *10;// 한번에 올릴 수 있는 파일 용량 : 10M로 제한
	String newName = "";
	String fileName = "";
	String option = "";
	String pw = "";
	MultipartRequest multi = null;
	try{
		multi = new MultipartRequest(request, uploadPath, maxSize, "utf-8", new DefaultFileRenamePolicy());
		option = multi.getParameter("option");
		pw = multi.getParameter("password");
		Enumeration files = multi.getFileNames();
		if(files.hasMoreElements()){
			String name = (String)files.nextElement();	// input type="file"' name :: fileUpload
			fileName = multi.getFilesystemName(name);
			String fileType = multi.getContentType(name);
			File file = multi.getFile(name);
			long fileSize = file.length();

			if(option.equals("decrypt") && fileName.substring(fileName.length()-8, fileName.length()-4).equals("_enc") == false){
				script.println("<script>");
				script.println("alert('decrypt file type error!')");
				script.println("location.href = 'index.jsp'");
				script.println("</script>");
			}
			else{
				fileDAO f = new fileDAO();
				
				//action 
				newName = fileName.substring(0, fileName.length()-4);
				if(option.equals("encrypt"))
					newName += "_enc.txt";
				else if(option.equals("decrypt"))
					newName = newName.substring(0, newName.length()-4) + ".txt";
				
				String newPath = uploadPath + "/" + newName;
				File resultFile = new File(newPath);
				resultFile.createNewFile();
				System.out.println(newPath);
				FileWriter fw = new FileWriter(newPath);
	 
				int index = 0;
				String tmp = "";
				ArrayList<String> Line = f.read(fileName);
				while(Line.size() > index){
					tmp += Line.get(index++) + "\n";
				}
				
				AES aes = new AES();
				if(option.equals("encrypt"))
					tmp = aes.ByteToStr(aes.encrypt(tmp, aes.setKey(pw)));
				else if(option.equals("decrypt"))
					tmp = aes.decrypt(aes.StrToByte(tmp), aes.setKey(pw));
				fw.write(tmp);
				fw.close();
				
				//db upload
				f.insert(fileName, newName, pw, f.fileSize(fileSize), f.fileSize(resultFile.length()), option);
				
				script.println("<script>");
				script.println("location.href = 'index.jsp?file=" + URLEncoder.encode(fileName, "UTF-8") + "'");
				script.println("</script>");
			}
		}
	}catch(Exception e){
		script.println("<script>");
		script.println("alert('decrypt file type error!')");
		script.println("location.href = 'index.jsp'");
		script.println("</script>");
		e.printStackTrace();
	}

%>
</html>