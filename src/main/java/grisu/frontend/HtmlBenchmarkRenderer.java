package grisu.frontend;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HtmlBenchmarkRenderer implements BenchmarkRenderer {

	private StringBuffer htmlString;
	private StringBuffer tableString;
	private StringBuffer effGraphString;
	private StringBuffer htmlBodyString;
	static int benchmarkCount;

	private List<Integer> cpuVals = new ArrayList<Integer>();
	private Map<Integer, HashMap<Integer, Long>> execTimeVals = new HashMap<Integer, HashMap<Integer, Long>>();
	private Map<Integer, HashMap<Integer, Double>> effVals = new HashMap<Integer, HashMap<Integer, Double>>();
	private Map<Integer, HashMap<Integer, Long>> totExecTimeVals = new HashMap<Integer, HashMap<Integer, Long>>();

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

		// holds the table tags for printing out all the values for each
		// benchmark job specified
		tableString = new StringBuffer();

		// holds the string for the graph drawing related data for each job
		effGraphString = new StringBuffer();

		// holds the graph placements related data for each job
		htmlBodyString = new StringBuffer("<table>");
	}

	// populate all the html file related strings for the specified benchmark
	// job
	@Override
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

		HashMap<Integer, Long> tempMap = new HashMap<Integer, Long>();
		HashMap<Integer, Double> tempMap2 = new HashMap<Integer, Double>();
		HashMap<Integer, Long> tempMap3 = new HashMap<Integer, Long>();

		for (JobDetailsVO job : bJob.getJobs()) {
			executionTime = job.getExecutionTime();
			jobValues[2] = "" + job.getStatus();
			if (executionTime != null && jobValues[2].equalsIgnoreCase("TRUE")) 
			{
				jobValues[0] = job.getJobName();
				jobValues[1] = "" + job.getHostCount();

				cpus = job.getCpus();
				jobValues[3] = "" + cpus;
				jobValues[4] = "" + job.getWallTime();

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

				if (benchmarkCount == 0) {
					if (!cpuVals.contains(cpus)) {
						cpuVals.add(cpus);
						tempMap.put(cpus, executionTime);
						tempMap2.put(cpus, efficiency);
						tempMap3.put(cpus, totalExecutionTime);
					}
				} else {
					if (cpuVals.contains(cpus)) {
						tempMap.put(cpus, executionTime);
						tempMap2.put(cpus, efficiency);
						tempMap3.put(cpus, totalExecutionTime);
					}
				}
			}
		}
		execTimeVals.put(benchmarkCount, tempMap);
		effVals.put(benchmarkCount, tempMap2);
		totExecTimeVals.put(benchmarkCount, tempMap3);

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

		htmlBodyString.append("<tr><td>" + bJob.getJobname() + "</td></tr>"
				+ "<tr><td><div id=\"chart_div" + benchmarkCount
				+ "\" style=\"width: 900px; height: 500px;\"></div></td>"
				+ "<td><div id=\"effchart_div" + benchmarkCount
				+ "\" style=\"width: 900px; height: 500px;\"></div></td></tr>");

		tableString.append("</table>");
		benchmarkCount++;
	}

	double trimDouble(double d) {
		DecimalFormat df = new DecimalFormat("#.##");
		return Double.valueOf(df.format(d));
	}

	// create the html file for all the job data collected above
	public void populateGraph(StringBuffer graphname, String chartType) {
		String varDataDeclaration = " var data = google.visualization.arrayToDataTable(["
				+ "\n['CPUs'";
		StringBuffer combinedGraphString = new StringBuffer(varDataDeclaration);
		StringBuffer combinedGraphString2 = new StringBuffer(varDataDeclaration);
		StringBuffer combinedGraphString3 = new StringBuffer(varDataDeclaration);

		for (int i = 0; i < benchmarkCount; i++) {
			combinedGraphString.append(", 'Job " + i + "'");
			combinedGraphString2.append(", 'Job " + i + "'");
			combinedGraphString3.append(", 'Job " + i + "'");
		}
		combinedGraphString.append("]");
		combinedGraphString2.append("]");
		combinedGraphString3.append("]");

		int cpuVal;
		Long execTimeVal;
		Long totExecTimeVal;
		double effVal;
		for (int cpuIndex = 0; cpuIndex < cpuVals.size(); cpuIndex++) {
			cpuVal = cpuVals.get(cpuIndex);
			combinedGraphString.append(",\n[ '" + cpuVal + "'");
			combinedGraphString2.append(",\n[ '" + cpuVal + "'");
			combinedGraphString3.append(",\n[ '" + cpuVal + "'");

			for (int jobIndex = 0; jobIndex < benchmarkCount; jobIndex++) {
				try {
					execTimeVal = execTimeVals.get(jobIndex).get(cpuVal);
					effVal = effVals.get(jobIndex).get(cpuVal);
					totExecTimeVal = totExecTimeVals.get(jobIndex).get(cpuVal);
					combinedGraphString.append(", " + execTimeVal);
					combinedGraphString2.append(", " + effVal);
					combinedGraphString3.append(", " + totExecTimeVal);
				} catch (NullPointerException e) {
					combinedGraphString.append(", 0");
					combinedGraphString2.append(", 0");
					combinedGraphString3.append(", 0");
				}
			}
			combinedGraphString.append("]");
			combinedGraphString2.append("]");
			combinedGraphString3.append("]");
		}

		combinedGraphString.append("\n]);" + "\nvar options = {"
				+ "\ntitle: 'Execution Time for each benchmark job',"
				+ "axisTitlesPosition: 'out',"
				+ "hAxis: {title: \"Number of CPUs used for the job\"}"
				+ "\n};" + "\nvar chart = new google.visualization."
				+ chartType + "(document.getElementById('chart_combi_div'));"
				+ "\nchart.draw(data, options);");

		combinedGraphString2
				.append("\n]);"
						+ "\nvar options = {"
						+ "\ntitle: 'Total Execution Time across all CPUs for each benchmark job',"
						+ "axisTitlesPosition: 'out',"
						+ "hAxis: {title: \"Number of CPUs used for the job\"}"
						+ "\n};" + "\nvar chart = new google.visualization."
						+ chartType
						+ "(document.getElementById('effchart_combi_div'));"
						+ "\nchart.draw(data, options);");

		combinedGraphString3.append("\n]);" + "\nvar effoptions = {"
				+ "\ntitle: 'CPUs v/s Efficiency for each benchmak jobs',"
				+ "axisTitlesPosition: 'out',"
				+ "hAxis: {title: \"Number of CPUs used for the job\"}"
				+ "\n};" + "\nvar chart = new google.visualization."
				+ chartType + "(document.getElementById('chart_combi_div2'));"
				+ "\nchart.draw(data, effoptions);");

		htmlBodyString
				.append("<tr><td>combined chart</td></tr>"
						+ "<tr><td><div id=\"chart_combi_div\" style=\"width: 900px; height: 500px;\"></div></td>"
						+ "<td><div id=\"chart_combi_div2\" style=\"width: 900px; height: 500px;\"></div></td>"
						+ "<td><div id=\"effchart_combi_div\" style=\"width: 900px; height: 500px;\"></div></td></tr>"
						+ "</table>");

		htmlString.append("" + effGraphString + combinedGraphString
				+ combinedGraphString2 + combinedGraphString3 + "\n}"
				+ "\n</script>" + "\n</head>" + "\n<body>" + htmlBodyString
				+ tableString + "\n</body>" + "\n</html>");

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