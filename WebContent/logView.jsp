<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.io.File" %>
<%@ page import="blockChain.block" %>
<%@ page import="blockChain.blockDAO" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.io.PrintWriter" %>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>log view page</title>
	<script src="https://code.jquery.com/jquery-3.5.1.min.js"></script>
	<style>
	table, td {
    	border: 1px solid #333;
   	 	border-collapse: collapse;
	}
	.code1, .code3 { background-color: #EFCDCE; }
	.code2, .code4 { background-color: #BAE1E0; }
	.code3 { border-bottom-style: none; }
	.code4 { border-top-style: none; }
	</style>
</head>
<body>
	<%
	String userID = null;
	if(session.getAttribute("userID") != null){
		userID = (String) session.getAttribute("userID");
	}
	if(userID == null || userID != null && !userID.equals("v1e3er")){
		PrintWriter script = response.getWriter();
		script.println("<script>");
		script.println("history.back()");
		script.println("</script>");
	}
	
	String optionList[] = {"RemoteIP", "LocalIP", "BytesSent", "RemoteHostName", "RequestProtocol", "RemoteLogicalUsername", "RequestMethod", "LocalPort", "QueryString", "FirstLineRequest", "StatusofResponse", "UserSessionID", "Date", "UserAuthenticated", "RequestedURL", "LocalServerName", "TimeTakenToProcesssRequest"};
	ArrayList<String> files = blockDAO.readAllFile();		// all files
	%>
	<div class="file"><span>HTTP 클라이언트 접속 정보</span></div>
	<div class="date">
		<span>날짜</span> <select id="select">
		<%
		for(int i = 0; i < files.size(); i++)
			out.println("<option value='"+i+"'>"+files.get(i)+"</option>");
		%>
		</select>
	</div>
	<div id="option">
		<span>옵션</span>
		<%
		for (int i=0; i<optionList.length; i++)
			out.println("<input type='checkbox' name='option' id='"+i+"' value='"+i+"'><label for='"+i+"'>"+optionList[i]+"</label>");
		%>
	</div>
	<input type="button" value="submit">
	<table><tbody></tbody></table>
	<script>
		$(function() {
			$select = $('#select');
			$option = $('div#option');
			$table = $('table > tbody');
			var optionList = ["RemoteIP", "LocalIP", "BytesSent", "RemoteHostName", "RequestProtocol", "RemoteLogicalUsername", "RequestMethod", "LocalPort", "QueryString", "FirstLineRequest", "StatusofResponse", "UserSessionID", "Date", "UserAuthenticated", "RequestedURL", "LocalServerName", "TimeTakenToProcesssRequest"];
			
	    $('input:button').on('click', function() {
	            var optionChecked = [];
	            $('input:checkbox[name=option]:checked').each(function() {
	                    optionChecked.push($(this).val());
	                    console.log("option Checked: "+$(this).val());
	            })
	            $table.empty();
	            console.log($('#select option:selected').text());
	            $.ajax({
	                    url: "<%=request.getContextPath()%>/Log",
	                    traditional:true,
	                    method: "POST",
	                    data: {
	                            file:$('#select option:selected').text(),
	                            option:optionChecked
	                            },
	                    dataType:"json"
	            })
	            .done(function(json) {
	                    $table.append("<tr></tr>");
	                    $.each(optionChecked, function(i, v) {
	                            $table.children('tr:eq(0)').append('<td>'+optionList[v]+'</td>');
	                    })
	                    $.each(json, function(jarrKey, jarrValue){
	                            $table.append("<tr></tr>");
	                            $.each(jarrValue, function(jobKey, jobValue) {
	                                    $tr = $table.children('tr:eq('+(jarrKey+1)+')');
	                                    $tr.addClass("code"+jobKey);
	                                    $.each(jobValue, function(i, v) {
	                                            $tr.append("<td>"+v+"</td>");
	                                    })
	                            })
	                    });
	            })
	            .fail(function() {
	                    if (optionChecked.length==0)
	                            alert("OPTION을 선택하여 주세요.");
	            })
		})
	})
</script>
</body>
</html>