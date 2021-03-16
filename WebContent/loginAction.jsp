<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="blockChain.socketClient" %>
<% request.setCharacterEncoding("UTF-8"); %>
<jsp:useBean id="login" class="security.login" scope="page" />
<jsp:setProperty name="login" property="userID" />
<jsp:setProperty name="login" property="userPassword" />
<%
	if(session.getAttribute("userID") != null){	// 로그인 한 사람 접근 불가
		PrintWriter script=response.getWriter();
		script.println("<script>");
		script.println("location.href = 'logView.jsp'");
		script.println("</script>");
	}
	
	String adminID = "AdmIn";
	String adminPW = "aDMiN";
	
	if(adminID.equals(login.getUserID()) && adminPW.equals(login.getUserPassword())){
		socketClient socket = new socketClient();
		ArrayList<String> files = socket.getAllChainName();
		ArrayList<ArrayList<String>> chain = socket.getAllChainContent();
		
		session.setAttribute("userID", adminID);
		session.setAttribute("chainName", files);
		session.setAttribute("chainContent", chain);
		session.setMaxInactiveInterval(-1);
		
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