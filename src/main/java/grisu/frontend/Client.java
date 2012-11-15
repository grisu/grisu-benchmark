package grisu.frontend;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import grisu.control.ServiceInterface;
import grisu.control.exceptions.JobPropertiesException;
import grisu.control.exceptions.JobSubmissionException;
import grisu.frontend.control.login.LoginManager;
import grisu.frontend.model.job.JobObject;
import grisu.frontend.view.cli.GrisuCliClient;
import grisu.jcommons.constants.Constants;
import grisu.model.FileManager;

public class Client extends GrisuCliClient<ExampleCliParameters> {

	public static void main(String[] args) {

		// basic housekeeping
		LoginManager.initGrisuClient("grisu-benchmark");

		// helps to parse commandline arguments, if you don't want to create
		// your own parameter class, just use DefaultCliParameters
		ExampleCliParameters params = new ExampleCliParameters();
		// create the client
		Client client = null;
		try {
			client = new Client(params, args);
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

	public Client(ExampleCliParameters params, String[] args) throws Exception {
		super(params, args);
	}

	@Override
	public void run() {

		String script = getCliParameters().getScript();
		String scriptName=FileManager.getFilename(script);
		String cpu = getCliParameters().getCpu();
		String group = getCliParameters().getGroup();
		String queue = getCliParameters().getQueue();
		String files = getCliParameters().getFiles();

		//merging older changes
		int wallTime = getCliParameters().getWallTime();
		String jobName = getCliParameters().getJobName();
		Boolean mpi = getCliParameters().getMpi();
		Boolean single = getCliParameters().getSingle();

		if(script==null)
		{
			System.err.println("Please specify the script name");
			System.exit(1);
		}
		
		if(group==null)
		{
			System.err.println("Please specify a group name");
			System.exit(1);
		}

		//		if(files==null)
		//			files=file;

		if(cpu==null)
			cpu="1";

		if(jobName==null)
		{
			System.err.println("Please specify a job name");
			System.exit(1);
		}

		if(wallTime==0)
			{
			System.err.println("Please specify wall time");
			System.exit(1);
			}

		if(mpi && single)
		{
			System.err.println("Cannot set job type to both mpi and single");
			System.exit(1);
		}


		String[] cpuSplit=cpu.split(",");
		String[] temp;
		String[] filename;// = FileManager.getFilename(file);
		for(int i=0;i<cpuSplit.length;i++)
		{

			//			for(int j=0; j<filename.length; j++)
			//			{
			//				// all login stuff is implemented in the parent class
			System.out.println("Getting serviceinterface...");
			ServiceInterface si = null;
			try {
				si = getServiceInterface();
			} catch (Exception e) {
				System.err.println("Could not login: " + e.getLocalizedMessage());
				System.exit(1);
			}

			System.out.println("Creating job...");
			JobObject job = new JobObject(si);

			System.out.println("File to use for the job: " + script);

			job.setApplication(Constants.GENERIC_APPLICATION_NAME);
			job.setCommandline("sh " + scriptName);
			job.addInputFileUrl(script);
			if(files!=null){
				filename=files.split(",");
				for(int j=0; j<filename.length; j++)
				{
					job.addInputFileUrl(filename[j]);
				}
			}
			job.setWalltimeInSeconds(wallTime);
			job.setForce_mpi(mpi);
			job.setForce_single(single);
			job.setTimestampJobname(jobName);

			System.out.println("jobtype: mpi-"+job.isForce_mpi()+" single-"+job.isForce_single());

			if(StringUtils.isBlank(queue)) 
			{
				System.err.println("No queue specified.");
				System.exit(1);
			}
				job.setSubmissionLocation(queue);
			

			if(cpuSplit[i].contains("=")){
				temp=cpuSplit[i].split("=");
				job.setCpus(Integer.parseInt(temp[0]));
				job.setHostCount(Integer.parseInt(temp[1]));
			}
			else
			{
				job.setCpus(Integer.parseInt(cpuSplit[i]));
			}

			System.out.println("Set jobname to be: " + job.getJobname());

			try {
				System.out.println("Creating job on backend...");
				job.createJob(group);
			} catch (JobPropertiesException e) {
				System.err.println("Could not create job: "
						+ e.getLocalizedMessage());
				System.exit(1);
			}

			try {
				System.out.println("Submitting job to the grid...");
				job.submitJob();
			} catch (JobSubmissionException e) {
				System.err.println("Could not submit job: "
						+ e.getLocalizedMessage());
				System.exit(1);
			} catch (InterruptedException e) {
				System.err.println("Jobsubmission interrupted: "
						+ e.getLocalizedMessage());
				System.exit(1);
			}

			//	Map jmap=job.getAllJobProperties();
			System.out.println("Job submitted to queue: "+job.getJobProperty("queue"));
			System.out.println("Job submission finished.");
			System.out.println("Job submitted to: "
					+ job.getJobProperty(Constants.SUBMISSION_SITE_KEY));

			System.out.println("cpu-"+job.getCpus());
			System.out.println("host-"+job.getHostCount());

			//				System.out.println("Waiting for job to finish...");
			//
			//				// for a realy workflow, don't check every 5 seconds since that would
			//				// put too much load on the backend/gateways
			//				job.waitForJobToFinish(5);
			//
			//				System.out.println("Job finished with status: "
			//						+ job.getStatusString(false));
			//
			//				System.out.println("Stdout: " + job.getStdOutContent());
			//				System.out.println("Stderr: " + job.getStdErrContent());


			//}
		}

		/***
		System.out.println("File to use for the job: " + file);

		// all login stuff is implemented in the parent class
		System.out.println("Getting serviceinterface...");
		ServiceInterface si = null;
		try {
			si = getServiceInterface();
		} catch (Exception e) {
			System.err.println("Could not login: " + e.getLocalizedMessage());
			System.exit(1);
		}

		System.out.println("Creating job...");
		JobObject job = new JobObject(si);
		String filename = FileManager.getFilename(file);
		job.setApplication(Constants.GENERIC_APPLICATION_NAME);
		job.setCommandline("cat " + filename);
		job.addInputFileUrl(file);
		job.setWalltimeInSeconds(60);

		job.setTimestampJobname("cat_job");

		System.out.println("Set jobname to be: " + job.getJobname());

		try {
			System.out.println("Creating job on backend...");
			job.createJob("/nz/nesi");
		} catch (JobPropertiesException e) {
			System.err.println("Could not create job: "
					+ e.getLocalizedMessage());
			System.exit(1);
		}

		try {
			System.out.println("Submitting job to the grid...");
			job.submitJob();
		} catch (JobSubmissionException e) {
			System.err.println("Could not submit job: "
					+ e.getLocalizedMessage());
			System.exit(1);
		} catch (InterruptedException e) {
			System.err.println("Jobsubmission interrupted: "
					+ e.getLocalizedMessage());
			System.exit(1);
		}

		System.out.println("Job submission finished.");
		System.out.println("Job submitted to: "
				+ job.getJobProperty(Constants.SUBMISSION_SITE_KEY));

		System.out.println("Waiting for job to finish...");

		// for a realy workflow, don't check every 5 seconds since that would
		// put too much load on the backend/gateways
		job.waitForJobToFinish(5);

		System.out.println("Job finished with status: "
				+ job.getStatusString(false));

		System.out.println("Stdout: " + job.getStdOutContent());
		System.out.println("Stderr: " + job.getStdErrContent());
		 ***/
	}

}
