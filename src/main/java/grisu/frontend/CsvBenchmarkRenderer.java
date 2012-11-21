package grisu.frontend;

import grisu.frontend.model.job.JobObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;

public class CsvBenchmarkRenderer implements BenchmarkRenderer {

	@Override
	public void renderer(BenchmarkJob bJob) {

		CSVWriter writer = null;
		CSVWriter errWriter = null;
		String[] jobValues = new String[10];
		jobValues[1] = "Host count";
		jobValues[2] = "Job success status";
		jobValues[3] = "CPUs";
		jobValues[4] = "Wall time";
		jobValues[5] = "Job execution time";
		jobValues[6] = "Execution time across all CPUs";
		jobValues[7] = "Efficiency";

		try {
			writer = new CSVWriter(new FileWriter(bJob.getJobname() + ".csv"));
			errWriter = new CSVWriter(new FileWriter(bJob.getJobname()	+ "_err.csv"));
			writer.writeNext(jobValues);
			jobValues[5] = jobValues[6] = jobValues[7] = null;
			jobValues[0] = "Job name";
			errWriter.writeNext(jobValues);

		} catch (IOException e) {
			e.printStackTrace();
		}

		Long executionTime;
		Long totalExecutionTime;
		int cpus;

		for (JobObject job : bJob.getJobs().keySet()) {
			jobValues[1] = "" + job.getHostCount();
			jobValues[2] = "" + job.isSuccessful(true);

			cpus = job.getCpus();
			jobValues[3] = "" + cpus;
			jobValues[4] = "" + job.getWalltimeInSeconds();

			executionTime = bJob.getJobs().get(job);
			if (executionTime != null) {
				jobValues[5] = "" + executionTime;

				totalExecutionTime = (executionTime * cpus);
				jobValues[6] = "" + totalExecutionTime;

				double efficiency = (bJob.getMinCpus() * bJob.getMinRunTime().doubleValue())
						/ totalExecutionTime;
				jobValues[7] = "" + efficiency;
				System.out.println(jobValues[7]);

				jobValues[0] = null;
				writer.writeNext(jobValues);
			} else {
				jobValues[0] = job.getJobname();
				jobValues[5] = jobValues[6] = jobValues[7] = null;
				errWriter.writeNext(jobValues);
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