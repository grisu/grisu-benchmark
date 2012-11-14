package grisu.frontend;

import grisu.frontend.view.cli.GrisuCliParameters;

import com.beust.jcommander.Parameter;

public class ExampleCliParameters extends GrisuCliParameters {


	@Parameter(names = {"-mpi", "--mpi"}, description = "sets the job type to be mpi")
	private boolean mpi;
	
	public Boolean getMpi(){
		return mpi;
	}

	@Parameter(names = {"-single", "--single"}, description = "sets the job type to be single")
	private boolean single;
	
	public Boolean getSingle(){
		return single;
	}
	
	@Parameter(names = {"-n", "--n"}, description = "the wall time for every job")
	private String jobName;
	
	public String getJobName(){
		return jobName;
	}
	
	@Parameter(names = {"-w", "--w"}, description = "the wall time for every job")
	private int wallTime;
	
	public int getWallTime(){
		return wallTime;
	}
	
	@Parameter(names = { "-c", "--command" }, description = "the command to execute for every parameter")
	private String command;
	
	public String getCommand() {
		return command;
	}
	
	@Parameter(names = { "-f", "--file" }, description = "the path to a file")
	private String file;

	public String getFile() {
		return file;
	}

	//Benchmark changes
	@Parameter(names = {"-cpu", "--cpu"}, description = "the number of cpus to be used. specified as a range (eg: 1..100) or a list (eg: 1,25,50,100)")
	private String cpu;
	
	public String getCpu(){
		return cpu;
	}

	//Benchmark changes
	@Parameter(names = {"-group", "--group"}, description = "the group for submitting the job")
	private String group;
	
	public String getGroup(){
		return group;
	}

	//Benchmark changes
		@Parameter(names = {"-queue", "--queue"}, description = "the queue for submitting the job")
		private String queue;
		
		public String getQueue(){
			return queue;
		}
		

		//Benchmark changes
			@Parameter(names = {"-files", "--files"}, description = "the input files for the jobs")
			private String files;
			
			public String getFiles(){
				return files;
			}		
}
