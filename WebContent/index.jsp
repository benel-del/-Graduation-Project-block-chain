<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.net.URLDecoder"%>
<%@ page import="java.util.ArrayList" %>
<%@ page import="file.FileDAO" %>
<%@ page import="java.io.File" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-BmbxuPwQa2lc/FVzBcNJ7UAyJxM6wuqIj61tLrc4wSX0szH/Ev+nYRRuWlolflfl" crossorigin="anonymous"> <!-- bootstrap -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta2/dist/js/bootstrap.bundle.min.js" integrity="sha384-b5kHyXgcpbZJO/tY9Ul7kGkf1S0CWuKcCD38l8YkeH8z8QjE0GmW1gYU5S9FOnJ0" crossorigin="anonymous"></script> <!-- bootstrap -->
<script src="https://code.jquery.com/jquery-3.5.1.min.js"></script> <!-- font style -->
<script src="http://malsup.github.io/jquery.form.js"></script> <!-- font style -->
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-BmbxuPwQa2lc/FVzBcNJ7UAyJxM6wuqIj61tLrc4wSX0szH/Ev+nYRRuWlolflfl" crossorigin="anonymous"> <!-- jQuery -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta2/dist/js/bootstrap.bundle.min.js" integrity="sha384-b5kHyXgcpbZJO/tY9Ul7kGkf1S0CWuKcCD38l8YkeH8z8QjE0GmW1gYU5S9FOnJ0" crossorigin="anonymous"></script> <!-- jQuery -->
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
	String[] name = {"originalFile", "originalSize", "newFile", "newSize", "option"};
	String[] value = new String[5];
	FileDAO f = new FileDAO();
%>
<body>
<div class="container-fluid">
	<div class="row mt-5 d-flex flex-row-reverse">
		<div class="display-3" style="font-family: 'Song Myung', serif;">Encrypt / Decrypt</div>
	</div>
	<form id="form" enctype="multipart/form-data">
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
			<input type="file" name="file" id="file" class="form-control form-control upload" required accept="text/css, text/html, text/javascript, text/plain">
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
		<div class="alert alert-danger" id="format" role="alert" style="visibility:hidden">
			<span>DECRYPT FILE MUST BE "_enc.txt" FORMAT</span>
		</div>
	</div>
	<div class="row mt-4">
		<div class="col-sm-2 left">
			<button type="button" class="btn btn-primary btn upload" id="upload">UPLOAD</button>
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
	</form>
	<div class="row mt-4">
		<div class="col-sm-5 left">
			<textarea class="form-control" id="original" name="original" rows="20" readonly></textarea>
		</div>
		<div class="col-sm-2 d-flex justify-content-center align-items-center">
		</div>
		<div class="col-sm-5 right">
			<textarea class="form-control" id="result" rows="20" readonly></textarea>
		</div>
	</div>
</div>
	
<script type="text/javascript">
	history.replaceState({}, null, "index.jsp");

	var isFile = false;
	var isPassword = false;
	
	const inputF = document.querySelector('#file');
	const inputP = document.querySelector('#password');
	const upload = document.getElementsByClassName("upload");
	
	init();
	
	$(function() {
		var newFile;
	    $('#upload').on('click', function() {
	    	$("#form").ajaxForm({
				url: "<%=request.getContextPath()%>/fileUpload",
	            enctype : "multipart/form-data",
	            traditional:true,
                processData: false,
                contentType: false,
                type: "POST",
                dataType:"json",
                beforeSubmit: function(data, form, option){
                	if($('#file').val() == ""){
                		alert("select file");
                		return false;
                	}
                	if($('#password').val().length < 5){
                		alert("at least 5 character");
                		return false;
                	}
                },
                success: function(result){
                	newFile = result['newFile'];
                	if (result['err'] == "type error"){
                		alert('decrypt file type error!');
                	}
                	else{
                		for(var i = 0; i < upload.length; i++)
                			upload.item(i).disabled = 'disabled';
                		document.getElementById('stateUpload').value = "File: "+result['originalFile']+", size: "+result['originalSize'];
            			document.getElementById('original').value += result['tmp'];
            			document.getElementById('download').disabled = false;
            			document.getElementById('stateDownload').value = "File: "+result['newFile']+", size: "+result['newSize'];
            			document.getElementById('result').value += result['tmp2'];
                	}
                }
	        }).submit();
		});

		$('#download').on('click', function() {
	    	$.ajax({
				url: "<%=request.getContextPath()%>/fileDownload",
                type: "POST",
                data: {
                	file: newFile
                },
                dataType:"text"
	        })
	        .done(function(data){
        		window.location.reload();
	        });
		});
	});

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
				document.getElementById('state').value = "File: Not a valid file type..";
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
	function init(){
		$("#file").val("");
		$("#state").val("No files currently selected for upload");
		document.getElementById('download').disabled = 'disabled';
		document.getElementById("stateUpload").value = "";
		document.getElementById("stateDownload").value = "";
		document.getElementById("original").value = "";
		document.getElementById("result").value = "";
		
		<%
		String path = "/Users/kjs/JSP/.metadata/.plugins/org.eclipse.wst.server.core/tmp1/wtpwebapps/block/uploadFile";
		String[] fileNameOfPath = new File(path).list();
		if(fileNameOfPath!=null){
			for(int i = 0; i < fileNameOfPath.length; i++)
				new File(path + "/" + fileNameOfPath[i]).delete();
		}
		//for(int i = 0; i < name.length; i++)
		//	session.removeAttribute(name[i]);
		
		%>
		
		
	}
	</script>
</body>
</html>
