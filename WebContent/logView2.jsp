<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.io.File" %>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>관리자 페이지</title>
	<script src="https://code.jquery.com/jquery-3.5.1.slim.min.js"></script>
</head>
<body>
<%
String path = "/usr/local/apache-tomcat-9.0.41/logs";
File dir = new File(path);
String[] files;
%>
	<script>
		$(function() {
			$("div.file").bind("change", function() {
				var radioVal = $(this).val();
				if (radioVal = "tomcat") {
					<% 
					files = dir.list((f, name)->name.startsWith("catalina."));
					for (int i=0; i<files.length; i++) {
						
					%>
					$(this).closest("div").siblings("div.date").append("<option value = <%=files[i]%>><%=files[i]%></option>\n");
					<%
					}
					%>
				} else {
					<% files = dir.list((f, name)->name.startsWith("localhost_access_log.")); %>
				}
			})
		})
	</script>
	<div class="file">
		<span>로그</span>
		<input type="radio" name="file" value="tomcat"><label for="tomcat">톰캣 내부 로그</label>
		<input type="radio" name="file" value="client"><label for="client">HTTP 클라이언트 접속 정보</label>
	</div>
	<div class="date">
		<span>날짜</span>
		<select>
		</select>
	</div>
	<div class="option">
		<span>옵션</span>
		<input type="checkbox" name="opt" value="ip"><label for="ip">IP</label>
	</div>
</html>
</body>