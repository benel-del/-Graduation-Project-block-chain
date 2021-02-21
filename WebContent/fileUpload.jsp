<%@ page language="java" contentType="text/html; charset=EUC-KR" pageEncoding="EUC-KR"%>
<%@page import="java.io.File" %>
<%@page import="java.util.Enumeration" %>
<%@page import="com.oreilly.servlet.multipart.DefaultFileRenamePolicy"%>
<%@page import="com.oreilly.servlet.MultipartRequest"%>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="file.fileDAO" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="EUC-KR">
<title>Insert title here</title>
</head>
<%
	request.setCharacterEncoding("UTF-8");
	String uploadPath = getServletContext().getRealPath("/uploadFile");
	System.out.println(uploadPath);
	int maxSize = 1024 *1024 *10;// 한번에 올릴 수 있는 파일 용량 : 10M로 제한
	String fileName = "";
	String originalName = "";
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
			originalName = multi.getOriginalFileName(name);
			String fileType = multi.getContentType(name);
			File file = multi.getFile(name);
			long fileSize = file.length();
			
			System.out.println(name + ": " + fileName);
			
			//db upload
			fileDAO f = new fileDAO();
			f.insert(originalName, fileName, pw, f.fileSize(fileSize), option);
			
			//action
			
		}
	}catch(Exception e){
		e.printStackTrace();
	}
	
	PrintWriter script = response.getWriter();
	script.println("<script>");
	script.println("location.href = 'index.jsp?file=" + fileName + "'");
	script.println("</script>");
%>
</html>