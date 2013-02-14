package grisu.frontend.benchmark;

import grisu.control.JobConstants;
import grisu.control.ServiceInterface;
import grisu.control.exceptions.NoSuchJobException;
import grisu.frontend.control.login.LoginManager;
import grisu.frontend.model.events.JobCleanedEvent;
import grisu.frontend.model.job.GrisuJob;
import grisu.frontend.view.cli.GrisuCliClient;
import grisu.jcommons.utils.OutputHelpers;
import grisu.model.GrisuRegistryManager;
import grisu.model.UserEnvironmentManager;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class Results extends GrisuCliClient<ClientResultsParams> {

	private static final int waittime = 10;

	public static void main(String[] args) {

		// basic housekeeping
		LoginManager.initGrisuClient("grisu-benchmark");

		// helps to parse commandline arguments, if you don't want to create
		// your own parameter class, just use DefaultCliParameters
		ClientResultsParams params = new ClientResultsParams();
		// create the client
		Results client = null;
		try {
			client = new Results(params, args);
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

	public Results(ClientResultsParams params, String[] args)
			throws Exception {
		super(params, args);
	}

	@Override
	public void run() {

		try {
			getCredential();
			getServiceInterface();
		} catch (Exception e) {
			System.err.println("Error: " + e.getLocalizedMessage());
			e.printStackTrace();
			System.exit(2);
		}
		
		List<String> jobnames = getCliParameters().getJobNames();

		boolean nowait = getCliParameters().getNowait();

		boolean list = getCliParameters().getList();
		
		if (jobnames == null && !list) {
			list = true;
		}

		
		System.out.println("Getting serviceinterface...");
		ServiceInterface si = null;
		try {
			si = getServiceInterface();
		} catch (Exception e) {
			System.err.println("Could not login: " + e.getLocalizedMessage());
			System.exit(1);
		}

		UserEnvironmentManager uem = GrisuRegistryManager.getDefault(si)
				.getUserEnvironmentManager();
		SortedSet<String> currentJobList = uem.getCurrentJobnames(true);

		// if --list option is specified
		if (list) {

			System.out.println("Retrieving benchmark jobs. This might take a while...");

			List<List<String>> table = Lists.newArrayList();
			List<String> titleRow = Lists.newArrayList();
			titleRow.add("Benchmark");
			titleRow.add("Pending");
			titleRow.add("Active");
			titleRow.add("Failed");
			titleRow.add("Finished");
			titleRow.add("JobCount (total)");
			titleRow.add("");

			table.add(titleRow);
			
			Map<String, Set<GrisuJob>> benchmarkMap = Maps.newTreeMap();
			
			for (String jobName : currentJobList) {
				if (jobName.contains("_cpus_")) {
					int index = jobName.indexOf("_cpus_");
					
					String benchmarkName = jobName.substring(0, index - 5);

					Set<GrisuJob> jobs = benchmarkMap.get(benchmarkName);
					if ( jobs == null ) {
						jobs = Sets.newTreeSet();
						benchmarkMap.put(benchmarkName, jobs);
					}

					GrisuJob job;
					try {
						job = new GrisuJob(si, jobName);
						
						jobs.add(job);

					} catch (NoSuchJobException e) {
						e.printStackTrace();
					}
				}
			}
			
			for ( String name : benchmarkMap.keySet() ) {
				
				int total = 0;
				int pending = 0;
				int finished = 0;
				int failed = 0;
				int active = 0;
				
				for ( GrisuJob job : benchmarkMap.get(name) ) {
					
					if (job.isFinished()) {
						finished++;
						if ( job.isFailed(false)) {
							failed++;
						}
					} else {
						if ( job.getStatus(false) < JobConstants.ACTIVE ) {
							pending++;
						} else {
							active++;
						}
					}
					total++;
					
				}
				List<String> row = Lists.newArrayList();
				row.add(name);
				row.add(Integer.toString(pending));
				row.add(Integer.toString(active));
				row.add(Integer.toString(failed));
				row.add(Integer.toString(finished));
				row.add(Integer.toString(total));
				if ( pending == 0 && active == 0 ) {
					row.add("Finished");
				} else {
					row.add("");
				}
				table.add(row);
			}
			
			
			String t = OutputHelpers.getTable(table);
			System.out.println("\n"+t);

			System.exit(0);
		}


		// TODO check for type of graph and initialize velocityhtmlrenderer
//		if(graph.equalsIgnoreCase("column"))
//			html.populateGraph(concatJobnames, "ColumnChart");
//		else
//			html.populateGraph(concatJobnames, "LineChart");

		
		List<BenchmarkJob> bjobs = Lists.newArrayList();
		for ( String jobname : jobnames ) {

			BenchmarkJob bj = new BenchmarkJob(si, jobname, nowait);
			bjobs.add(bj);
		}
		

		BenchmarkRenderer html = new VelocityHtmlRenderer();
		BenchmarkRenderer csv = new CsvBenchmarkRenderer();
		
		html.renderer(bjobs);
		csv.renderer(bjobs);

		
	}
}
