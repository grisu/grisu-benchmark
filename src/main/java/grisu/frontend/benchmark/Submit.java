package grisu.frontend.benchmark;

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

public class Submit extends GrisuCliClient<GrisuBenchmarkSubmitParameters> {
	
	public static final String NO_DESCRIPTION = "no description";
	public static final String BENCHMARK_CONFIG_FILE_NAME = "benchmark.config";
	public static final String BENCHMARKS_FOLDER_NAME = "benchmarks";
	
	public static final String CPUS_KEY = "cpus";
	public static final String ARGS_KEY = "args";

	public static void main(String[] args) {

		// basic housekeeping
		LoginManager.initGrisuClient("grisu-benchmark");

		// helps to parse commandline arguments, if you don't want to create
		// your own parameter class, just use DefaultCliParameters
		GrisuBenchmarkSubmitParameters params = new GrisuBenchmarkSubmitParameters();
		// create the client
		Submit submit = null;
		try {
			submit = new Submit(params, args);
		} catch (Exception e) {
			System.err.println("Could not start grisu-benchmark: "
					+ e.getLocalizedMessage());
			System.exit(1);
		}

		// finally:
		// execute the "run" method below
		submit.run();

		// exit properly
		System.exit(0);

	}

	private GJob gjob;
	private ServiceInterface si;
	
	public Submit(GrisuBenchmarkSubmitParameters params, String[] args)
			throws Exception {
		super(params, args);
	}

	@Override
	public void run() {
		
		String benchmark_config = getCliParameters().getBenchmarkConfig();
		
		File benchmark_config_folder = null;
		if ( StringUtils.isNotBlank(benchmark_config) ) {
			benchmark_config_folder = new File(benchmark_config);
			if ( benchmark_config_folder.isFile() ) {
				benchmark_config_folder = benchmark_config_folder.getParentFile();
			}
		}
			
		String cpus = null;
		String group = null;
		String queue = null;
		String args = null;
		String desc = null;
		String jobName = null;
		String job_folder_path = null;
		
		File job_config = null;

		if ( StringUtils.isNotBlank(benchmark_config)) {
			
			Map<String, String> config = Gee.parsePropertiesFile(benchmark_config, BENCHMARK_CONFIG_FILE_NAME);
			cpus = config.get(CPUS_KEY);
			group = config.get(Gee.GROUP_KEY);
			queue = config.get(Gee.QUEUE_KEY);
			args = config.get(ARGS_KEY);
			desc = config.get(Constants.JOB_DESCRIPTION_KEY);
			jobName = config.get(Constants.JOBNAME_KEY);
			job_folder_path = config.get(GJob.JOB_KEY);
			
			if ( StringUtils.isBlank(job_folder_path) ) {
				System.err.println("No job specified.");
				System.exit(1);
			}
			
			job_config = new File(job_folder_path);
			if ( ! job_config.exists() ) {
				job_config = new File(benchmark_config_folder, job_folder_path);
			}
			
		} else {
			job_folder_path = getCliParameters().getJobFolder();

			if ( StringUtils.isBlank(job_folder_path) ) {
				System.err.println("Please specify the job to use.");
				System.exit(1);
			}
			
			job_config = new File(job_folder_path);
			cpus = getCliParameters().getCpu();
			group = getCliParameters().getGroup();
			queue = getCliParameters().getQueue();

			args = getCliParameters().getArgs();
			desc = getCliParameters().getDescription();
			if (StringUtils.isBlank(desc) ) {
				desc = NO_DESCRIPTION;
			}

			jobName = getCliParameters().getJobName();

		}

		if (!job_config.exists()) {
			System.err.println("Job '"+job_config.getAbsolutePath()+"' does not exist.");
			System.exit(1);
		}


		if (group == null) {
			System.err.println("Please specify a group name");
			System.exit(1);
		}

		if (cpus == null) {
			System.err.println("No cpus specified");
			System.exit(1);
		}


		if (jobName == null) {
			System.err.println("Please specify a job name");
			System.exit(1);
		}

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
		
		gjob = new GJob(job_config);

		String[] cpuSplit = cpus.split(",");
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
			
			String cpus_rest = whole_string;

			if ( StringUtils.isNotBlank(walltime) ) {
				submitPropertiesThisOne.put(Constants.WALLTIME_IN_MINUTES_KEY, walltime);
			}
			
			if (StringUtils.isNotBlank(hostCount)) {
				submitPropertiesThisOne.put(Constants.HOSTCOUNT_KEY, hostCount);
			}
			
			submitPropertiesThisOne.put(Constants.NO_CPUS_KEY, cpus_rest);
			
			submitPropertiesThisOne.put(Gee.JOBNAME_KEY, jobName+"_"+toFourDigits(Integer.parseInt(cpus_rest))+"_cpus");

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
		
		GrisuJob job = null;
		try {
			System.out.println("Creating job on backend...");
			String group = properties.get(Gee.GROUP_KEY);
			if ( StringUtils.isBlank(group) ) {
				group = properties.get(Constants.FQAN_KEY);
			}

			job = gjob.createJob(si, properties, false);
			
			job.addEnvironmentVariable("PROLOG", "echo \"Started: `date +%s`\" >> benchmark.log");
			job.addEnvironmentVariable("EPILOG", "echo \"Finished: `date +%s`\" >> benchmark.log");
			
			job.createJob(group);
			
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
