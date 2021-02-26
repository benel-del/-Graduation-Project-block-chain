<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="security.RSA" %>
<%@ page import="security.block" %>
<%@ page import="security.blockDAO" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.security.PublicKey" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
<%
RSA rsa = new RSA();
blockDAO block = new blockDAO();
String file  = "catalina.2021-02-26.log";
PublicKey publicKey = rsa.getPublicKey(block.readKey());
%>
<textarea id="result" readonly style="width: 95%; height: 650px"></textarea>
<script type="text/javascript">
	<%
	if(block.isFile(file) != -1){
		ArrayList<block> content = block.getChainContents(file);
		
		// verification
		ArrayList<String> originalFile = block.readFile(file);
		for(int i = 1; i < content.size(); i++){
			// sign verification
			int dec = Integer.parseInt(rsa.decrypt(content.get(i).sign, publicKey));
			int hash = (content.get(i-1).sign + "||" + content.get(i-1).content).hashCode();
			if(dec != hash){
				System.out.println("[test.jsp] block chain verification - error");
				%>
				document.getElementById("result").value = "block chain hashcode error";
				<%
				break;
			}
			else{
				// compare
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
	}
	%>

</script>
</body>
</html>