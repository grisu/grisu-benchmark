package grisu.frontend;

import grisu.frontend.view.cli.GrisuCliParameters;

import java.util.List;

import com.beust.jcommander.Parameter;

public class GrisuBenchmarkParameters extends GrisuCliParameters {


//	@Parameter(names = { "--single"}, description = "sets the job type to be single")
//	private boolean single;
//
//	public Boolean getSingle(){
//		return single;
//	}

	@Parameter(names = {"-n", "--jobname"}, description = "the name for the job")
	private String jobName;

	public String getJobName(){
		return jobName;
	}


	@Parameter(names = {"-d", "--description"}, description = "a description for this benchmark setup")
	public String description;
	
	public String getDescription(){
		return description;
	}



	@Parameter(names = {"--job" }, description = "folder/file containing the test job")
	private String script;

	public String getJobFolder() {
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


	@Parameter(names = {"--arg"}, description = "the arguments for the script specified")
	private String args;

	public String getArgs(){
		return args;
	}	
}
