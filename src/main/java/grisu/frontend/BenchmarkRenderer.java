package grisu.frontend;

import grisu.frontend.model.job.JobObject;

import java.util.List;

public interface BenchmarkRenderer{
	public void renderer(String jobname, List<String[]> jobs, int minCpus, Long minRuntime);
}