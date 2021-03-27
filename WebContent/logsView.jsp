<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8" http-equiv="refresh" content="60">
	<script src="https://cdn.jsdelivr.net/npm/chart.js@2.8.0"></script>
	<script src="https://code.jquery.com/jquery-3.5.1.min.js"></script>
	<script src="https://cdn.jsdelivr.net/npm/chartjs-plugin-piechart-outlabels"></script>
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
<div class="row mt-5 mb-5">
	<div class="col-sm-9">
		<div class="row file">
			<div class="lead"><a href="logsView.jsp" class="link-secondary text-decoration-none">HTTP 클라이언트 접속 정보</a>&nbsp;&nbsp;&nbsp;<a href="logout.jsp" class="link-secondary text-decoration-none">logout</a></div>
		</div>
		<div class="row mt-2">
			<div class="display-3" style="font-family: 'Nanum Gothic', sans-serif;">HTTP 클라이언트 접속 정보</div>
		</div>
	</div>
</div>
<div class="row mt-5">
	<div class="col-sm-5 shadow-sm bg-light rounded"><canvas id="pconn"></canvas></div>
	<div class="col-sm-1"></div>
	<div class="col-sm-5 shadow-sm bg-light rounded"><canvas id="tconn"></canvas></div>
</div>
<div class="row mt-5">
	<div class="col-sm-5 shadow-sm bg-light rounded"><canvas id="lconn"></canvas></div>
	<div class="col-sm-1"></div>
	<div class="col-sm-5 shadow-sm bg-light rounded"><canvas id="econn"></canvas></div>
</div>
<div class="row mt-5" style="display:none">
	<div class="col-sm-1"><button type="button" class="btn btn-secondary" id="time">time</button></div>
	<div class="col-sm-1"><button type="button" class="btn btn-secondary" id="load">load</button></div>
	<div class="col-sm-1 dropdown">
	<button class="btn btn-secondary dropdown-toggle" type="button" id="dropdownMenuButton1" data-bs-toggle="dropdown" aria-expanded="false">
		Drop
	</button>
	<ul class="dropdown-menu" aria-labelledby="dropdownMenuButton1">
		<li><a class="dropdown-item person" id="day" href="#">day</a></li>
	    <li><a class="dropdown-item person" id="month" href="#">month</a></li>
	</ul>
	</div>
	<div class="col-sm-1"><button type="button" class="btn btn-secondary" id="err">error</button></div>
</div>
</div>
<script>
$(function() {
	const PCONN = $("#pconn");
	const TCONN = $('#tconn');
	const LCONN = $('#lconn');
	const ECONN = $('#econn');
	$('.person').on('click', function() {
		$.ajax({
            url: "<%=request.getContextPath()%>/LogsView",
			method: "POST",
			data: {chart:$(this).attr('id')},
			dataType: "json"
		})
        .done(function(json) {
        	const orderedDay = sortResults(json);
        	var lineChart = new Chart(PCONN, {
				type: 'line',
				data: {
					labels: Object.keys(ordered),
					datasets: [{
						label: '# of IP (Day)',
						fill: false,
						lineTension: 0,
						backgroundColor: "rgba(235, 159, 159, 0.4)",
						borderColor: "rgba(235, 159, 159, 1)",
						borderCapStyle: 'butt',
						borderDash: [],
						borderDashOffset: 0.0,
						borderJointStyle: 'miter',
						data: Object.values(ordered)
					}]
        	   },
				options: {
					responsive: true,
					title: {
        	            display: true,
						text: 'Connection (Day/Month)'
					}
				}
        	})
        })
        .fail(function(xhr, status, errorThrown) {
            alert("오류가 발생했습니다.");
        })
    })
	$('#time').on('click', function() {
		$.ajax({
            url: "<%=request.getContextPath()%>/LogsView",
			method: "POST",
			data: {chart:$(this).attr('id')},
			dataType: "json"
		})
        .done(function(json) {
        	const ordered = sortResults(json);
        	const time24 = ["0-2", "3-5", "6-8", "9-11", "12-14", "15-17", "18-20", "21-23"];
        	var lineChart = new Chart(TCONN, {
				type: 'pie',
				data: {
					labels: time24,
					datasets: [{
						label: '# of connection per Today',
						fill: false,
						lineTension: 0,
						backgroundColor: [
							'rgba(51, 149, 229, 0.4)',
							'rgba(81, 142, 219, 0.4)',
							'rgba(112, 136, 210, 0.4)',
							'rgba(82, 80, 192, 0.4)',
							'rgba(143, 130, 201, 0.4)',
							'rgba(174, 123, 192, 0.4)',
							'rgba(205, 117, 183, 0.4)',
							'rgba(236, 111, 174, 0.4)',
						],
        	         	borderColor: [
        	         		'rgba(51, 149, 229, 0.4)',
							'rgba(81, 142, 219, 0.4)',
        	         		'rgba(112, 136, 210, 0.4)',
        	         		'rgba(82, 80, 192, 0.4)',
							'rgba(143, 130, 201, 0.4)',
        	         		'rgba(174, 123, 192, 0.4)',
							'rgba(205, 117, 183, 0.4)',
        	         		'rgba(236, 111, 174, 0.4)'
        	         	],
        	         	borderCapStyle: 'butt',
        	         	borderDash: [],
        	         	borderDashOffset: 0.0,
        	         	borderJointStyle: 'miter',
        	         	data: Object.values(ordered)
					}]
				},
				options: {
					responsive: true,
					title: {
        	            display: true,
        	            padding: 20,
						text: 'Connection (24h)'
					},
					plugins: {
				        legend: false,
				        outlabels: {
				           text: '%l %p',
				           color: 'white',
				           stretch: 0,
				           font: {
				               resizable: true,
				               minSize: 12,
				               maxSize: 18
				           }
				        }
				     }
				}
			})
        })
        .fail(function(xhr, status, errorThrown) {
            alert("오류가 발생했습니다.");
        })
    })
	$('#load').on('click', function() {
		$.ajax({
            url: "<%=request.getContextPath()%>/LogsView",
			method: "POST",
			data: {chart:$(this).attr('id')},
			dataType: "json"
		})
        .done(function(json) {
        	const orderedUp = sortResults(json['upload']);
        	const orderedDown = sortResults(json['download']);
        	var lineChart = new Chart(LCONN, {
				type: 'line',
				data: {
					labels: Object.keys(orderedUp),
					datasets: [{
						label: '# of Upload',
						fill: false,
						lineTension: 0,
						backgroundColor: "rgba(75,192,192,0.4)",
						borderColor: "rgba(75,192,192,1)",
						borderCapStyle: 'butt',
						borderDash: [],
						borderDashOffset: 0.0,
						borderJointStyle: 'miter',
						data: Object.values(orderedUp)
					},
					{
						label: '# of Download',
						fill: false,
						lineTension: 0,
						backgroundColor: "rgba(137,119,173,0.4)",
						borderColor: "rgba(137,119,173,1)",
						borderCapStyle: 'butt',
						borderDash: [],
						borderDashOffset: 0.0,
						borderJointStyle: 'miter',
 						data: Object.values(orderedDown)
					}]
        		},
				options: {
					responsive: true,
					title: {
        	            display: true,
						text: 'Upload / Download'
					},
				}
        	})
        })
        .fail(function(xhr, status, errorThrown) {
            alert("오류가 발생했습니다.");
        })
	})
	$('#err').on('click', function() {
		$.ajax({
            url: "<%=request.getContextPath()%>/LogsView",
			method: "POST",
			data: {chart:$(this).attr('id')},
			dataType: "json"
		})
        .done(function(json) {
        	const ordered = sortResults(json);
        	var lineChart = new Chart(ECONN, {
				type: 'line',
				data: {
					labels: Object.keys(ordered),
					datasets: [{
						label: '# of Status',
						fill: false,
						lineTension: 0,
						backgroundColor: "rgba(246, 234, 140, 0.4)",
						borderColor: "rgba(246, 234, 140, 1)",
						borderCapStyle: 'butt',
						borderDash: [],
						borderDashOffset: 0.0,
						borderJointStyle: 'miter',
						data: Object.values(ordered)
					}]
        		},
				options: {
					responsive: true,
					title: {
        	            display: true,
						text: 'Blockchain Status'
					}
				}
        	})
        })
        .fail(function(xhr, status, errorThrown) {
            alert("오류가 발생했습니다.");
        })
	})
	$('#day').trigger("click");
	$('#time').trigger("click");
	$('#load').trigger("click");
	$('#err').trigger("click");
	
	function sortResults(json) { // sort graph by date 
		const ordered={};
    	Object.keys(json).sort().forEach(function(key) {
      	  ordered[key] = json[key];
      	});
    	return ordered;
	}
})
</script>
</body>
</html>
