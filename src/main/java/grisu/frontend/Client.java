package grisu.frontend;

import grisu.control.ServiceInterface;
import grisu.control.exceptions.JobPropertiesException;
import grisu.control.exceptions.JobSubmissionException;
import grisu.frontend.control.login.LoginManager;
import grisu.frontend.model.job.GrisuJob;
import grisu.frontend.view.cli.GrisuCliClient;
import grisu.jcommons.constants.Constants;
import grisu.jcommons.utils.WalltimeUtils;
import grisu.model.FileManager;

import java.util.List;

import org.apache.commons.lang.StringUtils;

public class Client extends GrisuCliClient<GrisuBenchmarkParameters> {
	
	public static final String NO_DESCRIPTION = "no description";

	public static void main(String[] args) {

		// basic housekeeping
		LoginManager.initGrisuClient("grisu-benchmark");

		// helps to parse commandline arguments, if you don't want to create
		// your own parameter class, just use DefaultCliParameters
		GrisuBenchmarkParameters params = new GrisuBenchmarkParameters();
		// create the client
		Client client = null;
		try {
			client = new Client(params, args);
		} catch (Exception e) {
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

	public Client(GrisuBenchmarkParameters params, String[] args)
			throws Exception {
		super(params, args);
	}

	@Override
	public void run() {

		String script = getCliParameters().getScript();

		String cpu = getCliParameters().getCpu();
		String group = getCliParameters().getGroup();
		String queue = getCliParameters().getQueue();
		String files = getCliParameters().getFiles();
		List<String> envVarList = getCliParameters().getEnvVars();
		String args = getCliParameters().getArgs();
		String desc = getCliParameters().getDescription();
		if (StringUtils.isBlank(desc) ) {
			desc = NO_DESCRIPTION;
		}

		// merging older changes
		String wallTime = getCliParameters().getWallTime();
		String jobName = getCliParameters().getJobName();
		Boolean mpi = getCliParameters().getMpi();
//		Boolean single = getCliParameters().getSingle();

		if (script == null) {
			System.err.println("Please specify the script name");
			System.exit(1);
		}
		String scriptName = FileManager.getFilename(script);

		if (group == null) {
			System.err.println("Please specify a group name");
			System.exit(1);
		}

		if (cpu == null)
			cpu = "1";

		if (jobName == null) {
			System.err.println("Please specify a job name");
			System.exit(1);
		}

		if (StringUtils.isBlank(wallTime)) {
			System.err.println("Please specify wall time");
			System.exit(1);
		}

//		if (mpi && single) {
//			System.err.println("Cannot set job type to both mpi and single");
//			System.exit(1);
//		}

		if (queue == null) {
			System.err.println("No queue specified.");
			System.exit(1);
		}

		if (args == null)
			args = "";

		String[] cpuSplit = cpu.split(",");
		String[] temp = new String[3];
		String[] filename;// = FileManager.getFilename(file);
		int index;
		for (int i = 0; i < cpuSplit.length; i++) {
			// all login stuff is implemented in the parent class
			System.out.println("Getting serviceinterface...");
			ServiceInterface si = null;
			try {
				si = getServiceInterface();
			} catch (Exception e) {
				System.err.println("Could not login: "
						+ e.getLocalizedMessage());
				System.exit(1);
			}

			System.out.println("Creating job...");
			GrisuJob job = new GrisuJob(si);

			System.out.println("File to use for the job: " + script);

			job.setApplication(Constants.GENERIC_APPLICATION_NAME);
			job.setCommandline("sh " + scriptName + " " + args);
			
			System.out.println("commandline:" + job.getCommandline());
			job.addInputFileUrl(script);
			if (files != null) {
				filename = files.split(",");
				for (int j = 0; j < filename.length; j++) {
					job.addInputFileUrl(filename[j]);
				}
		//		job.setCommandline("cat "+filename[0]);
			}
			
			try {
				job.setWalltimeInSeconds(WalltimeUtils
						.fromShortStringToSeconds(wallTime));
			} catch (Exception e1) {
				System.err.println("Can't parse walltime: " + wallTime);
				System.exit(1);
			}
			if (mpi != null && mpi)
				job.setForce_mpi(mpi);
			else 
				job.setForce_single(true);


			System.out.println("jobtype: mpi-" + job.isForce_mpi() + " single-"
					+ job.isForce_single());

			job.setSubmissionLocation(queue);
			
			job.setDescription(desc);

			temp = cpuSplit[i].split("=");
			String holder;
			if (temp[0].contains("[")) {
				index = temp[0].indexOf("[");
				holder = temp[0].substring(index + 1, temp[0].length() - 1);
				temp[0] = temp[0].substring(0, index);
				try {
					job.setWalltimeInSeconds(WalltimeUtils
							.fromShortStringToSeconds(holder));
				} catch (Exception e) {
					System.err.println("Can't parse walltime: " + holder);
					System.exit(1);
				}
			}
			job.setCpus(Integer.parseInt(temp[0]));
			if (temp.length > 1) {
				temp[0] = temp[1];
				index = temp[0].indexOf("[");
				if (index != -1) {
					temp[1] = temp[0]
							.substring(index + 1, temp[0].length() - 1);
					temp[0] = temp[0].substring(0, index);
					try {
						job.setWalltimeInSeconds(WalltimeUtils
								.fromShortStringToSeconds(temp[1]));
					} catch (Exception e) {
						System.out.println("Exception in WalltimeUtils.fromShortStringToSeconds: Cannot parse the string");
					}
				}
				job.setHostCount(Integer.parseInt(temp[0]));
			}

			if (envVarList != null) {
				temp = new String[3];
				for (int k = 0; k < envVarList.size(); k++) {
					temp[0] = envVarList.get(k);
					index = temp[0].indexOf("=");
					temp[1] = temp[0].substring(0, index);
					temp[2] = temp[0].substring(index + 1, temp[0].length());
					job.addEnvironmentVariable(temp[1], temp[2]);
				}
			}

			job.setTimestampJobname(jobName + "_" + toFourDigits(job.getCpus())
					+ "_cpus");
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

			System.out.println("Job submitted to queue: "
					+ job.getJobProperty("queue"));
			System.out.println("Job submission finished.");
			System.out.println("Job submitted to: "
					+ job.getJobProperty(Constants.SUBMISSION_SITE_KEY));

			System.out.println("cpu-" + job.getCpus());
			System.out.println("host-" + job.getHostCount());

		}
	}

	private static String toFourDigits(Integer cpus) {
		String cpuStr = cpus.toString();
		while (cpuStr.length() < 4) {
			cpuStr = "0" + cpuStr;
		}
		return cpuStr;
	}

}
