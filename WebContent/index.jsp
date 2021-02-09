<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="security.AES" %>
<%@ page import="security.RSA" %>
<%@ page import="db.Decrypt" %>
<%@ page import="db.DecryptDAO" %>
<%@ page import="db.logDAO" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<link rel="stylesheet" type="text/css" href="frame.css">
<title>Insert title here</title>
</head>
<body>
	<form>
		<input type="text" id="text" value="">
		<input onclick="recieveMessage()" value="Recieve Data" id="recieve" type="button" disabled="disabled">
		<input onclick="location.reload();" value="Connect" id="reconnect" type="button" disabled="disabled">
		<input onclick="disconnect_()" value="Disconnect" id="disconnect" type="button">
		<br/>
	</form>
	<form method="post" action="decryptAction.jsp">
		<input type="text" id="state" name="state" value="communtion state: " readonly>
		<input type="text" id="name" name="name" value="">
		<input type="text" id="aes" name="aes" value="">

		<br /><br />
		<div class="left"><textarea id="ciphertext" name="ciphertext" readonly></textarea></div>
		<div class="center"><input id="decrypt" value=">>" type="submit" disabled="disabled"></input></div>
		<div class="right" id="update"><textarea id="plaintext" readonly></textarea></div>
	</form>

	<script type="text/javascript">
		<%
		DecryptDAO decryptDAO = new DecryptDAO();
		logDAO log = new logDAO();
		Decrypt data = decryptDAO.isExist();
		
		if(data == null){
			%>
			document.getElementById("state").value = "communtion state: Server Connect try...\n";
			<%
			String address = "ws://192.168.36.221:8080/block/websockets";	//203.153.146.57:8080
			AES aes = new AES();
			RSA rsa = new RSA();
			aes.setKey();

			int index = log.register("client", RSA.KeyToStr(rsa.getPublicKey())) - 1;
			String server = "server" + index;
		%>
			var webSocket = new WebSocket("<%=address%>");
			webSocket.onopen = function(message) {
				document.getElementById("state").value = "communtion state: Server connect...\n\n";

				webSocket.send("<%=index+1%>");
				document.getElementById("name").value = "client"+"<%=index+1%>";
				document.getElementById("aes").value = "<%=aes.getKey_st()%>";
				
				document.getElementById("recieve").disabled = false;
			};
			
			webSocket.onclose = function(message) {
				document.getElementById("state").value = "communtion state: Server Disconnect...\n";
			};
			
			webSocket.onerror = function(message) {
				document.getElementById("state").value = "communtion state: error...\n";
			};
			
			webSocket.onmessage = function(message) {
				if(message.data == "OK"){
					webSocket.send("<%= RSA.sign(aes.getKey_st(), log.getKey(server), rsa.getPrivateKey())%>");
				}
				else if(message.data == "disconnect")
					disconnect_();
				else
					document.getElementById("ciphertext").value = message.data;
			};
			
			function recieveMessage(){
				webSocket.send("recieve");
				//document.getElementById("recieve").disabled = 'disabled';
				document.getElementById("decrypt").disabled = false;
			}

			<%
		}
		else{
			%>
			var webSocket;
			document.getElementById("aes").value = "<%=data.getAes()%>";
			document.getElementById("name").value = "<%=data.getName()%>";
			document.getElementById("plaintext").innerHTML = "<%=data.getPlaintext().replaceAll("\\\\", "/").replaceAll("\n", "&#10").replaceAll("\"", "\'") %>";
			document.getElementById("state").value = "communtion state: Server Disconnect...\n";
			disconnect_();
			<%
		}
		%>
		function connect(){
			location.reload();
		}
		
		function disconnect_() {
			document.getElementById("ciphertext").value = "";
			document.getElementById("aes").value = "";
			document.getElementById("name").value = "";
			document.getElementById("recieve").disabled = 'disabled';
			document.getElementById("reconnect").disabled = false;
			document.getElementById("disconnect").disabled = 'disabled';

			disconnect();
		}
		
		function disconnect(){
			<%
			if(data == null){
				%>
				webSocket.close();
				<%
			}
			else{
				int index = Integer.parseInt(data.getName().substring(6));
				log.delete(data.getName());
				if(index != 0)	log.delete("server"+index);
			}
			%>
		}
	</script>
</body>
</html>