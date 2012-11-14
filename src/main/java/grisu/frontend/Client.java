package grisu.frontend;

import java.util.Map;

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

		String file = getCliParameters().getFile();
		String cpu = getCliParameters().getCpu();
		String group = getCliParameters().getGroup();
		String queue = getCliParameters().getQueue();
		String files = getCliParameters().getFiles();

		if(group==null)
		{
			group = "/nz/nesi";
		}

		if(files==null)
			files=file;

		if(cpu==null)
			cpu="1";


		String[] cpuSplit=cpu.split(",");
		String[] temp;
		String[] filename;// = FileManager.getFilename(file);
		for(int i=0;i<cpuSplit.length;i++)
		{
			filename=files.split(",");
			for(int j=0; j<filename.length; j++)
			{
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

				System.out.println("File to use for the job: " + filename);
				
				job.setApplication(Constants.GENERIC_APPLICATION_NAME);
				job.setCommandline("cat " + filename[j]);
				job.addInputFileUrl(filename[j]);
				job.setWalltimeInSeconds(60);
				job.setTimestampJobname("cat_job");

				if(queue!=null)
				{
					job.addJobProperty("queue", queue);
				}

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


			}
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
