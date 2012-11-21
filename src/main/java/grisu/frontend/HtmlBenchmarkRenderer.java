package grisu.frontend;

import grisu.frontend.model.job.JobObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

public class HtmlBenchmarkRenderer implements BenchmarkRenderer {

	@Override
	public void renderer(BenchmarkJob bJob) {

		StringBuffer htmlString = new StringBuffer(
				"<html>"
						+ "\n<head>"
						+ "\n<script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script>"
						+ "\n<script type=\"text/javascript\">"
						+ "\ngoogle.load(\"visualization\", \"1\", {packages:[\"corechart\"]});"
						+ "\ngoogle.setOnLoadCallback(drawChart);"
						+ "\nfunction drawChart() {"
						+ "\nvar data = google.visualization.arrayToDataTable(["
						+ "\n['Number of CPUs', 'Execution time for the job', 'Total Execution Time across all CPUs']");

		StringBuffer tableString = new StringBuffer("<table border=\"1\">"
				+ "<tr>" + "<th>Number of CPUs</th>"
				+ "<th>Execution time for the job</th>"
				+ "<th>Total Execution time across all CPUs</th>"
				+ "<th>Efficiency</th>" + "</tr>");

		StringBuffer effGraphString = new StringBuffer(
				"\nvar effdata = google.visualization.arrayToDataTable(["
						+ "\n['Number of CPUs', 'Efficiency']");

		Long executionTime;
		Long totalExecutionTime;
		String[] jobValues = new String[10];
		int cpus;

		for (JobObject job : bJob.getJobs().keySet()) {
			executionTime = bJob.getJobs().get(job);
			if (executionTime != null) {
				jobValues[1] = "" + job.getHostCount();
				jobValues[2] = "" + job.isSuccessful(true);

				cpus = job.getCpus();
				jobValues[3] = "" + cpus;
				jobValues[4] = "" + job.getWalltimeInSeconds();

				jobValues[5] = "" + executionTime;

				totalExecutionTime = (executionTime * cpus);
				jobValues[6] = "" + (executionTime * cpus);

				double efficiency = (bJob.getMinCpus() * bJob.getMinRunTime().doubleValue())
						/ totalExecutionTime;
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

		effGraphString.append("\n]);");
		tableString.append("</table>");
		htmlString.append("\n]);"
						+ "\nvar options = {"
						+ "\ntitle: 'Benchmarking - Execution time for the job and Total Execution time accross all CPUs',"
						+ "axisTitlesPosition: 'out',"
						+ "hAxis: {title: \"Number of CPUs used for the job\"}"
						+ "\n};"
						+ "\nvar effoptions = {"
						+ "\ntitle: 'Benchmarking - CPUs v/s Efficiency',"
						+ "axisTitlesPosition: 'out',"
						+ "hAxis: {title: \"Number of CPUs used for the job\"}"
						+ "\n};"
						+ effGraphString
						+ "\nvar chart = new google.visualization.LineChart(document.getElementById('chart_div'));"
						+ "\nchart.draw(data, options);"
						+ "\nvar effchart = new google.visualization.LineChart(document.getElementById('effchart_div'));"
						+ "\neffchart.draw(effdata, effoptions);"
						+ "\n}"
						+ "\n</script>"
						+ "\n</head>"
						+ "\n<body>"
						+ "\n<div id=\"chart_div\" style=\"width: 900px; height: 500px;\"></div>"
						+ "\n<div id=\"effchart_div\" style=\"width: 900px; height: 500px;\"></div>"
						+ tableString + "\n</body>" + "\n</html>");
		BufferedWriter out;
		try {
			out = new BufferedWriter(new FileWriter(bJob.getJobname()
					+ "_graph.html"));
			out.write(new String(htmlString));
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