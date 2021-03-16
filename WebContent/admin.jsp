<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.io.PrintWriter" %> 
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<link rel="stylesheet" type="text/css" href="frame.css">
<title>admin login page</title>
</head>
<body>
<%
	if(session.getAttribute("userID") != null){	// 로그인 한 사람 접근 불가
		PrintWriter script=response.getWriter();
		script.println("<script>");
		script.println("location.href = 'logView.jsp'");
		script.println("</script>");
	}
%>     
	<div class="login_page">
	  	<form method="post" action="loginAction.jsp">
	  		<div class="login_header">
	  			<h1>관리자 로그인</h1>
	  			<span>관리자 이외에는 접근을 금지합니다.</span>
	   		</div>
	
			<div class="login_form">
	   		<table>
		   		<tbody>
		       		<tr>
		       			<td>아이디</td>
		       			<td><input type="text" placeholder="ID" name="userID" maxlength="15" tabindex="1"></td>
		       			<td rowspan=2><input type="submit" class="login_submit-btn" value="login"></td>
	      			</tr>
	      			<tr>
	      				<td>패스워드</td>
	      				<td><input type="password" placeholder="Password" name="userPassword" maxlength="15" tabindex="2"></td>
	  				</tr>
   				</tbody>           
	   		</table>
	   		</div>
		</form>
	</div>
</body>
</html>