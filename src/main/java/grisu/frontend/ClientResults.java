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

		boolean list = getCliParameters().getList();

		if(jobname==null && !list)
			System.out.println("Please enter a job name or use the --list option");
		System.out.println("Getting serviceinterface...");
		ServiceInterface si = null;
		try {
			si = getServiceInterface();
		} catch (Exception e) {
			System.err.println("Could not login: " + e.getLocalizedMessage());
			System.exit(1);
		}		

		UserEnvironmentManager uem = GrisuRegistryManager.getDefault(si).getUserEnvironmentManager();
		SortedSet<String> currentJobList = uem.getCurrentJobnames(true);	

	    //if --list option is specified
		if(list)
		{
			String jnConst="";
			int index;
			int jCount=0;
			int jOn=0;
			int jFinished=0;
			for(String jobName:currentJobList)
			{
				if(jobName.contains("_cpus_"))
				{
					index = jobName.indexOf("_cpus_");
					if(!jobName.substring(0, index-5).equals(jnConst))
					{
						jnConst=jobName.substring(0, index-5);
						if(jCount!=0)
						{
							System.out.print("\tJob Count: "+jCount+"\tFinished jobs count: "+jFinished+"\tIn progress jobs count: "+jOn);
							jCount=0;
							jFinished=0;
							jOn=0;
						}
						System.out.print("\n"+jnConst);
					}
					JobObject job;
					try 
					{
						job = new JobObject(si, jobName);
						if(job.isFinished())
							jFinished++;
						else
							jOn++;
						jCount++;
					} catch (NoSuchJobException e) {
						e.printStackTrace();
					}
				}
			}
			System.out.print("\tJob Count: "+jCount+"\tFinished jobs count: "+jFinished+"\tIn progress jobs count: "+jOn);
			System.exit(0);
		}


		//for --jobname option 
		CSVWriter errWriter = null;
		String[] csvTemp = new String[10];

		csvTemp[0]="Job name";
		csvTemp[1]="Host count";
		csvTemp[2]="Job success status";
		csvTemp[3]="CPUs";
		csvTemp[4]="Wall time";
		csvTemp[5]="Job execution time";
		csvTemp[6]="Execution time across all CPUs";
		csvTemp[7]="Efficiency";

		try {
			errWriter = new CSVWriter(new FileWriter(jobname+"_err.csv"));
			csvTemp[5]=csvTemp[6]=csvTemp[7]=null;
			errWriter.writeNext(csvTemp);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Boolean flag=true;
		List<JobObject> downloadStatus=new ArrayList<JobObject>();
		Long totalExecTime=0L;
		jobname=jobname+"_";
		List<String[]> allJobs = Lists.newArrayList();
		Integer minCpu=9999;
		Long minCpuWallTime=0L;
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
								String[] valuesForJob = new String[10];

								System.out.println("Jobname:"+job.getJobname());
								valuesForJob[0]=job.getJobname();
								System.out.println("Number of CPUs:"+job.getCpus());
								System.out.println("Host count:"+job.getHostCount());
								valuesForJob[1]=job.getHostCount().toString();
								System.out.println("job succeeded:"+job.isSuccessful(true));
								valuesForJob[2]=String.valueOf(job.isSuccessful(true));

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

									allJobs.add(valuesForJob);

									if ( cpus < minCpu ) 
									{
										minCpu = cpus;
										minCpuWallTime = totalExecTime;
									}
								}
								catch(Exception e)
								{
									System.out.println("job failed");
									valuesForJob[5]=valuesForJob[6]=null;
									errWriter.writeNext(valuesForJob);
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
			// wait a few seconds, so we don't overload the backend
			if ( flag ) {
				try {
					Thread.sleep(waittime*1000);
				} catch (InterruptedException e) {
				}
			}

		}
		
		Collections.sort(allJobs, new Comparator<String[]>(){
			public int compare(String[] j1, String[] j2)
			{
				return Integer.parseInt(j1[3])-Integer.parseInt(j2[3]);
			}
		});


		CsvBenchmarkRenderer csv=new CsvBenchmarkRenderer();
		csv.renderer(jobname.substring(0, jobname.length()-1), allJobs, minCpu, minCpuWallTime);

		HtmlBenchmarkRenderer html=new HtmlBenchmarkRenderer();
		html.renderer(jobname.substring(0, jobname.length()-1), allJobs, minCpu, minCpuWallTime);

		try {
			errWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
