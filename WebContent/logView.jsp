<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import = "javax.servlet.jsp.*" %>
<!DOCTYPE html>
<html>
<head>
        <meta charset="UTF-8">
        <title>LOG</title>
</head>
<style>
table, td {
    border: 1px solid #333;
    border-collapse: collapse;
}
tr.basic {
        background-color: white;
}
tr.serv {
        background-color: #EFCDCE;
}
tr.block {
        background-color: #BAE1E0;
}
td.serv {
        border-bottom-style: none;
}
td.block {
        border-top-style: none;
}
</style>
<!--
-->
<body>
<%!
        String[] color = {"basic", "serv", "block"};
        private void printLog(String[] log, int code, javax.servlet.jsp.JspWriter out) throws ServletException {
                try {
                        out.print("<tr class="+color[code]+"><td class="+color[code]+">");
                        for (int i=0; i<log.length; i++) {
                                out.println(log[i]+" ");
                                if (i==1||i==4)
                                        out.println("</td><td class="+color[code]+">");
                        }
                        out.print("</td></tr>");
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }
        private boolean compareLog(String[] log1, String[] log2) {
                if (log1.length != log2.length)
                        return false;
                for (int i=0; i<log1.length; i++) {
                        if (!log1[i].equals(log2[i]))
                                return false;
                }
                return true;
        }
%>
<table class="log">
<tr><td>TIME</td><td>CLASS</td><td>log</td></tr>
<%
        request.setCharacterEncoding("UTF-8");

        String[] servLog = (String[]) request.getAttribute("servLog");
        String[] blockLog = (String[]) request.getAttribute("blockLog");
        String[] splitServ;
        String[] splitBlock;

        for (int i=0; i<servLog.length; i++) {
                splitServ = servLog[i].split(" ");
                splitBlock = blockLog[i].split(" ");

                if (compareLog(splitServ, splitBlock))
                        printLog(splitServ, 0, out);

                else {
                        printLog(splitServ, 1, out);
                        printLog(splitBlock, 2, out);
                }
        }
%>
</table>
</body>
</html>