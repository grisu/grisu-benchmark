package grisu.frontend;

import grisu.control.ServiceInterface;
import grisu.control.exceptions.JobSubmissionException;
import grisu.frontend.control.login.LoginManager;
import grisu.frontend.gee.GJob;
import grisu.frontend.gee.Gee;
import grisu.frontend.model.job.GrisuJob;
import grisu.frontend.view.cli.GrisuCliClient;
import grisu.jcommons.constants.Constants;
import grisu.jcommons.utils.PackageFileHelper;
import grisu.jcommons.view.html.VelocityUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.beust.jcommander.internal.Maps;
import com.google.common.collect.ImmutableMap;

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

	private GJob gjob;
	private ServiceInterface si;
	
	public Client(GrisuBenchmarkParameters params, String[] args)
			throws Exception {
		super(params, args);
	}

	@Override
	public void run() {

		String job_folder = getCliParameters().getJobFolder();

		String cpu = getCliParameters().getCpu();
		String group = getCliParameters().getGroup();
		String queue = getCliParameters().getQueue();

		String args = getCliParameters().getArgs();
		String desc = getCliParameters().getDescription();
		if (StringUtils.isBlank(desc) ) {
			desc = NO_DESCRIPTION;
		}


		String jobName = getCliParameters().getJobName();

		if (job_folder == null) {
			System.err.println("Please specify the script name");
			System.exit(1);
		}


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

//		if (mpi && single) {
//			System.err.println("Cannot set job type to both mpi and single");
//			System.exit(1);
//		}


		if (queue == null) {
			System.err.println("No queue specified.");
			System.exit(1);
		}

		if (args == null) {
			args = "";
		}
		
		Map<String, String> submitProperties = Maps.newHashMap();
		submitProperties.put(Gee.GROUP_KEY, group);
		submitProperties.put(Gee.QUEUE_KEY, queue);
		submitProperties.put(Gee.JOBNAME_KEY, jobName);
		
		submitProperties.put(Constants.JOB_DESCRIPTION_KEY, desc);
		
		
		try {
			si = getServiceInterface();
		} catch (Exception e) {
			System.err.println("Could not login: "
					+ e.getLocalizedMessage());
			System.exit(1);
		}
		
		gjob = new GJob(job_folder);

		String[] cpuSplit = cpu.split(",");
		String[] temp = new String[3];

		int startIndex = -1;
		int endIndex = -1;
		for (int i = 0; i < cpuSplit.length; i++) {

			System.out.println("Creating job...");
			Map<String, String> submitPropertiesThisOne = new HashMap(submitProperties);
			
			String whole_string = cpuSplit[i];
			String walltime = null;
			String hostCount = null;
			// test for walltime
			if ( whole_string.contains("[")) {
				startIndex = whole_string.indexOf("[");
				endIndex = whole_string.indexOf("]");
				walltime = whole_string.substring(startIndex+1, endIndex);
				whole_string = whole_string.substring(0, startIndex);
			}
			
			// check whether this cpu has got a special hostcount
			if (whole_string.contains("=")) {
				startIndex = whole_string.indexOf("=");
				hostCount = whole_string.substring(startIndex+1);
				whole_string = whole_string.substring(0, startIndex);
			}
			
			String cpus = whole_string;

			if ( StringUtils.isNotBlank(walltime) ) {
				submitPropertiesThisOne.put(Constants.WALLTIME_IN_MINUTES_KEY, walltime);
			}
			
			if (StringUtils.isNotBlank(hostCount)) {
				submitPropertiesThisOne.put(Constants.HOSTCOUNT_KEY, hostCount);
			}
			
			submitPropertiesThisOne.put(Constants.NO_CPUS_KEY, cpus);
			
			submitPropertiesThisOne.put(Gee.JOBNAME_KEY, jobName+"_"+toFourDigits(Integer.parseInt(cpus))+"_cpus");

			System.out.println("Set jobname to be: " + submitPropertiesThisOne.get(Constants.JOBNAME_KEY));

			
			createAndSubmitJob(submitPropertiesThisOne);
		}
	}
	
	private void createAndSubmitJob(Map<String, String> properties) {

		String command = properties.get(Constants.COMMANDLINE_KEY);
		
		if ( StringUtils.isBlank(command) ) {
			
			command = gjob.getProperties().get(Constants.COMMANDLINE_KEY);
			
			if ( StringUtils.isBlank(command)) {
				throw new RuntimeException("No command specified");
			}
		}
		

		// create wrapper script
		Map<String, Object> script_props = Maps.newHashMap();
		script_props.put("command", command);
		
		String wrapper_content = VelocityUtils.render("wrapper_script", script_props);
		File wrapper_script_file = PackageFileHelper.createTempFile(wrapper_content, "benchmark_wrapper_script.sh");
		
		properties.put(Constants.COMMANDLINE_KEY, "bash "+wrapper_script_file.getName());
		
		GrisuJob job = null;
		try {
			System.out.println("Creating job on backend...");
			job = gjob.createJob(si, properties, wrapper_script_file);
		} catch (Exception e) {
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

	private static String toFourDigits(Integer cpus) {
		String cpuStr = cpus.toString();
		while (cpuStr.length() < 4) {
			cpuStr = "0" + cpuStr;
		}
		return cpuStr;
	}

}
