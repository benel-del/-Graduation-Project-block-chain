<%@ page language="java" contentType="text/html; charset=UTF-8" 
	pageEncoding="UTF-8"%>
<%@ page import="java.io.PrintWriter" %>

<%
	session.invalidate();
		
	PrintWriter script = response.getWriter();
	script.println("<script>");
	script.println("location.href = 'admin.jsp'");
	script.println("</script>");
%>