package grisu.frontend.benchmark;

import com.beust.jcommander.Parameter;

public class GrisuBenchmarkCreateParameters extends GrisuBenchmarkParameters {

	@Parameter(names = { "-f", "--applications-folder" }, description = "root folder containing the applications (default: current folder)")
	private String folder = System.getProperty("user.dir");
	
	@Parameter(names = {"--package", "-p" }, description = "the package name")
	private String app;

	@Parameter(names = {"--benchmark-name", "-n" }, description = "the name of the benchmark")
	private String testname;

	public String getApp() {
		return app;
	}

	public String getFolder() {
		return folder;
	}
	
	public String getBenchmarkName() {
		return testname;
	}
	

}
