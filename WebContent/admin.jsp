<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.io.PrintWriter" %> 
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<script src="https://code.jquery.com/jquery-3.5.1.min.js"></script>
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
	<div class="admin_page">
  		<div class="admin_header">
  			<h1>관리자 로그인</h1>
  			<span>관리자 이외에는 접근을 금지합니다.</span>
   		</div>

		<div class="admin_form">
   		<table>
	   		<tbody>
	       		<tr>
	       			<td>아이디</td>
	       			<td><input type="text" placeholder="ID" id="id" maxlength="15" tabindex="1"></td>
	       			<td rowspan=2><button type="button" class="admin_submit-btn" id="submit">login</button></td>
      			</tr>
      			<tr>
      				<td>패스워드</td>
      				<td><input type="password" placeholder="Password" id="pw" maxlength="15" tabindex="2"></td>
  				</tr>
  				</tbody>
   		</table>
   		</div>
	</div>
	<script>
		$(function() {
		    $('#submit').on('click', function() {
	            $.ajax({
	                    url: "<%=request.getContextPath()%>/login",
	                    traditional:true,
	                    method: "POST",
	                    data: {
	                            id:$('#id').val(),
	                            pw:$('#pw').val()
	                            },
	                    dataType:"text"
	            })
	            .done(function(data) {
	            	if(data == "loginFail"){
	            		alert("로그인에 실패하였습니다.");
	            	}
	            	else if(data == "NoID"){
	            		alert("존재하지 않는 아이디입니다.");
	            	}
	            	else{
	            		window.location.href = "logsView.jsp";
	            	}
				})
			})
		})
	</script>
</body>
</html>