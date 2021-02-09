<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="db.DecryptDAO" %>
<%@ page import="security.AES" %>
<%@ page import="security.RSA" %>
<%@ page import="java.io.PrintWriter" %>
<% request.setCharacterEncoding("UTF-8"); %>
<jsp:useBean id="decrypt" class="db.Decrypt" scope="page" />
<jsp:setProperty name="decrypt" property="name"/>
<jsp:setProperty name="decrypt" property="aes"/>
<jsp:setProperty name="decrypt" property="rsa"/>
<jsp:setProperty name="decrypt" property="ciphertext"/>

<%
	DecryptDAO decryptDAO = new DecryptDAO();

    String message = AES.recieve(decrypt.getCiphertext(), decrypt.getAes());

	int result = decryptDAO.insertDB(decrypt.getName(), decrypt.getAes(), decrypt.getRsa(), message);
	if(result == -1){
		PrintWriter script = response.getWriter();
           script.println("<script>");
           script.println("alert('데이터베이스 오류')");
           script.println("location.href = 'index.jsp'");
           script.println("</script>");
	}
	else{
		PrintWriter script = response.getWriter();
		script.println("<script>");
		script.println("location.href = 'index.jsp'");
		script.println("</script>");
	}
%>