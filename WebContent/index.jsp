<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.ArrayList" %>
<%@ page import="security.AES" %>
<%@ page import="security.RSA" %>
<%@ page import="db.Decrypt" %>
<%@ page import="db.DecryptDAO" %>
<%@ page import="db.logDAO" %>
<%@ page import="file.file" %>
<%@ page import="file.fileDAO" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<link rel="stylesheet" type="text/css" href="frame.css">
<title>Insert title here</title>
</head>
<%
	fileDAO f = new fileDAO();
	file file1 = null;
	String file = "";
	String option = "";
	boolean upload = false;
	if(request.getParameter("file") != null){
		file = request.getParameter("file");
		upload = true;
		file1 = f.getFileInfo(file);
		option = file1.getFileOption();
	}
	else upload = false;
%>
<body>
	<form method="post" action="fileUpload.jsp" enctype="Multipart/form-data">
		<div class="fileUpload">
			<input type="radio" name="option" class="upload" value="encrypt" checked>encrypt
			<input type="radio" name="option" class="upload" value="decrypt">decrypt
			<input type="file" name="fileUpload" id="file" class="upload" accept="text/css, text/html, text/javascript, text/plain">
			<label for="password">password :</label>
			<input type="text" name="password" id="password" class="upload" autofocus>
			<input type="text" id="state" value="No files currently selected for upload" readonly>
		</div>
		<br><br>
		<div class="left">
			<button id="upload">UPLOAD</button>
			<textarea id="original" name="original" readonly></textarea>
		</div>
	</form>

	<div class="center"><button onclick="actionFile();" id="action">>></button></div>
	<div class="right">
		<button id="download" class="download">DOWNLOAD</button>
		<textarea id="result" readonly></textarea>
	</div>
	
<script type="text/javascript">
	var isFile = false;
	var isPassword = false;
	
	const inputF = document.querySelector('#file');
	const inputP = document.querySelector('#password');
	const upload = document.getElementsByClassName("upload");
	const download = document.getElementsByClassName("download");
	
	init();
	<%
	if(upload){
		int index = 0;
		String tmp = "";
		ArrayList<String> Line;
		%>
		document.getElementById('state').value = "File name: <%=file1.getOriginalName()%>, file size: <%=file1.getFileSize()%>";
		for(var i = 0; i < upload.length; i++)
			upload.item(i).disabled = 'disabled';
		uploadFile();
		
		function uploadFile(){
			document.getElementById('original').value = "";
			document.getElementById("action").disabled = false;
			<%
			index = 0;
			Line = f.read(file);
			while(Line.size() > index) {
				tmp = Line.get(index++);
			%>
				document.getElementById('original').value += "<%=tmp.replaceAll("\\\\", "/").replaceAll("\"", "\'")%>\n";
			<%
			}
			%>
		}
		
		function actionFile(){
			for(var i = 0; i < download.length; i++)
				download.item(i).disabled = false;
			<%
			index = 0;
			Line = f.readResult(file);
			while(Line.size() > index) {
				tmp = Line.get(index++);
			%>
				document.getElementById('result').value += "<%=tmp.replaceAll("\\\\", "/").replaceAll("\"", "\'")%>\n";
			<%
			}
		}
		
			document.getElementById("rsa").value = "";
			document.getElementById("aes").value = "";
			document.getElementById("name").value = "";
			document.getElementById("recieve").disabled = 'disabled';
			document.getElementById("reconnect").disabled = false;
			document.getElementById("disconnect").disabled = 'disabled';
		document.getElementById("download").addEventListener("click", function(event) {
            event.preventDefault();// a 태그의 기본 동작을 막음
            event.stopPropagation();// 이벤트의 전파를 막음=
            var fName = encodeURIComponent("<%=file%>");
            window.location.href ="fileDownload.jsp?file=" + fName;
        });

	<%
	}
	else{
	%>
		inputF.addEventListener("change", (evt) => {
			const file = evt.target.files[0];
			if(file == null){
				document.getElementById('state').value = 'No files currently selected for upload';
				isFile = false;
			}
			else{
				if(validFileType(file)){
					document.getElementById('state').value = "File name: " + file.name + ", file size: " + returnFileSize(file.size);
					isFile = true;
				}
				else{
					document.getElementById('state').value = "File name: " + file.name + ": Not a valid file type. Update your selection.";
					isFile = false;
				}
			}
			registerCheck();
		});
		
		inputP.addEventListener("change", (evt) => {
			const pw = evt.target.value;
			if(pw.length < 6)
				isPassword = false;
			else
				isPassword = true;
			registerCheck();
		});
		
		function validFileType(file) {
			const fileTypes = [
				"text/css",
				"text/html",
				"text/javascript",
				"text/plain"
			];
			return fileTypes.includes(file.type);
		}
		
		function returnFileSize(number){
			if(number < 1024)
				return number + 'bytes';
			else if(number >= 1024 && number < 1048576)
				return (number/1024).toFixed(1) + 'KB';
			else if(number >= 1048576)
				return (number/1048576).toFixed(1) + 'MB';
		}
		
		function registerCheck(){
			if(isFile && isPassword){
				document.getElementById("upload").disabled = false;
			}
			else{
				document.getElementById("upload").disabled = 'disabled';
			}
		}
	<%
	}
	%>

	function init(){
		document.getElementById("upload").disabled = 'disabled';
		document.getElementById("action").disabled = 'disabled';
		for(var i = 0; i < download.length; i++)
			download.item(i).disabled = 'disabled';
	}
	</script>
</body>
</html>