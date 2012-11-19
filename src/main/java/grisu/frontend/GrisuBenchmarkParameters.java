package grisu.frontend;

import java.util.List;

import grisu.frontend.view.cli.GrisuCliParameters;

import com.beust.jcommander.Parameter;

public class GrisuBenchmarkParameters extends GrisuCliParameters {


	@Parameter(names = { "--mpi"}, description = "sets the job type to be mpi")
	private boolean mpi;

	public Boolean getMpi(){
		return mpi;
	}

	@Parameter(names = { "--single"}, description = "sets the job type to be single")
	private boolean single;

	public Boolean getSingle(){
		return single;
	}

	@Parameter(names = {"-n", "--jobname"}, description = "the name for the job")
	private String jobName;

	public String getJobName(){
		return jobName;
	}

	@Parameter(names = {"-w", "--walltime"}, description = "the wall time for every job")
	private int wallTime;

	public int getWallTime(){
		return wallTime;
	}



	@Parameter(names = {"--script" }, description = "script containing the test job")
	private String script;

	public String getScript() {
		return script;
	}

	//Benchmark changes
	@Parameter(names = {"--cpu"}, description = "the number of cpus to be used. specified as a range (eg: 1..100) or a list (eg: 1,25,50,100)")
	private String cpu;

	public String getCpu(){
		return cpu;
	}

	//Benchmark changes
	@Parameter(names = {"-g", "--group"}, description = "the group for submitting the job")
	private String group;

	public String getGroup(){
		return group;
	}

	//Benchmark changes
	@Parameter(names = {"-q", "--queue"}, description = "the queue for submitting the job")
	private String queue;

	public String getQueue(){
		return queue;
	}


	//Benchmark changes
	@Parameter(names = {"-f", "--files"}, description = "the input files for the jobs")
	private String files;

	public String getFiles(){
		return files;
	}		
	
	//Benchmark changes
	@Parameter(names = {"--env"}, description = "the environment variables for the job")
	private List<String> envVars;

	public List<String> getEnvVars(){
		return envVars;
	}			


}
