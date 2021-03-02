<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="blockChain.block" %>
<%@ page import="blockChain.accessedByJSP" %>
<%@ page import="java.util.ArrayList" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<%
accessedByJSP block = new accessedByJSP();
ArrayList<String> files = block.readAllFile();		// all files
String file = null;
if(request.getParameter("file") != null){
	file = request.getParameter("file");
}
%>
</head>
<body>
	<label for="name">log file: </label>
	<select id="log">
		<option value="no" selected>select file</option>
	<%
		for(int i = 0; i < files.size(); i++){
			%>
			<option value="<%=files.get(i) %>"<%if(file != null && file.equals(files.get(i))) out.print(" selected");%>><%=files.get(i)%></option>
			<%
		}
	%>
	</select>
	<button onclick="url()">select</button>
<div id="logview">
<%
	if(file != null && !file.equals("no")){
%>
	<table>
	<%
		ArrayList<block> content = block.getChain(file);
		ArrayList<String> originalFile = block.readLogFile(file);
		out.println("<tr><th>content size: " + content.size());
		out.println("original size: " + originalFile.size() + "</th></tr>");
		int k = 0;
		for(int i = 0; i < content.size(); i++){
			String[] str = content.get(i).content.split("\n");
			
			for(int j = 0; j < str.length; j++){
				%>
				<tr><td>
				<%
				if(str[j].equals(originalFile.get(k))){	// compare file & block chain
					out.println(str[j].replaceAll("\\\\", "/").replaceAll("\"", "\'"));
				}
				else{
					out.println("[block chain]" + str[j].replaceAll("\\\\", "/").replaceAll("\"", "\'"));
					out.println("[original file ]" + originalFile.get(k).replaceAll("\\\\", "/").replaceAll("\"", "\'"));
				}
				k++;
				%>
				</td></tr>
			<%
			}
		}
		%>
	</table>
<%
	}
%>
</div>

<script type="text/javascript">
	const log = document.getElementById("log");
	function url(){
		var data = log.options[log.selectedIndex].value;
		if(data != "no")
			location.href = "test.jsp?file="+data;
		else
			location.href = "test.jsp?file=no";
	}
</script>
</body>
</html>