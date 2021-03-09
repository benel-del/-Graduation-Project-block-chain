<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="blockChain.blockDAO" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.net.URI" %>
<%@ page import="blockChain.socketClient" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<%
ArrayList<String> files = blockDAO.readAllFile();		// all files
String file = null;
if(request.getParameter("file") != null){
	file = request.getParameter("file");
}
%>
</head>
<body>
	<label for="name">log file: </label>
	<select id="log">
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
		try{
			socketClient socket = new socketClient(file);
			ArrayList<String> content = socket.getContent();
			ArrayList<String> originalFile = blockDAO.readLogFile(file);
			out.println("<tr><th>content size: " + content.size());
			out.println("original size: " + originalFile.size() + "</th></tr>");
			int k = 0;
			for(int i = 0; i < content.size(); i++){
				%>
				<tr><td>
				<%
				if(content.get(i).equals(originalFile.get(k))){	// compare file & block chain
					out.println(content.get(i).replaceAll("\\\\", "/").replaceAll("\"", "\'"));
				}
				else{
					out.println("[block chain]" + content.get(i).replaceAll("\\\\", "/").replaceAll("\"", "\'"));
					out.println("[original file ]" + originalFile.get(k).replaceAll("\\\\", "/").replaceAll("\"", "\'"));
				}
				k++;
				%>
				</td></tr>
			<%
			}
			%>
		</table>
	<%
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
%>
</div>

<script type="text/javascript">
	history.replaceState({}, null, "test.jsp");

	const log = document.getElementById("log");
	function url(){
		var data = log.options[log.selectedIndex].value;
		location.href = "test.jsp?file="+data;
	}
</script>
</body>
</html>