package grisu.frontend;

import grisu.frontend.model.job.JobObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

public class HtmlBenchmarkRenderer implements BenchmarkRenderer {

	private StringBuffer htmlString;
	private StringBuffer tableString;
	private StringBuffer effGraphString;
	private StringBuffer htmlBodyString;
	static int benchmarkCount;

	public HtmlBenchmarkRenderer() {

		// holds the content for the html file to be created
		htmlString = new StringBuffer(
				"<html>"
						+ "\n<head>"
						+ "\n<script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script>"
						+ "\n<script type=\"text/javascript\">"
						+ "\ngoogle.load(\"visualization\", \"1\", {packages:[\"corechart\"]});"
						+ "\ngoogle.setOnLoadCallback(drawChart);"
						+ "\nfunction drawChart() {"
						+ "\nvar options = {"
						+ "\ntitle: 'Benchmarking - Execution time for the job and Total Execution time accross all CPUs',"
						+ "axisTitlesPosition: 'out',"
						+ "hAxis: {title: \"Number of CPUs used for the job\"}"
						+ "\n};" + "\nvar effoptions = {"
						+ "\ntitle: 'Benchmarking - CPUs v/s Efficiency',"
						+ "axisTitlesPosition: 'out',"
						+ "hAxis: {title: \"Number of CPUs used for the job\"}"
						+ "\n};");

		// holds the table tags for printing out all the values for each benchmark job specified
		tableString = new StringBuffer(

		);

		// holds the string for the graph drawing related data for each job
		effGraphString = new StringBuffer();
		//holds the graph placements related data for each job 
		htmlBodyString = new StringBuffer("<table>");
	}

	@Override
	// public void renderer(List<String> jobnames) {
	//populate all the html file related strings for the specified benchmark job
	public void renderer(BenchmarkJob bJob) {

		Long executionTime;
		Long totalExecutionTime;
		String[] jobValues = new String[10];
		int cpus;

		htmlString
				.append("\nvar data = google.visualization.arrayToDataTable(["
						+ "\n['Number of CPUs', 'Execution time for the job', 'Total Execution Time across all CPUs']");

		effGraphString
				.append("\nvar effdata = google.visualization.arrayToDataTable(["
						+ "\n['Number of CPUs', 'Efficiency']");

		tableString.append("<table border=\"1\">" + "<caption>"
				+ bJob.getJobname() + "</caption>" + "<tr>"
				+ "<th>Number of CPUs</th >"
				+ "<th>Execution time for the job</th>"
				+ "<th>Total Execution time across all CPUs</th>"
				+ "<th>Efficiency</th>" + "</tr>");

		for (JobDetailsVO job : bJob.getJobs()) {
			jobValues[0] = job.getJobName();
			jobValues[1] = "" + job.getHostCount();
			jobValues[2] = "" + job.getStatus();

			cpus = job.getCpus();
			jobValues[3] = "" + cpus;
			jobValues[4] = "" + job.getWallTime();

			// executionTime = bJob.getJobs().get(job);
			executionTime = job.getExecutionTime();
			if (executionTime != null) {
				jobValues[5] = "" + executionTime;

				totalExecutionTime = job.getTotalExecutionTime();
				jobValues[6] = "" + totalExecutionTime;

				double efficiency;
				if (bJob.getJobname().endsWith(".csv")) {
					efficiency = job.getEfficiency();
				} else {
					efficiency = (bJob.getMinCpus() * bJob.getMinRunTime()
							.doubleValue()) / totalExecutionTime;
				}
				jobValues[7] = "" + efficiency;
				System.out.println(jobValues[7]);

				htmlString.append(",\n['" + jobValues[3] + "', " + jobValues[5]
						+ ", " + totalExecutionTime + "]");
				effGraphString.append(",\n['" + jobValues[3] + "', "
						+ efficiency + "]");
				tableString.append("<tr><td align=\"right\">" + jobValues[3]
						+ "</td><td align=\"right\">" + jobValues[5]
						+ "</td><td align=\"right\">"
						+ trimDouble(totalExecutionTime)
						+ "</td><td align=\"right\"> " + trimDouble(efficiency)
						+ "</td></tr>");
			}
		}

		htmlString
				.append("\n]);"
						+ "\nvar chart = new google.visualization.LineChart(document.getElementById('chart_div"
						+ benchmarkCount + "'));"
						+ "\nchart.draw(data, options);");
		effGraphString
				.append("\n]);"
						+ "\nvar effchart = new google.visualization.LineChart(document.getElementById('effchart_div"
						+ benchmarkCount + "'));"
						+ "\neffchart.draw(effdata, effoptions);");

		htmlBodyString.append(
				"<tr><td>"+bJob.getJobname()+"</td></tr>"
				+"<tr><td><div id=\"chart_div" + benchmarkCount
				+ "\" style=\"width: 900px; height: 500px;\"></div></td>"
				+ "<td><div id=\"effchart_div" + benchmarkCount
				+ "\" style=\"width: 900px; height: 500px;\"></div></td></tr>"
				);
		
		tableString.append("</table>");
		benchmarkCount++;
	}

	double trimDouble(double d) {
		DecimalFormat df = new DecimalFormat("#.##");
		return Double.valueOf(df.format(d));
	}

	//create the html file for all the job data collected above
	public void populateGraph(StringBuffer graphname) {
		htmlBodyString.append("</table>");
		htmlString.append("" + effGraphString + "\n}" + "\n</script>"
				+ "\n</head>" + "\n<body>" + htmlBodyString + tableString
				+ "\n</body>" + "\n</html>");
		BufferedWriter out;
		try {
			out = new BufferedWriter(new FileWriter(graphname + "graph.html"));
			out.write(new String(htmlString));
			out.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}