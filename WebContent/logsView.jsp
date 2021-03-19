<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import= "java.io.File" %>
<%@ page import="java.io.FileReader" %>
<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.util.ArrayList" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
	<script src="https://cdn.jsdelivr.net/npm/chart.js@2.8.0"></script>
	<script src="https://code.jquery.com/jquery-3.5.1.min.js"></script>
	<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-BmbxuPwQa2lc/FVzBcNJ7UAyJxM6wuqIj61tLrc4wSX0szH/Ev+nYRRuWlolflfl" crossorigin="anonymous">
	<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta2/dist/js/bootstrap.bundle.min.js" integrity="sha384-b5kHyXgcpbZJO/tY9Ul7kGkf1S0CWuKcCD38l8YkeH8z8QjE0GmW1gYU5S9FOnJ0" crossorigin="anonymous"></script>
	<style>
	@media (min-width: 1405px) {
	  .container-fluid{
	    width: 1400px;
	  }
	}
	</style>
	<title>Log Analysis</title>
</head>
<body>
<div class="container-fluid">
<div class="row mt-5">
<div class="col-sm-3"></div>
<div class="col-sm-6"><canvas id="myChart"></canvas></div>
</div>
</div>
<%
class RemoteIP {
	private String date;
	private int count;
	
	RemoteIP(String date, int count) {
		this.date = date;
		this.count = count;
	}
	public String getDate() {
		return date;
	}
	public int getCount() {
		return count;
	}
}

String s;
String[] temp = new String[2];
BufferedReader in = new BufferedReader(new FileReader("/usr/local/apache-tomcat-9.0.41/webapps/block/remoteIP.txt"));
ArrayList<RemoteIP> datelist = new ArrayList<RemoteIP>();
while ((s = in.readLine())!=null) {
	temp = s.split(" ");
	datelist.add(new RemoteIP(temp[0], Integer.parseInt(temp[1])));
}
int listSize = datelist.size();
%>
<script>
var ctx = document.getElementById('myChart').getContext('2d');
var chart = new Chart(ctx, {
    type: 'line',
    data: {
        labels: [
			<%
			if (datelist.size() > 4) {
				int i = 5;
				while (--i>=0)
					out.print(datelist.get(listSize-i).getDate());
			}
			else {
				int i = listSize;
				while (--i>=0)
					out.print(datelist.get(i).getDate());
			}
			%>
        ],
        datasets: [{
            label: '# of Connection',
            backgroundColor: 'rgb(255, 99, 132)',
            borderColor: 'rgb(255, 99, 132)',
            data: [
    			<%
    			if (datelist.size() > 4) {
    				int i = 5;
    				while (--i>=0) {
    					out.print(datelist.get(listSize-i).getCount());
    					if (i>0) out.println(", ");
    				}
    				
    			}
    			else {
    				int i = listSize;
    				while (--i>=0) {
    					out.print(datelist.get(i).getCount());
						if (i>0) out.println(", ");
    				}
    			}
    			%>
            	]
        }]
    },

    // Configuration options go here
    options: {}
});
</script>
</body>
</html>
