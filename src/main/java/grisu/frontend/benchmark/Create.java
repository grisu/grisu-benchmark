package grisu.frontend.benchmark;

import grisu.frontend.control.login.LoginManager;
import grisu.frontend.gee.GJob;
import grisu.frontend.gee.Gee;
import grisu.frontend.view.cli.GrisuCliClient;
import grisu.jcommons.constants.Constants;
import grisu.jcommons.view.html.VelocityUtils;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Maps;

public class Create extends GrisuCliClient<GrisuBenchmarkCreateParameters> {

	public static void main(String[] args) {
		// basic housekeeping
		LoginManager.initGrisuClient("grisu-benchmark");

		// helps to parse commandline arguments, if you don't want to create
		// your own parameter class, just use DefaultCliParameters
		GrisuBenchmarkCreateParameters params = new GrisuBenchmarkCreateParameters();
		// create the client
		Create client = null;
		try {
			client = new Create(params, args);
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

	public Create(GrisuBenchmarkCreateParameters params, String[] args)
			throws Exception {
		super(params, args);
	}

	@Override
	public void run() {
		
		String application_folder_path = getCliParameters().getFolder();
		String app = getCliParameters().getApp();
		String benchmarkName = getCliParameters().getBenchmarkName();
				
		
		if ( StringUtils.isBlank(application_folder_path) ) {
			System.err.println("Application root folder not specified.");
			System.exit(1);
		}
		if ( StringUtils.isBlank(app) ) {
			System.err.println("Application not specified.");
			System.exit(1);
		}
		if ( StringUtils.isBlank(benchmarkName) ) {
			System.err.println("Benchmark name not specified.");
			System.exit(1);
		}
		
		String cpus = null;
		String group = null;
		String queue = null;
		String args = null;
		String desc = null;
		String jobName = null;
		String job_folder = null;

		job_folder = getCliParameters().getJobFolder();

		cpus = getCliParameters().getCpu();
		group = getCliParameters().getGroup();
		queue = getCliParameters().getQueue();

		args = getCliParameters().getArgs();
		desc = getCliParameters().getDescription();
		if (StringUtils.isBlank(desc)) {
			desc = Submit.NO_DESCRIPTION;
		}

		jobName = getCliParameters().getJobName();

		if (StringUtils.isBlank(group)) {
			System.err.println("Please specify a group name");
			System.exit(1);
		}

		if (StringUtils.isBlank(cpus)) {
			cpus = "1=1[1h],6=1[1h],18=3[30m],48[20m]";
		}

		if (StringUtils.isBlank(jobName)) {
			jobName = "benchmark_"+benchmarkName;
		}

		if (StringUtils.isBlank(queue)) {
			System.err.println("Please specify at least one queue.");
			System.exit(1);
		}

		if (StringUtils.isBlank(args)) {
			args = "";
		}

		File application_folder = new File(application_folder_path);
		
		File benchmark_folder = new File(application_folder_path+File.separator+app+File.separator+Submit.BENCHMARKS_FOLDER_NAME+File.separator+benchmarkName);
		
		if ( benchmark_folder.exists() ) {
			System.err.println("Benchmark '"+benchmark_folder+"' already exists. Not doing anything.");
			System.exit(1);
		}
		
		benchmark_folder.mkdirs();
		if ( ! benchmark_folder.exists() ) {
			System.err.println("Could not create benchmark folder: "+benchmark_folder.getAbsolutePath());
			System.exit(1);
		}
		
		File benchmark_config_file = new File(benchmark_folder, Submit.BENCHMARK_CONFIG_FILE_NAME);

		if (StringUtils.isBlank(job_folder)) {
			
			File benchmark_job_folder = new File(application_folder.getAbsolutePath()+File.separator+app+File.separator+Gee.JOBS_DIR_NAME+File.separator+benchmarkName);
			if ( ! benchmark_job_folder.exists() ) {
				System.out.println("Creating job: "+benchmark_job_folder.getAbsolutePath());
				GJob.createJobStub(benchmark_job_folder, benchmarkName);
			} else {
				System.out.println("Job with name '"+benchmarkName+"' already exists for package '"+app+"', not creating new one.");
			}
			job_folder = "../../"+Gee.JOBS_DIR_NAME+"/"+benchmark_job_folder.getName()+"/"+GJob.JOB_PROPERTIES_FILE_NAME;
			
		}

		Map<String, Object> submitProperties = Maps.newHashMap();
		submitProperties.put(Gee.GROUP_KEY, group);
		submitProperties.put(Gee.QUEUE_KEY, queue);
		submitProperties.put(Gee.JOBNAME_KEY, jobName);
		submitProperties.put(Constants.JOB_DESCRIPTION_KEY, desc);
		submitProperties.put(Submit.ARGS_KEY, args);
		submitProperties.put(Submit.CPUS_KEY, cpus);
		submitProperties.put(GJob.JOB_KEY, job_folder);
		
		String content = VelocityUtils.render("benchmark.config", submitProperties);
		
		try {
			FileUtils.writeStringToFile(benchmark_config_file, content);
			System.out.println("Wrote benchmark config: "+benchmark_config_file.getAbsolutePath());
		} catch (Exception e) {
			System.err.println("Can't write to file '"+benchmark_config_file.getAbsolutePath()+"': "+e.getLocalizedMessage());
		}
		
	}

}
