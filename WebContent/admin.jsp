<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.io.PrintWriter" %> 
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>admin login page</title>
</head>
<body>
<%
	String userID = null;
	if(session.getAttribute("userID") != null){
		userID = (String) session.getAttribute("userID");
	}
	if(userID != null){	// 로그인 한 사람 접근 불가
       	PrintWriter script=response.getWriter();
		script.println("<script>");
		script.println("location.href = 'logView.jsp'");
		script.println("</script>");
	}
%>     
	<div class="login_page">
	  	<form method="post" action="loginAction.jsp">
	  		<div class="login_header">
	       		<a href="admin.jsp">LOGIN</a>
	   		</div>
	
	   		<div class="login_form">
	       		<input type="text" placeholder="ID" name="userID" maxlength="15">
	       		<br>
	       		<input type="password" placeholder="PW" name="userPassword" maxlength="15" />           
	   		</div>
	   
	   		<input type="submit" class="login_submit-btn" value="login" >
		</form>
	</div>
</body>
</html>