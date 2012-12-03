package grisu.frontend;

import grisu.control.ServiceInterface;
import grisu.control.exceptions.NoSuchJobException;
import grisu.frontend.control.login.LoginManager;
import grisu.frontend.model.job.JobObject;
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

public class ClientResults extends GrisuCliClient<ClientResultsParams> {

	private static final int waittime = 10;

	public static void main(String[] args) {

		// basic housekeeping
		LoginManager.initGrisuClient("grisu-benchmark");

		// helps to parse commandline arguments, if you don't want to create
		// your own parameter class, just use DefaultCliParameters
		ClientResultsParams params = new ClientResultsParams();
		// create the client
		ClientResults client = null;
		try {
			client = new ClientResults(params, args);
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

	public ClientResults(ClientResultsParams params, String[] args)
			throws Exception {
		super(params, args);
	}

	@Override
	public void run() {

		List<String> jobnames = getCliParameters().getJobNames();

		boolean nowait = getCliParameters().getNowait();

		boolean list = getCliParameters().getList();
		
		String graph = getCliParameters().getGraph();

		if (jobnames == null && !list) {
			list = true;
		}
		
		if(graph==null || (!graph.equalsIgnoreCase("line") && !graph.equalsIgnoreCase("column")))
		{
			System.out.println("Setting default graph type to Line");
			graph="Line";
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


			List<List<String>> table = Lists.newArrayList();
			List<String> titleRow = Lists.newArrayList();
			titleRow.add("Benchmark");
			titleRow.add("JobCount (running)");
			titleRow.add("JobCount (finished)");
			titleRow.add("JobCount (total)");

			table.add(titleRow);
			
			Map<String, Set<JobObject>> benchmarkMap = Maps.newTreeMap();
			
			for (String jobName : currentJobList) {
				if (jobName.contains("_cpus_")) {
					int index = jobName.indexOf("_cpus_");
					
					String benchmarkName = jobName.substring(0, index - 5);

					Set<JobObject> jobs = benchmarkMap.get(benchmarkName);
					if ( jobs == null ) {
						jobs = Sets.newTreeSet();
						benchmarkMap.put(benchmarkName, jobs);
					}

					JobObject job;
					try {
						job = new JobObject(si, jobName);
						
						jobs.add(job);

					} catch (NoSuchJobException e) {
						e.printStackTrace();
					}
				}
			}
			
			for ( String name : benchmarkMap.keySet() ) {
				
				int jCount = 0;
				int jOn = 0;
				int jFinished = 0;
				
				for ( JobObject job : benchmarkMap.get(name) ) {
					
					if (job.isFinished())
						jFinished++;
					else
						jOn++;
					jCount++;
					
				}
				List<String> row = Lists.newArrayList();
				row.add(name);
				row.add(Integer.toString(jOn));
				row.add(Integer.toString(jFinished));
				row.add(Integer.toString(jCount));
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
