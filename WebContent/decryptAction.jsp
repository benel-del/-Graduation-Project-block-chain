<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="db.DecryptDAO" %>
<%@ page import="security.AES" %>
<%@ page import="java.io.PrintWriter" %>
<% request.setCharacterEncoding("UTF-8"); %>
<jsp:useBean id="decrypt" class="db.Decrypt" scope="page" />
<jsp:setProperty name="decrypt" property="state"/>
<jsp:setProperty name="decrypt" property="name"/>
<jsp:setProperty name="decrypt" property="aes"/>
<jsp:setProperty name="decrypt" property="ciphertext"/>

<%
	if(decrypt.getState().contains("connect")){
		DecryptDAO decryptDAO = new DecryptDAO();
		AES aes = new AES();
		
		String text = decrypt.getCiphertext();	
		int idx = text.indexOf("||");
        if(idx == -1){
        	decrypt.setPlaintext("Error:: incorrected data");
        }
        String message = text.substring(0, idx);
       	int hash = Integer.parseInt(text.substring(idx+2));
       	decrypt.setPlaintext(AES.decrypt(AES.StrToByte(message), AES.setKey(decrypt.getAes())));
        text = decrypt.getPlaintext() + decrypt.getAes();
        if(hash != text.hashCode()){
        	decrypt.setPlaintext("Error:: data integrity - " + text.hashCode());
        }

		int result = -1;
		result = decryptDAO.insertDB(decrypt.getName(), decrypt.getAes(), decrypt.getPlaintext());
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
	
	}
	else{
		PrintWriter script = response.getWriter();
           script.println("<script>");
           script.println("alert('" + decrypt.getState() + "')");
           script.println("location.href = 'index.jsp'");
           script.println("</script>");
	}
%>