<%@ page language="java" contentType="text/html; charset=EUC-KR" pageEncoding="UTF-8"%>
<%@page import="java.io.File" %>
<%@page import="java.io.FileInputStream"%>
<%@page import="java.net.URLEncoder"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="EUC-KR">
<title>Insert title here</title>
</head>
<%
	out.clear();
	out = pageContext.pushBody();
	
	int maxSize = 1024 *1024 *10;
	String file = request.getParameter("file");
	String savePath = "uploadFile";
	String sDownPath = getServletContext().getRealPath(savePath);
	String sFilePath = sDownPath + "\\" + file;
	
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
%>
</html>