package grisu.frontend;

import grisu.frontend.model.job.JobObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

public class HtmlBenchmarkRenderer implements BenchmarkRenderer
{

	@Override
	public void renderer(String jobname, List<String[]> jobs, int minCpus, Long minRuntime) {
		
		StringBuffer htmlString=new StringBuffer("<html>"+
				"\n<head>"+
				"\n<script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script>"+
				"\n<script type=\"text/javascript\">"+
				"\ngoogle.load(\"visualization\", \"1\", {packages:[\"corechart\"]});"+
				"\ngoogle.setOnLoadCallback(drawChart);"+
				"\nfunction drawChart() {"+
				"\nvar data = google.visualization.arrayToDataTable(["+
				"\n['Number of CPUs', 'Execution time for the job', 'Total Execution Time across all CPUs']");

		StringBuffer tableString=new StringBuffer("<table border=\"1\">"+
				"<tr>"+
//				"<th>Job name</th>"+
				"<th>Number of CPUs</th>"+
				"<th>Execution time for the job</th>"+
				"<th>Total Execution time across all CPUs</th>"+
				"<th>Efficiency</th>"+
				"</tr>");

		StringBuffer effGraphString=new StringBuffer("\nvar effdata = google.visualization.arrayToDataTable(["+
				"\n['Number of CPUs', 'Efficiency']");
		
		
		for ( String[] values : jobs ) {

			double efficiency = minRuntime.doubleValue()/(minCpus * Long.parseLong(values[6]));
			values[7]=""+ efficiency;
			System.out.println(values[7]);

			htmlString.append(",\n['"+values[3]+"', "+values[5]+", "+Double.parseDouble(values[6])+"]");
			effGraphString.append(",\n['"+values[3]+"', "+Double.parseDouble(values[7])+"]");
//			tableString.append("<tr><td>"+values[0]+"</td><td align=\"right\">"+values[3]+"</td><td align=\"right\">"+values[5]+"</td><td align=\"right\">"+trimDouble(Double.parseDouble(values[6]))+"</td><td align=\"right\"> "+trimDouble(Double.parseDouble(values[7]))+"</td></tr>");
			tableString.append("<tr><td align=\"right\">"+values[3]+"</td><td align=\"right\">"+values[5]+"</td><td align=\"right\">"+trimDouble(Double.parseDouble(values[6]))+"</td><td align=\"right\"> "+trimDouble(Double.parseDouble(values[7]))+"</td></tr>");
		}
	
		
		effGraphString.append("\n]);");
		tableString.append("</table>");
		htmlString.append("\n]);"+
				"\nvar options = {"+
				"\ntitle: 'Benchmarking - Execution time for the job (ms) and Total Execution time accross all CPUs',"+
				"axisTitlesPosition: 'out',"+
				"hAxis: {title: \"Number of CPUs used for the job\"}"+
				"\n};"+
				"\nvar effoptions = {"+
				"\ntitle: 'Benchmarking - CPUs v/s Efficiency',"+
				"axisTitlesPosition: 'out',"+
				"hAxis: {title: \"Number of CPUs used for the job\"}"+
				"\n};"+
				effGraphString+
				"\nvar chart = new google.visualization.LineChart(document.getElementById('chart_div'));"+
				"\nchart.draw(data, options);"+
				"\nvar effchart = new google.visualization.LineChart(document.getElementById('effchart_div'));"+
				"\neffchart.draw(effdata, effoptions);"+
				"\n}"+
				"\n</script>"+
				"\n</head>"+
				"\n<body>"+
				"\n<div id=\"chart_div\" style=\"width: 900px; height: 500px;\"></div>"+
				"\n<div id=\"effchart_div\" style=\"width: 900px; height: 500px;\"></div>"+
				tableString+
				"\n</body>"+
				"\n</html>");
		BufferedWriter out;
		try {
			out = new BufferedWriter(new FileWriter(jobname+"graph.html"));
			out.write(new String(htmlString));
			//Close the output stream
			out.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	double trimDouble(double d) {
		DecimalFormat df = new DecimalFormat("#.##");
		return Double.valueOf(df.format(d));
	}

}