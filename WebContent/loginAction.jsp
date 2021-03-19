<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="security.adminDAO" %>
<%@ page import="blockChain.blockChain" %>
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

	adminDAO loginDB = new adminDAO(login.getUserID(), login.getUserPassword());
	
	if(loginDB.isUser() == 1){
		if(loginDB.connect() == 1){
			session.setAttribute("userID", login.getUserID());
						
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
	}
	else{
		PrintWriter script = response.getWriter();
		script.println("<script>");
		script.println("alert('존재하지 않는 아이디입니다.')");
		script.println("history.back()");
		script.println("</script>");
	}
%>