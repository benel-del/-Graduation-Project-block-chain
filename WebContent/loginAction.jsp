<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.io.PrintWriter" %>
<% request.setCharacterEncoding("UTF-8"); %>
<jsp:useBean id="login" class="security.login" scope="page" />
<jsp:setProperty name="login" property="userID" />
<jsp:setProperty name="login" property="userPassword" />
<%
	String userID = null;
	if(session.getAttribute("userID") != null){
		userID = (String) session.getAttribute("userID");
	}
	if(userID != null){
		PrintWriter script=response.getWriter();
		script.println("<script>");
		script.println("location.href = 'logView.jsp'");
		script.println("</script>");
	}
	
	String adminID = "AdmIn";
	String adminPW = "aDMiN";
	
	if(adminID.equals(login.getUserID()) && adminPW.equals(login.getUserPassword())){
		session.setAttribute("userID", "v1e3er");
		PrintWriter script = response.getWriter();
		script.println("<script>");
		script.println("location.href = 'logView.jsp'");
		script.println("</script>");
	}
	else{
		PrintWriter script = response.getWriter();
		script.println("<script>");
		script.println("history.back()");
		script.println("</script>");
	}
%>