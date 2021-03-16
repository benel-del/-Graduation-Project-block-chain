<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.io.File" %>
<%@ page import="blockChain.block" %>
<%@ page import="blockChain.blockDAO" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.io.PrintWriter" %>
<!DOCTYPE html>
<html>
<link rel="stylesheet" type="text/css" href="frame.css">
<head>
	<meta charset="UTF-8">
	<title>log view page</title>
	<script src="https://code.jquery.com/jquery-3.5.1.min.js"></script>
	<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-BmbxuPwQa2lc/FVzBcNJ7UAyJxM6wuqIj61tLrc4wSX0szH/Ev+nYRRuWlolflfl" crossorigin="anonymous">
	<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta2/dist/js/bootstrap.bundle.min.js" integrity="sha384-b5kHyXgcpbZJO/tY9Ul7kGkf1S0CWuKcCD38l8YkeH8z8QjE0GmW1gYU5S9FOnJ0" crossorigin="anonymous"></script>
</head>
<body>
	<%
	if(session.getAttribute("userID") == null){
		PrintWriter script = response.getWriter();
		script.println("<script>");
		script.println("history.back()");
		script.println("</script>");
	}

	String optionList[] = {"Remote IP", "Local IP", "BytesSent", "Request Protocol", "Request Method", "Time", "HTTP status code", "user session ID", "Requested URL"};
	@SuppressWarnings("unchecked")
	ArrayList<String> files = (ArrayList<String>) session.getAttribute("chainName");		// all files
	%>
	
	<div class="container">
	<div class="row mt-5 file">
		<div class="lead">HTTP 클라이언트 접속 정보</div>
		<div class="logout"><a href="logoutAction.jsp">logout</a></div>
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
			if (i==0||i==8) out.print(" checked disabled ");
			out.println(">\n<label class=\"form-check-label\" for=\""+i+"\">"+optionList[i]+"</label>");
			out.println("</div>");
			if (i==4||i==8) out.println("\n<div class=\"w-100\"></div>\n<div class=\"col-sm-2\"></div>");
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
	<table class="table table-hover mt-5"><tbody></tbody></table>
	</div>
	<script>
		$(function() {
			$select = $('#select');
			$option = $('div#option');
			$table = $('table > tbody');
			var optionList = ["Status", "Remote IP", "Local IP", "BytesSent", "Request Protocol", "Request Method", "Time", "HTTP status code", "user session ID", "Requested URL"];

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
	                    $table.append("<tr><td>status</td></tr>");
	                    $.each(optionChecked, function(i, v) {
	                            $table.children('tr:eq(0)').append('<td>'+optionList[v]+'</td>');
	                    })
	                    $.each(json, function(jarrKey, jarrValue){
	                            $table.append("<tr></tr>");
	                            $.each(jarrValue, function(jobKey, jobValue) {
	                                    $tr = $table.children('tr:eq('+(jarrKey+1)+')');
                                    if (jobKey==1) {
                                    	$tr.addClass("table-success");
	                                    $tr.append("<td><span class=\"badge bg-success\">101A</span></td>");
                                    }
                                    else if (jobKey==2) {
                                    	$tr.addClass("table-danger");
	                                    $tr.append("<td><span class=\"badge bg-danger\">101B</span></td>");
                                    }
                                    else if (jobKey==3) {
                                    	$tr.addClass("table-primary");
	                                    $tr.append("<td><span class=\"badge bg-primary\">102</span></td>");
                                    }
                                    else if (jobKey==4) {
                                    	$tr.addClass("table-secondary");
	                                    $tr.append("<td><span class=\"badge bg-secondary\">103</span></td>");
                                    }
                                    else {
	                                    $tr.append("<td>-</td>");
                                    }
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
