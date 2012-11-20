package grisu.frontend;
import grisu.frontend.model.job.JobObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;

public class CsvBenchmarkRenderer implements BenchmarkRenderer
{

	@Override
	public void renderer(String jobname, List<String[]> jobs, int minCpus, Long minRuntime) {
		
		CSVWriter writer = null;
		String[] csvTemp = new String[10];
		csvTemp[1]="Host count";
		csvTemp[2]="Job success status";
		csvTemp[3]="CPUs";
		csvTemp[4]="Wall time";
		csvTemp[5]="Job execution time";
		csvTemp[6]="Execution time across all CPUs";
		csvTemp[7]="Efficiency";
		
		try {
			writer = new CSVWriter(new FileWriter(jobname+".csv"));
			//errWriter = new CSVWriter(new FileWriter(jobname+"_err.csv"));
			writer.writeNext(csvTemp);
			csvTemp[5]=csvTemp[6]=csvTemp[7]=null;
			csvTemp[0]="Job name";
		//	errWriter.writeNext(csvTemp);

		} catch (IOException e) {
			e.printStackTrace();
		}


		for ( String[] values : jobs ) {

			double efficiency = minRuntime.doubleValue()/(minCpus * Long.parseLong(values[6]));
			values[7]=""+ efficiency;
			System.out.println(values[7]);

//			htmlString.append(",\n['"+values[3]+"', "+values[5]+", "+Double.parseDouble(values[6])+"]");
//			effGraphString.append(",\n['"+values[3]+"', "+Double.parseDouble(values[7])+"]");
//			tableString.append("<tr><td>"+values[0]+"</td><td align=\"right\">"+values[3]+"</td><td align=\"right\">"+values[5]+"</td><td align=\"right\">"+trimDouble(Double.parseDouble(values[6]))+"</td><td align=\"right\"> "+trimDouble(Double.parseDouble(values[7]))+"</td></tr>");
//			tableString.append("<tr><td align=\"right\">"+values[3]+"</td><td align=\"right\">"+values[5]+"</td><td align=\"right\">"+trimDouble(Double.parseDouble(values[6]))+"</td><td align=\"right\"> "+trimDouble(Double.parseDouble(values[7]))+"</td></tr>");
			values[0]=null;
			writer.writeNext(values);
		}

		try {
			writer.close();
		//	errWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}
//
//	@Override
//	public void renderer(String jobname, List<? extends Object> jobs,
//			int minCpus, long minRuntime) {
//		// TODO Auto-generated method stub
//		
//	}
	
}