package grisu.frontend;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
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

		File file=new File(jobname+".csv");
		CSVWriter writer = null;
		String[] csvTemp = new String[10];

		csvTemp[0]="Job name";
		csvTemp[1]="CPUs";
		csvTemp[2]="Host count";
		csvTemp[3]="Stdout";
		csvTemp[4]="Stderr";
		csvTemp[5]="Job success status";
		csvTemp[6]="Job execution time";

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
		int run=0;
		HashMap<String, Boolean> downloadStatus=new HashMap<String, Boolean>();
		while(flag)
		{
			flag=false;
			run++;
			for(String jname:currentJobList)
			{
				if(jname.contains(jobname)){
					try 
					{
						JobObject job = new JobObject(si, jname);
						//	if(run==1)
						//		downloadStatus.put(jobname, false);
						if(!job.isFinished())
						{
							flag=true;
						}
						else
						{
							if(!downloadStatus.containsKey(jname))
							{
								//	String[] csvTemp = new String[10];
								System.out.println("Jobname:"+job.getJobname());
								csvTemp[0]=job.getJobname();
								System.out.println("Number of CPUs:"+job.getCpus());
								csvTemp[1]=job.getCpus().toString();
								System.out.println("Host count:"+job.getHostCount());
								csvTemp[2]=job.getHostCount().toString();
								try{
									System.out.println("Stdout:"+job.getStdOutContent());
									csvTemp[3]=job.getStdOutContent();
								}
								catch(JobException je){
									je.printStackTrace();
									csvTemp[3]="Could not read stdout file";
								}
								try
								{
									System.out.println("Stderr:"+job.getStdErrContent());
									csvTemp[4]=job.getStdErrContent();
								}
								catch(JobException je){
									je.printStackTrace();
									csvTemp[4]="Could not read stderr file";
								}
								System.out.println("job succeeded:"+job.isSuccessful(true));
								csvTemp[5]=String.valueOf(job.isSuccessful(true));

								//	    writer=new CSVWriter(new FileWriter(jobname+".csv"));

								//		writer.close();
								try{
									String jobLog=job.getFileContent("benchmark.log");
									//	System.out.println(jobLog);
									String temp=jobLog.substring(jobLog.lastIndexOf("Started:"));//.substring(beginIndex);
									//	System.out.println("start time:"+temp.substring(0,temp.indexOf("\n")));
									//DateTime d1=DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss").parseDateTime(temp.substring(0,temp.indexOf("\n")));
									Long long1=Long.parseLong(temp.substring(9,temp.indexOf("\n")));
									temp=jobLog.substring(jobLog.lastIndexOf("Finished:"));//.substring(beginIndex);
									//	System.out.println("end time:"+temp.substring(0,temp.indexOf("\n")));
									Long long2=Long.parseLong(temp.substring(10,temp.indexOf("\n")));
									//DateTime d2=DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss").parseDateTime(temp.substring(0,temp.indexOf("\n")));
									//System.out.println("diff:"+d2-d1);
									//Period p=new Period(d1, d2, PeriodType.millis());

									System.out.println("Time taken for execution:"+(long2-long1) +"ms");
									csvTemp[6]=(long2-long1)+" ms";

								}
								catch(Exception e)
								{
									//e.printStackTrace();
									System.out.println("job failed");
									csvTemp[6]=" ";
								}
								writer.writeNext(csvTemp);
								downloadStatus.put(jname, true);
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

		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
