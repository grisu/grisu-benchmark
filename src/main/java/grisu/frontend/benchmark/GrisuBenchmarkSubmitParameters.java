package grisu.frontend.benchmark;

import com.beust.jcommander.Parameter;

public class GrisuBenchmarkSubmitParameters extends GrisuBenchmarkParameters {


	@Parameter(names = {"-c", "--benchmark-config"}, description = "Config file containing the benchmark configuration, if you choose this, you don't need to specify the parameters on the command line")
	private String benchmarkConfig;
	
	public String getBenchmarkConfig() {
		return benchmarkConfig;
	}
}
