package grisu.frontend;

import grisu.control.ServiceInterface;
import grisu.control.exceptions.NoSuchJobException;
import grisu.frontend.control.login.LoginManager;
import grisu.frontend.model.job.JobObject;
import grisu.frontend.view.cli.GrisuCliClient;
import grisu.model.FileManager;
import grisu.model.GrisuRegistryManager;
import grisu.model.UserEnvironmentManager;
import grisu.model.dto.DtoJob;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;

import au.com.bytecode.opencsv.CSVWriter;

import com.google.common.collect.Lists;

public class ClientResults extends GrisuCliClient<ClientResultsParams> {
	
	private static final int waittime = 10;

	public static void main(String[] args) {

		// basic housekeeping
		LoginManager.initGrisuClient("grisu-benchmark");

		// helps to parse commandline arguments, if you don't want to create
		// your own parameter class, just use DefaultCliParameters
		ClientResultsParams params = new ClientResultsParams();
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

	public ClientResults(ClientResultsParams params, String[] args) throws Exception {
		super(params, args);
	}

	@Override
	public void run() {

		String jobname = getCliParameters().getJobName();
		
		boolean nowait = getCliParameters().getNowait();
				

		CSVWriter writer = null;
		CSVWriter errWriter = null;
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
				"\n['Number of CPUs', 'Execution time for the job', 'Total Execution Time across all CPUs', 'Efficiency']");

		StringBuffer tableString=new StringBuffer("<table border=\"1\">"+
				"<tr>"+
				"<th>Job name</th>"+
				"<th>Number of CPUs</th>"+
				"<th>Execution time for the job</th>"+
				"<th>Total Execution time across all CPUs</th>"+
				"<th>Efficiency</th>"+
				"</tr>");


		try {
			writer = new CSVWriter(new FileWriter(jobname+".csv"));
			errWriter = new CSVWriter(new FileWriter(jobname+"_err.csv"));
			writer.writeNext(csvTemp);
			csvTemp[5]=csvTemp[6]=csvTemp[7]=null;
			errWriter.writeNext(csvTemp);

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
		Long totalExecTime=0L;
		jobname=jobname+"_";
		while(flag)
		{
			flag=false;
			for(String jname:currentJobList)
			{
				
				if(jname.contains(jobname)){
					System.out.println("Checking job: "+jname);
					try 
					{
						JobObject job = new JobObject(si, jname);

						if(!job.isFinished())
						{
							System.out.println("\tNot finished.");
							if ( !nowait) {
								flag=true;
							}
						}
						else
						{
							System.out.println("\tFinished.");
							if(!downloadStatus.contains(job))
							{

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
			// wait a few seconds, so we don't overload the backend
			if ( flag ) {
				try {
					Thread.sleep(waittime*1000);
				} catch (InterruptedException e) {
				}
			}

		}


		Collections.sort(downloadStatus, new Comparator<JobObject>(){
			public int compare(JobObject j1, JobObject j2)
			{
				return j1.getCpus()-j2.getCpus();
			}
		});
		
		List<String[]> allJobs = Lists.newArrayList();

		Integer minCpu=9999;
		Long minCpuWallTime=0L;

		for(JobObject job: downloadStatus)
		{
			String[] valuesForJob = new String[10];

			
			System.out.println("Jobname:"+job.getJobname());
			valuesForJob[0]=job.getJobname();
			System.out.println("Number of CPUs:"+job.getCpus());
			System.out.println("Host count:"+job.getHostCount());
			valuesForJob[1]=job.getHostCount().toString();
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
			valuesForJob[2]=String.valueOf(job.isSuccessful(true));

			//	    writer=new CSVWriter(new FileWriter(jobname+".csv"));

			//		writer.close();
			Integer cpus=job.getCpus();
			valuesForJob[3]=""+cpus;
			valuesForJob[4]=""+job.getWalltimeInSeconds();

			try{
				String jobLog=job.getFileContent("benchmark.log");
				String holder=jobLog.substring(jobLog.lastIndexOf("Started:"));
				Long long1=Long.parseLong(holder.substring(9,holder.indexOf("\n")));
				holder=jobLog.substring(jobLog.lastIndexOf("Finished:"));
				Long long2=Long.parseLong(holder.substring(10,holder.indexOf("\n")));

				System.out.println("Time taken for execution:"+(long2-long1));
				valuesForJob[5]=""+(long2-long1);
				totalExecTime=(long2-long1);
				Long totalAllCpus = totalExecTime * cpus;
				valuesForJob[6]=""+totalAllCpus;
				System.out.println(valuesForJob[6]);

//				writer.writeNext(valueNames);
//				htmlString.append(",\n['"+cpus+"', "+totalExecTime+", "+Double.parseDouble(valuesForJob[6])+", "+Double.parseDouble(valuesForJob[7])+"]");
//				tableString.append("<tr><td>"+valuesForJob[0]+"</td><td align=\"right\">"+cpus+"</td><td align=\"right\">"+totalExecTime+"</td><td align=\"right\">"+trimDouble(Double.parseDouble(valuesForJob[6]))+"</td><td align=\"right\"> "+trimDouble(Double.parseDouble(valuesForJob[7]))+"</td></tr>");

				allJobs.add(valuesForJob);
				
				if ( cpus < minCpu ) {
					minCpu = cpus;
					minCpuWallTime = totalExecTime;
				}
				
			}
			catch(Exception e)
			{
				System.out.println("job failed");
			}
			
		}

		for ( String[] values : allJobs ) {

			double efficiency = minCpuWallTime.doubleValue()/(minCpu * Long.parseLong(values[6]));
			values[7]=""+ efficiency;
			System.out.println(values[7]);
			
			writer.writeNext(values);
			htmlString.append(",\n['"+values[3]+"', "+values[5]+", "+Double.parseDouble(values[6])+", "+Double.parseDouble(values[7])+"]");
			tableString.append("<tr><td>"+values[0]+"</td><td align=\"right\">"+values[3]+"</td><td align=\"right\">"+values[5]+"</td><td align=\"right\">"+trimDouble(Double.parseDouble(values[6]))+"</td><td align=\"right\"> "+trimDouble(Double.parseDouble(values[7]))+"</td></tr>");

			
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
			e1.printStackTrace();
		}

		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	double trimDouble(double d) {
		DecimalFormat df = new DecimalFormat("#.##");
		return Double.valueOf(df.format(d));
	}
}
