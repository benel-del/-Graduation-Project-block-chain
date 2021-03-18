<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
    <%@ page import="java.io.PrintWriter" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<link rel="stylesheet" type="text/css" href="frame.css">
<title>admin register page</title>
</head>
<body>
	<%
		if(session.getAttribute("userID") != null){
			PrintWriter script=response.getWriter();
			script.println("<script>");
			script.println("history.back()");
			script.println("</script>");
		}

	%>
	<div class="admin_page">
	  	<form method="post" action="registerAction.jsp">
	  		<div class="admin_header">
	  			<h1>관리자 등록</h1>
	  			<span>관리자 이외에는 접근을 금지합니다.</span>
	   		</div>
	
			<div class="admin_form">
	   		<table>
		   		<tbody>
		       		<tr>
		       			<td>아이디</td>
		       			<td><input type="text" placeholder="ID" name="userID" maxlength="15" tabindex="1"></td>
		       			<td rowspan=2><input type="submit" class="admin_submit-btn" value="register"></td>
	      			</tr>
	      			<tr>
	      				<td>패스워드</td>
	      				<td><input type="password" placeholder="Password" name="userPassword" maxlength="15" tabindex="2"></td>
	  				</tr>
   				</tbody>
	   		</table>
	   		</div>
	   		
	   		<div class="login">
	   			<a href="admin.jsp">관리자 로그인</a>
	   		</div>
		</form>
	</div>

</body>
</html>