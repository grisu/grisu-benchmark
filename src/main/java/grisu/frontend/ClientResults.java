package grisu.frontend;

import grisu.control.ServiceInterface;
import grisu.control.exceptions.NoSuchJobException;
import grisu.frontend.control.login.LoginManager;
import grisu.frontend.model.job.JobObject;
import grisu.frontend.view.cli.GrisuCliClient;
import grisu.model.FileManager;
import grisu.model.GrisuRegistryManager;
import grisu.model.UserEnvironmentManager;
import grisu.model.dto.DtoJob;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;

import org.python.antlr.ast.boolopType;

import au.com.bytecode.opencsv.CSVWriter;

import com.google.common.collect.Lists;

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

		String jobname = getCliParameters().getJobName();

		boolean nowait = getCliParameters().getNowait();

		boolean list = getCliParameters().getList();

		if (jobname == null && !list)
			System.out
					.println("Please enter a job name or use the --list option");
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
			String jnConst = "";
			int index;
			int jCount = 0;
			int jOn = 0;
			int jFinished = 0;
			for (String jobName : currentJobList) {
				if (jobName.contains("_cpus_")) {
					index = jobName.indexOf("_cpus_");
					if (!jobName.substring(0, index - 5).equals(jnConst)) {
						jnConst = jobName.substring(0, index - 5);
						if (jCount != 0) {
							System.out.print("\tJob Count: " + jCount
									+ "\tFinished jobs count: " + jFinished
									+ "\tIn progress jobs count: " + jOn);
							jCount = 0;
							jFinished = 0;
							jOn = 0;
						}
						System.out.print("\n" + jnConst);
					}
					JobObject job;
					try {
						job = new JobObject(si, jobName);
						if (job.isFinished())
							jFinished++;
						else
							jOn++;
						jCount++;
					} catch (NoSuchJobException e) {
						e.printStackTrace();
					}
				}
			}
			System.out.print("\tJob Count: " + jCount
					+ "\tFinished jobs count: " + jFinished
					+ "\tIn progress jobs count: " + jOn);
			System.exit(0);
		}

		// for --jobname option
		BenchmarkJob bJob = new BenchmarkJob(si, jobname, nowait);

		CsvBenchmarkRenderer csv = new CsvBenchmarkRenderer();
		csv.renderer(bJob);

		HtmlBenchmarkRenderer html = new HtmlBenchmarkRenderer();
		html.renderer(bJob);
	}
}
