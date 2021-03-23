<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.net.URLDecoder"%>
<%@ page import="java.util.ArrayList" %>
<%@ page import="crypto.AES" %>
<%@ page import="file.FileInfo" %>
<%@ page import="file.FileDAO" %>
<%@ page import="java.io.File" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-BmbxuPwQa2lc/FVzBcNJ7UAyJxM6wuqIj61tLrc4wSX0szH/Ev+nYRRuWlolflfl" crossorigin="anonymous">
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta2/dist/js/bootstrap.bundle.min.js" integrity="sha384-b5kHyXgcpbZJO/tY9Ul7kGkf1S0CWuKcCD38l8YkeH8z8QjE0GmW1gYU5S9FOnJ0" crossorigin="anonymous"></script>
<title>Insert title here</title>
<style>
@media (min-width: 1405px) {
  .container-fluid{
    width: 1400px;
  }
}
</style>
</head>
<%
file.FileDAO f = new file.FileDAO();
	file.FileInfo file1 = null;
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
	<div class="container-fluid">
	<div class="row align-items-center mt-5 file upload">
		<div class="col-sm-2">
			<div class="form-check form-check-inline">
				<input class="form-check-input upload" type="radio" name="option" id="encrypt" value="encrypt" checked onclick="radio(0);">
				<label class="form-check-label" for="encrypt">encrypt</label>
			</div>
			<div class="form-check form-check-inline">
				<input class="form-check-input upload" type="radio" name="option" id="decrypt" value="decrypt" onclick="radio(1);">
				<label class="form-check-label" for="decrypt">decrypt</label>
			</div>
		</div>
		<div class="col-sm-2">
			<input type="file" name="fileUpload" id="file" class="form-control form-control upload" required accept="text/css, text/html, text/javascript, text/plain">
		</div>
		<div class="col-sm-1">
			<label for="password" class="col-form-label">Password: </label>
		</div>
		<div class="col-sm-2">
			<input type="password" name="password" id="password" class="form-control upload" maxlength="20" placeholder="5~20 영어" pattern="[A-Za-z]+" autofocus required aria-describedby="passwordHelpInline">
		</div>
		<div class="col-sm-2">
			<span id="passwordHelpInline" class="form-text"> Must be 5-20 English characters long. </span>
		</div>
		<div class="col-sm-3">
	        <input type="text" id="state" class="form-control upload" value="No files currently selected for upload" readonly>
		</div>
	</div>
	<div class="row mt-2">
		<div class="alert alert-danger" id="format" role="alert">
			<span>DECRYPT FILE MUST BE "_enc.txt" FORMAT</span>
		</div>
	</div>
	<div class="row mt-4">
		<div class="col-sm-2 left">
			<button class="btn btn-primary btn" id="upload">UPLOAD</button>
		</div>
		<div class="col-sm-3 left">
			<input type="text" id="stateUpload" class="form-control input-lg loadState" readonly>
		</div>
		<div class="col-sm-2"></div>
		<div class="col-sm-2 right">
			<button type="button" class="btn btn-primary btn download" id="download">DOWNLOAD</button>
		</div>
		<div class="col-sm-3 right">
			<input type="text" id="stateDownload" class="form-control input-lg loadState" readonly>
		</div>
	</div>
	<div class="row mt-4">
		<div class="col-sm-5 left">
			<textarea class="form-control" id="original" name="original" rows="20" readonly></textarea>
		</div>
		<div class="col-sm-2 d-flex justify-content-center align-items-center">
			<button type="button" onclick="actionFile();" id="action">>></button>
		</div>
		<div class="col-sm-5 right">
			<textarea class="form-control" id="result" rows="20" readonly></textarea>
		</div>
	</div>
	</div>
	</form>
	
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
			
			document.getElementById('stateDownload').value = "File: <%=nfile%>, size: <%=file1.getResultFileSize()%>";
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
		for(int i = 0; fileNameOfPath!=null && i < fileNameOfPath.length; i++){
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
					document.getElementById('state').value = "File: " + file.name + ", size: " + returnFileSize(file.size);
					isFile = true;
				}
				else{
					document.getElementById('state').value = "File: " + file.name + ": Not a valid file type..";
					isFile = false;
				}
			}
			registerCheck();
		});

		inputP.addEventListener("change", (evt) => {
			const pw = evt.target.value;
			if(pw.length < 5)
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
