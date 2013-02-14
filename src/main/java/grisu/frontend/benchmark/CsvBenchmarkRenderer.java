package grisu.frontend.benchmark;

import grisu.frontend.model.job.GrisuJob;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;

public class CsvBenchmarkRenderer implements BenchmarkRenderer {

	//create a csv file from the data for the benchmark job
	@Override
	public void renderer(List<BenchmarkJob> bJobs) {
		
		for ( BenchmarkJob bJob : bJobs ) {
			renderer(bJob);
		}
		
	}
	
	
	private void renderer(BenchmarkJob bJob) {
		
		if ( bJob.getJobname().endsWith(".csv") ) {
			return;
		}

		CSVWriter writer = null;
		CSVWriter errWriter = null;
		String[] jobValues = new String[10];
		jobValues[0] = "Job name";
		jobValues[1] = "Host count";
		jobValues[2] = "Job success status";
		jobValues[3] = "CPUs";
		jobValues[4] = "Wall time";
		jobValues[5] = "Job execution time";
		jobValues[6] = "Execution time across all CPUs";
		jobValues[7] = "Efficiency";

		try {
			writer = new CSVWriter(new FileWriter(bJob.getJobname() + ".csv"));
			errWriter = new CSVWriter(new FileWriter(bJob.getJobname()
					+ "_err.csv"));
			writer.writeNext(jobValues);
			jobValues[5] = jobValues[6] = jobValues[7] = null;
			errWriter.writeNext(jobValues);

		} catch (IOException e) {
			e.printStackTrace();
		}

		Long executionTime;
		Long totalExecutionTime;
		int cpus;

		for (JobDetailsVO job : bJob.getJobs()) {
			jobValues[0] = job.getJobName();
			jobValues[1] = "" + job.getHostCount();
			jobValues[2] = "" + job.getStatus();

			cpus = job.getCpus();
			jobValues[3] = "" + cpus;
			jobValues[4] = "" + job.getWallTime();

			// executionTime = bJob.getJobs().get(job);
			executionTime = job.getExecutionTime();
			if (executionTime != null) {
				jobValues[5] = "" + executionTime;

				totalExecutionTime = job.getTotalExecutionTime();
				jobValues[6] = "" + totalExecutionTime;

				double efficiency = (bJob.getMinCpus() * bJob.getMinRunTime()
						.doubleValue()) / totalExecutionTime;
				jobValues[7] = "" + efficiency;
				System.out.println(jobValues[7]);

				writer.writeNext(jobValues);
			} else {
				jobValues[5] = jobValues[6] = jobValues[7] = null;
				writer.writeNext(jobValues);
			}
		}

		try {
			writer.close();
			errWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}