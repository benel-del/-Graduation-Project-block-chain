<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import= "java.io.File" %>
<%@ page import="java.io.FileReader" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="blockChain.UserServer" %>
<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.util.ArrayList" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8" http-equiv="refresh" content="60">
	<script src="https://cdn.jsdelivr.net/npm/chart.js@2.8.0"></script>
	<script src="https://code.jquery.com/jquery-3.5.1.min.js"></script>
	<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-BmbxuPwQa2lc/FVzBcNJ7UAyJxM6wuqIj61tLrc4wSX0szH/Ev+nYRRuWlolflfl" crossorigin="anonymous">
	<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta2/dist/js/bootstrap.bundle.min.js" integrity="sha384-b5kHyXgcpbZJO/tY9Ul7kGkf1S0CWuKcCD38l8YkeH8z8QjE0GmW1gYU5S9FOnJ0" crossorigin="anonymous"></script>
	<style>
	@media (min-width: 1405px) {
	  .container-fluid{
	    width: 1400px;
	  }
	}
	</style>
	<title>Log Analysis</title>
</head>
<body>
<%
	String userID = null;
	String userPW = null;
	if(session.getAttribute("userID") != null && session.getAttribute("userPW") != null){
		userID = (String) session.getAttribute("userID");
		userPW = (String) session.getAttribute("userPW");
		UserServer server = new UserServer(userID, userPW);
		server.strt();
	}
	else{
		PrintWriter script = response.getWriter();
		script.println("<script>");
		script.println("location.href = 'admin.jsp'");
		script.println("</script>");
	}
%>
<div class="container-fluid">
<div class="row mt-5">
	<div class="col-sm-3">
		<div class="row file">
				<div class="lead"><a href="logView.jsp">HTTP 클라이언트 접속 정보</a></div>
				<div class="lead"><a href="logout.jsp">logout</a></div>
		</div>
	</div>
</div>
<div class="row mt-5">
	<div class="col-sm-5"><canvas id="pconn"></canvas></div>
	<div class="col-sm-1 dropdown">
		<button type="button" class="btn btn-secondary" id="p">day</button>
	</div>
	<div class="col-sm-5"><canvas id="wconn"></canvas></div>	
</div>
</div>
<script>
$(function() {
	const PCONN = $("#pconn");
	$('#p').on('click', function() {
		$.ajax({
            url: "<%=request.getContextPath()%>/LogsView",
			method: "POST",
			data: {chart:"day"},
			dataType: "json"
		})
		// HTTP 요청이 성공하면 요청한 데이터가 done() 메소드로 전달됨.
        .done(function(json) {
        	var lineChart = new Chart(PCONN, {
        	   type: 'line',
        	   data: {
        	      labels: Object.keys(json),
        	      datasets: [{
        	         label: 'My first dataset',
        	         fill: false,
        	         lineTension: 0,
        	         backgroundColor: "rgba(75,192,192,0.4)",
        	         borderColor: "rgba(75,192,192,1)",
        	         borderCapStyle: 'butt',
        	         borderDash: [],
        	         borderDashOffset: 0.0,
        	         borderJointStyle: 'miter',
        	         data: Object.values(json)
        	      }]
        	   }
        	})
        })
		// HTTP 요청이 실패하면 오류와 상태에 관한 정보가 fail() 메소드로 전달됨.
        .fail(function(xhr, status, errorThrown) {
            alert("오류가 발생했습니다.");
        })
    })
})
</script>
</body>
</html>
