package grisu.frontend;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import grisu.control.ServiceInterface;
import grisu.control.exceptions.JobPropertiesException;
import grisu.control.exceptions.JobSubmissionException;
import grisu.control.exceptions.NoSuchJobException;
import grisu.frontend.control.login.LoginManager;
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
								System.out.println("Jobname:"+job.getJobname());
								System.out.println("Number of CPUs:"+job.getCpus());
								System.out.println("Host count:"+job.getHostCount());
								System.out.println("Stdout:"+job.getStdOutContent());
								System.out.println("Stderr:"+job.getStdErrContent());
								System.out.println("job succeeded:"+job.isSuccessful(true));
								try{
									String jobLog=job.getFileContent("benchmark.log");
									System.out.println(jobLog);
									String temp=jobLog.substring(jobLog.lastIndexOf("Started:"));//.substring(beginIndex);
									System.out.println("start time:"+temp.substring(0,temp.indexOf("\n")));
									//DateTime d1=DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss").parseDateTime(temp.substring(0,temp.indexOf("\n")));
									Long long1=Long.parseLong(temp.substring(9,temp.indexOf("\n")));
									temp=jobLog.substring(jobLog.lastIndexOf("Finished:"));//.substring(beginIndex);
									System.out.println("end time:"+temp.substring(0,temp.indexOf("\n")));
									Long long2=Long.parseLong(temp.substring(10,temp.indexOf("\n")));
									//DateTime d2=DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss").parseDateTime(temp.substring(0,temp.indexOf("\n")));
									//System.out.println("diff:"+d2-d1);
								//Period p=new Period(d1, d2, PeriodType.millis());
									
									System.out.println("Time taken for execution:"+(long2-long1) +"ms");
									
								}
								catch(Exception e)
								{
									//e.printStackTrace();
									System.out.println("job failed");
								}
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
	}
}
