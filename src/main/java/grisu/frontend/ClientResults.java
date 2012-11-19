package grisu.frontend;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import au.com.bytecode.opencsv.CSVWriter;

import grisu.control.ServiceInterface;
import grisu.control.exceptions.JobPropertiesException;
import grisu.control.exceptions.JobSubmissionException;
import grisu.control.exceptions.NoSuchJobException;
import grisu.frontend.control.login.LoginManager;
import grisu.frontend.model.job.JobException;
import grisu.frontend.model.job.JobObject;
import grisu.frontend.view.cli.GrisuCliClient;
import grisu.jcommons.constants.Constants;
import grisu.model.FileManager;
import grisu.model.GrisuRegistryManager;
import grisu.model.UserEnvironmentManager;
import grisu.model.dto.DtoJob;

public class ClientResults extends GrisuCliClient<ExampleCliParameters> {

	public static void main(String[] args) {

		// basic housekeeping
		LoginManager.initGrisuClient("grisu-benchmark");

		// helps to parse commandline arguments, if you don't want to create
		// your own parameter class, just use DefaultCliParameters
		ExampleCliParameters params = new ExampleCliParameters();
		// create the client
		ClientResults client = null;
		try {
			client = new ClientResults(params, args);
		} catch(Exception e) {
			System.err.println("Could not start grisu-benchmark: "
					+ e.getLocalizedMessage());
			System.exit(1);
		}

		// finally:
		// execute the "run" method below
		client.run();

		// exit properly
		System.exit(0);

	}

	public ClientResults(ExampleCliParameters params, String[] args) throws Exception {
		super(params, args);
	}

	@Override
	public void run() {

		String jobname = getCliParameters().getJobName();

		CSVWriter writer = null;
		String[] csvTemp = new String[10];

		csvTemp[0]="Job name";
		csvTemp[1]="Host count";
		csvTemp[2]="Job success status";
		csvTemp[3]="CPUs";
		csvTemp[4]="Wall time";
		csvTemp[5]="Job execution time";
		csvTemp[6]="Average execution time per CPU";
		csvTemp[7]="Efficiency";

		StringBuffer htmlString=new StringBuffer("<html>"+
				"\n<head>"+
				"\n<script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script>"+
				"\n<script type=\"text/javascript\">"+
				"\ngoogle.load(\"visualization\", \"1\", {packages:[\"corechart\"]});"+
				"\ngoogle.setOnLoadCallback(drawChart);"+
				"\nfunction drawChart() {"+
				"\nvar data = google.visualization.arrayToDataTable(["+
				"\n['Number of CPUs', 'Total Execution time for the job', 'Execution Time per CPU (ms)', 'Efficiency']");

		StringBuffer tableString=new StringBuffer("<table border=\"1\">"+
				"<tr>"+
				"<th>Job name</th>"+
				"<th>Number of CPUs</th>"+
				"<th>Total Execution time for the job (ms)</th>"+
				"<th>Execution time per CPU</th>"+
				"<th>Efficiency</th>"+
				"</tr>");


		try {
			writer = new CSVWriter(new FileWriter(jobname+".csv"));
			writer.writeNext(csvTemp);

		} catch (IOException e) {
			e.printStackTrace();
		}



		System.out.println("Getting serviceinterface...");
		ServiceInterface si = null;
		try {
			si = getServiceInterface();
		} catch (Exception e) {
			System.err.println("Could not login: " + e.getLocalizedMessage());
			System.exit(1);
		}

		FileManager fm = GrisuRegistryManager.getDefault(si).getFileManager();
		UserEnvironmentManager uem = GrisuRegistryManager.getDefault(si).getUserEnvironmentManager();
		SortedSet<DtoJob> currentJobs = uem.getCurrentJobs(true);
		SortedSet<String> currentJobList = uem.getCurrentJobnames(true);
		Boolean flag=true;
		List<JobObject> downloadStatus=new ArrayList<JobObject>();
		int cpuCount=0;
		long totalExecTime=0;
		int minCpu=9999;
		int minCpuWallTime=0;
		int temp;
		jobname=jobname+"_";
		while(flag)
		{
			flag=false;
			for(String jname:currentJobList)
			{
				if(jname.contains(jobname)){
					try 
					{
						JobObject job = new JobObject(si, jname);

						if(!job.isFinished())
						{
							flag=true;
						}
						else
						{
							if(!downloadStatus.contains(job))
							{
								temp=Integer.parseInt(job.getCpus().toString());
								if(temp<minCpu){
									minCpu=temp;
									minCpuWallTime=job.getWalltimeInSeconds();
								}
								downloadStatus.add(job);
							}
						}
					} catch (NoSuchJobException e) {
						e.printStackTrace();
					}
					catch (NullPointerException e) {
					}

				}

			}
		}
		System.out.println("minimum no. of CPUs: "+minCpu);
		System.out.println("minimum wall time: "+minCpuWallTime);

		Collections.sort(downloadStatus, new Comparator<JobObject>(){
			public int compare(JobObject j1, JobObject j2)
			{
				return j1.getCpus()-j2.getCpus();
			}
		});

		for(JobObject job: downloadStatus)
		{
			System.out.println("Jobname:"+job.getJobname());
			csvTemp[0]=job.getJobname();
			System.out.println("Number of CPUs:"+job.getCpus());
			System.out.println("Host count:"+job.getHostCount());
			csvTemp[1]=job.getHostCount().toString();
			//				try{
			//					System.out.println("Stdout:"+job.getStdOutContent());
			//					csvTemp[2]=job.getStdOutContent();
			//				}
			//				catch(JobException je){
			//				//	je.printStackTrace();
			//					csvTemp[2]="Could not read stdout file";
			//				}
			//				try
			//				{
			//					System.out.println("Stderr:"+job.getStdErrContent());
			//					csvTemp[3]=job.getStdErrContent();
			//				}
			//				catch(JobException je){
			//				//	je.printStackTrace();
			//					csvTemp[3]="Could not read stderr file";
			//				}
			System.out.println("job succeeded:"+job.isSuccessful(true));
			csvTemp[2]=String.valueOf(job.isSuccessful(true));

			//	    writer=new CSVWriter(new FileWriter(jobname+".csv"));

			//		writer.close();
			temp=job.getCpus();
			csvTemp[3]=""+temp;
			csvTemp[4]=""+job.getWalltimeInSeconds();

			try{
				String jobLog=job.getFileContent("benchmark.log");
				String holder=jobLog.substring(jobLog.lastIndexOf("Started:"));
				Long long1=Long.parseLong(holder.substring(9,holder.indexOf("\n")));
				holder=jobLog.substring(jobLog.lastIndexOf("Finished:"));
				Long long2=Long.parseLong(holder.substring(10,holder.indexOf("\n")));

				System.out.println("Time taken for execution:"+(long2-long1) +"ms");
				csvTemp[5]=(long2-long1)+" ms";
				totalExecTime=(long2-long1);

				csvTemp[6]=""+((Long)totalExecTime).doubleValue()/((Integer)temp).doubleValue();
				System.out.println(csvTemp[6]);
				csvTemp[7]=""+ ((Integer)(minCpuWallTime)).doubleValue()/((Long)((minCpu) * (temp * totalExecTime))).doubleValue();
				System.out.println(csvTemp[7]);

				writer.writeNext(csvTemp);
				htmlString.append(",\n['"+temp+"', "+totalExecTime+", "+Double.parseDouble(csvTemp[6])+", "+Double.parseDouble(csvTemp[7])+"]");
				tableString.append("<tr><td>"+csvTemp[0]+"</td><td>"+temp+"</td><td>"+totalExecTime+"</td><td>"+trimDouble(Double.parseDouble(csvTemp[6]))+"</td><td> "+trimDouble(Double.parseDouble(csvTemp[7]))+"</td></tr>");

			}
			catch(Exception e)
			{
				System.out.println("job failed");
			}

		}

		//		csvTemp[0]="Total number of CPUs="+cpuCount;
		//		System.out.println(csvTemp[0]);
		//		csvTemp[1]="Total execution time="+totalExecTime;
		//		System.out.println(csvTemp[1]);
		//		csvTemp[2]="average execution time per CPU="+(totalExecTime/cpuCount);
		//		System.out.println(csvTemp[2]);
		//		csvTemp[3]="Efficiency="+ ((Integer)(minCpuWallTime)).doubleValue()/((Integer)((minCpu) * (cpuCount * totalExecTime))).doubleValue();
		//		System.out.println(csvTemp[3]);
		//		csvTemp[4]=csvTemp[5]=csvTemp[6]=null;
		//		writer.writeNext(csvTemp);
		tableString.append("</table>");
		htmlString.append("\n]);"+
				"\nvar options = {"+
				"\ntitle: 'Benchmarking',"+
				"axisTitlesPosition: 'out',"+
				"hAxis: {title: \"Number of CPUs used for the job\"}"+
				"\n};"+
				"\nvar chart = new google.visualization.LineChart(document.getElementById('chart_div'));"+
				"\nchart.draw(data, options);"+
				"\n}"+
				"\n</script>"+
				"\n</head>"+
				"\n<body>"+
				"\n<div id=\"chart_div\" style=\"width: 900px; height: 500px;\"></div>"+
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
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	double trimDouble(double d) {
        DecimalFormat df = new DecimalFormat("#.##");
    return Double.valueOf(df.format(d));
}
}
