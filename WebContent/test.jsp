<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="blockChain.block" %>
<%@ page import="blockChain.accessedByJSP" %>
<%@ page import="java.util.ArrayList" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
<%
accessedByJSP block = new accessedByJSP();
ArrayList<String> files = block.readAllFile();		// all files
String file = files.get(0);
%>
<textarea id="result" readonly style="width: 95%; height: 650px"><%=file+"\n" %></textarea>
<script type="text/javascript">
	<%
	ArrayList<block> content = block.getChain(file);
	ArrayList<String> originalFile = block.readLogFile(file);
	int k = 0;
	for(int i = 0; i < content.size(); i++){
		String[] str = content.get(i).content.split("\n");
		for(int j = 0; j < str.length; j++){
			if(str[j].equals(originalFile.get(k))){	// compare file & block chain
				%>
				document.getElementById("result").value += "<%=str[j].replaceAll("\\\\", "/").replaceAll("\"", "\'") %>\n";
				<%
			}
			else{
				%>
				document.getElementById("result").value += "[block chain]<%=str[j].replaceAll("\\\\", "/").replaceAll("\"", "\'") %>\n";
				document.getElementById("result").value += "[original file ]<%=originalFile.get(k).replaceAll("\\\\", "/").replaceAll("\"", "\'") %>\n";
				<%
			}
			k++;
		}
	}
	%>

</script>
</body>
</html>