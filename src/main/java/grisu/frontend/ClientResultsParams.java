package grisu.frontend;

import java.util.List;

import grisu.frontend.view.cli.GrisuCliParameters;

import com.beust.jcommander.Parameter;

public class ClientResultsParams extends GrisuCliParameters {

	@Parameter(names = {"-n", "--jobname"}, description = "the name for the job")
	private String jobName;

	public String getJobName(){
		return jobName;
	}
	
	@Parameter(names = { "--no-wait"}, description = "don't wait for all jobs to finish")
	private boolean nowait = true;
	public Boolean getNowait() {
		return nowait;
	}

}
