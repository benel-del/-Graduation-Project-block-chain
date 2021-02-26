<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="security.block" %>
<%@ page import="security.blockDAO" %>
<%@ page import="java.util.ArrayList" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
<%
blockDAO block = new blockDAO();
ArrayList<String> files = block.readAllFile();		// all files
String file  = "catalina.2021-02-26.log";	// sample
%>
<textarea id="result" readonly style="width: 95%; height: 650px"></textarea>
<script type="text/javascript">
	<%
	if(block.isFile(file) != -1){
		ArrayList<block> content = block.getChain(file);
		ArrayList<String> originalFile = block.readLogFile(file);
		for(int i = 1; i < content.size(); i++){
			// compare file & block chain
			if(content.get(i).content.equals(originalFile.get(i-1))){
				//System.out.println("[test.jsp] content: "+ content.get(i).content);
				%>
				document.getElementById("result").value += "<%=content.get(i).content.replaceAll("\\\\", "/").replaceAll("\"", "\'") %>\n";
				<%
			}
			else{
				%>
				document.getElementById("result").value += "[block chain  ]<%=content.get(i).content.replaceAll("\\\\", "/").replaceAll("\"", "\'") %>\n";
				document.getElementById("result").value += "[original file]<%=originalFile.get(i).replaceAll("\\\\", "/").replaceAll("\"", "\'") %>\n";
				<%
			}
		}
	}
	%>

</script>
</body>
</html>