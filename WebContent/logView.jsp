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
	<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-BmbxuPwQa2lc/FVzBcNJ7UAyJxM6wuqIj61tLrc4wSX0szH/Ev+nYRRuWlolflfl" crossorigin="anonymous">
	<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta2/dist/js/bootstrap.bundle.min.js" integrity="sha384-b5kHyXgcpbZJO/tY9Ul7kGkf1S0CWuKcCD38l8YkeH8z8QjE0GmW1gYU5S9FOnJ0" crossorigin="anonymous"></script>
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
	String optionList[] = {"RemoteIP", "LocalIP", "BytesSent", "RemoteHostName", "RequestProtocol", "Logical Username", "RequestMethod", "LocalPort", "QueryString", "Date", "HTTP status code", "user session ID", "Remote user", "RequestedURL"};
	ArrayList<String> files = blockDAO.readAllFile();		// all files
	%>
	
	<div class="container">
	<div class="row mt-5 file">
		<div class="lead">HTTP 클라이언트 접속 정보</div>
	</div>
	<div class="row mt-2">
		<div class="display-3">HTTP 클라이언트 접속 정보</div>
	</div>
	<div class="row mt-5 align-items-center">
		<div class="col-sm-2 date">
			<span>날짜</span>
		</div>
		<div class="col-sm-5">
			 <select class="form-select formm-select-lg" id="select" aria-label="Default select example">
				<%
				for(int i = 0; i < files.size(); i++)
					out.println("<option value='"+i+"'>"+files.get(i)+"</option>");
				%>
			</select>
		</div>
	</div>
	<div class="row mt-3" id="option">
		<div class="col-sm-2">
			<span>옵션</span>
		</div>
		<%
		for (int i=0; i<optionList.length; i++) {
			out.println("<div class=\"col-sm-2 form-check form-switch\">");
			out.print("<input class=\"form-check-input\" type=\"checkbox\" name=\"option\" id=\""+i+"\" value=\""+i+"\"");
			if (i==0||i==13) out.print(" checked disabled ");
			out.println(">\n<label class=\"form-check-label\" for=\""+i+"\">"+optionList[i]+"</label>");
			out.println("</div>");
			if (i==4||i==9||i==13) out.println("\n<div class=\"w-100\"></div>\n<div class=\"col-sm-2\"></div>");
			}
		%>
		<div class="col-sm-2 form-check form-switch">
			<input class="form-check-input" type="checkbox" id="all">
			<label class="form-check-label" for="all">ALL</label>
		</div>
		<div class="col-sm-2 form-check form-switch">
			<input class="form-check-input" type="checkbox" id="none">
			<label class="form-check-label" for="none">NONE</label>
		</div>
	</div>
	<div class="row mt-3">
		<div class="col-sm-10"></div>
		<button type="button" class="col-sm-1 btn btn-primary">submit</button>
	</div>
	</div>
	
	<table><tbody></tbody></table>
	<script>
		$(function() {
			$select = $('#select');
			$option = $('div#option');
			$table = $('table > tbody');
			var optionList = ["RemoteIP", "LocalIP", "BytesSent", "RemoteHostName", "RequestProtocol", "Logical Username", "RequestMethod", "LocalPort", "QueryString", "Date", "HTTP status code", "user session ID", "Remote user", "RequestedURL"];

			$('#all').on('click', function() {
				if ($(this).is(':checked')) {
					$('input:checkbox[name=option]').prop("checked", true);
				}
				else {
					$('input:checkbox[name=option]').prop("checked", false);
					$('input:checkbox#0').prop("checked", true);
					$('input:checkbox#13').prop("checked", true);
				}
			})
			
			$('#none').on('click', function() {
				if ($(this).is(':checked')) {
					$('input:checkbox[name=option]').prop("checked", false);
					$('input:checkbox#0').prop("checked", true);
					$('input:checkbox#13').prop("checked", true);
				}
			})

			
	    $('input:button').on('click', function() {
	            var optionChecked = [];
	            $('input:checkbox[name=option]:checked').each(function() {
	                    optionChecked.push($(this).val());
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
