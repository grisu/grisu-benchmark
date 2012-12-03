package grisu.frontend;

import java.util.List;

import grisu.frontend.view.cli.GrisuCliParameters;

import com.beust.jcommander.Parameter;

public class ClientResultsParams extends GrisuCliParameters {

	@Parameter(names = {"-n", "--jobnames"}, description = "comma-separated list of benchmark job names to include in the statistics generation")
	private List<String> jobNames;

	public List<String> getJobNames(){
		return jobNames;
	}
	
	@Parameter(names = { "--no-wait"}, description = "don't wait for all jobs to finish")
	private boolean nowait = true;
	public Boolean getNowait() {
		return nowait;
	}
	
	@Parameter(names = { "--list"}, description = "list all the jobs for benchmarking")
	private boolean list;
	public Boolean getList() {
		return list;
	}

//	@Parameter(names = { "--graph"}, description = "specifies the type of graph (line or column) to be displayed for comparing the jobs in the jobnames list")
//	private String graph;
//	public String getGraph() {
//		return graph;
//	}
}
