<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.net.URLDecoder"%>
<%@ page import="java.util.ArrayList" %>
<%@ page import="security.AES" %>
<%@ page import="file.file" %>
<%@ page import="file.fileDAO" %>
<%@ page import="java.io.File" %>

<%@ page import="blockChain.test" %>

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
		file = URLDecoder.decode(request.getParameter("file"), "UTF-8");
		upload = true;
		file1 = f.getFileInfo(file);
		option = file1.getFileOption();
	}
	else upload = false;
%>
<body>
	<form method="post" action="fileUpload.jsp" enctype="Multipart/form-data">
		<div class="fileUpload">
			<input type="radio" name="option" class="upload" id="encrypt" value="encrypt" checked onclick="radio(0);"><label for="encrypt">encrypt</label>
			<input type="radio" name="option" class="upload" id="decrypt" value="decrypt" onclick="radio(1);"><label for="decrypt">decrypt</label>
			<input type="file" name="fileUpload" id="file" class="upload" required accept="text/css, text/html, text/javascript, text/plain">
			<label for="password">password :</label>
			<input type="text" name="password" id="password" class="upload" maxlength="20" placeholder="5~20 영어" pattern="[A-Za-z]+" autofocus required>
			<input type="text" id="state" class="upload" value="No files currently selected for upload" readonly>
		</div>
		<p id="format">DECRYPT FILE MUST BE "_enc.txt" FORMAT</p><br>
		<div class="left">
			<button id="upload">UPLOAD</button>
			<input type="text" id="stateUpload" class="loadState" readonly>
			<textarea id="original" name="original" readonly></textarea>
		</div>
	</form>

	<div class="center"><button onclick="actionFile();" id="action">>></button></div>
	<div class="right">
		<button id="download" class="download">DOWNLOAD</button>
		<input type="text" id="stateDownload" class="loadState" readonly>
		<textarea id="result" readonly></textarea>
	</div>
	
<script type="text/javascript">
	history.replaceState({}, null, "index.jsp");


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
		String nfile = "";
		ArrayList<String> Line;
		%>
		document.getElementById('stateUpload').value = "File name: <%=file1.getOriginalName()%>, file size: <%=file1.getOriginalFileSize()%>";
		for(var i = 0; i < upload.length; i++)
			upload.item(i).disabled = 'disabled';
		uploadFile();
		
		<%
		if(option.equals("encrypt"))
			nfile = file.substring(0, file.length()-4) + "_" + option.substring(0, 3) + ".txt";
		else if(option.equals("decrypt"))
			nfile = file.substring(0, file.length()-8) + ".txt";
		%>
		
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
			document.getElementById("action").disabled = 'disabled';
			
			document.getElementById('stateDownload').value = "File name: <%=nfile%>, file size: <%=file1.getResultFileSize()%>";
			for(var i = 0; i < download.length; i++)
				download.item(i).disabled = false;
			<%
			index = 0;
			Line = f.read(nfile);
			while(Line.size() > index) {
				tmp = Line.get(index++);
			%>
				document.getElementById('result').value += "<%=tmp.replaceAll("\\\\", "/").replaceAll("\"", "\'")%>\n";
			<%
			}
			%>
		}
		
		document.getElementById("download").addEventListener("click", function(event) {
            event.preventDefault();// a 태그의 기본 동작을 막음
            event.stopPropagation();// 이벤트의 전파를 막음=
            window.location.href ="fileDownload.jsp?file=<%=nfile%>";
        });

	<%
	}
	else{
		String path = "/usr/local/lib/apache-tomcat-9.0.43/webapps/block/uploadFile";
		String[] fileNameOfPath = new File(path).list();
		for(int i = 0; i < fileNameOfPath.length; i++){
			System.out.println("delete:" + fileNameOfPath[i]);
			new File(path + "/" + fileNameOfPath[i]).delete();
		}
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
		
		function radio(index){
			if(index == 1)
				document.getElementById("format").style.visibility = 'visible';
			else if(index == 0)
				document.getElementById("format").style.visibility = 'hidden';
		}
	<%
	}
	%>

	function init(){
		document.getElementById("upload").disabled = 'disabled';
		document.getElementById("action").disabled = 'disabled';
		document.getElementById("format").style.visibility = 'hidden';
		for(var i = 0; i < download.length; i++)
			download.item(i).disabled = 'disabled';
		document.getElementById("stateUpload").value = "";
		document.getElementById("stateDownload").value = "";
		document.getElementById("original").value = "";
		document.getElementById("result").value = "";
	}
	</script>
</body>
</html>