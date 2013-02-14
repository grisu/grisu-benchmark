package grisu.frontend.benchmark;

import grisu.jcommons.view.html.VelocityUtils;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.velocity.tools.generic.DisplayTool;

import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;

public class VelocityHtmlRenderer implements BenchmarkRenderer {
	
	public VelocityHtmlRenderer() {
	}

	@Override
	public void renderer(List<BenchmarkJob> bJobs) {

    	VelocityHtmlRenderer r = new VelocityHtmlRenderer();
    	Map props = Maps.newHashMap();

    	props.put("benchmarks", bJobs);
    	DisplayTool dt = new DisplayTool();
    	props.put("display", dt);
    	
    	props.put("renderer", r);	
    	
    	JobDetailsVO baseline = BenchmarkJob.findBaselineJob(bJobs);
    	props.put("baseline", baseline);
    	
    	String html = VelocityUtils.render("benchmark", props);
    	
    	try {
			FileUtils.writeStringToFile(new File("./benchmark.html"), html);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
	}
	
    
    public static void main(String[] args) throws IOException {
    	
    	VelocityHtmlRenderer r = new VelocityHtmlRenderer();
    	Map props = Maps.newHashMap();
    	
    	BenchmarkJob bJob1 = new BenchmarkJob(null, "/media/data/work/Workspaces/Grisu/grisu-benchmark/gromacs_mpi_one_host.csv", true);
    	BenchmarkJob bJob2 = new BenchmarkJob(null, "/media/data/work/Workspaces/Grisu/grisu-benchmark/gromacs_smp.csv", true);
    	
    	List<BenchmarkJob> names = Lists.newArrayList();
    	names.add(bJob1);
    	names.add(bJob2);
    	
    	JobDetailsVO baseline = BenchmarkJob.findBaselineJob(names);
    	props.put("baseline", baseline);

    	props.put("benchmarks", names);
    	DisplayTool dt = new DisplayTool();
    	props.put("display", dt);
    	
    	props.put("renderer", r);	
    	
    	String html = VelocityUtils.render("benchmark", props);
    	
    	
    	System.out.println(html);
    	
    	FileUtils.writeStringToFile(new File("/home/markus/result.html"), html);
    	
    }
    
    
    public String createGraphHtml(BenchmarkJob bJob) {
    	
    	String array = createGraphArrayString(bJob);
    	Map<String, Object> properties = Maps.newHashMap();
    	properties.put("table", array);
    	properties.put("name", bJob.toString());
    	//properties.put("title", "Benchmark: "+bJob.toString());
    	properties.put("title", "");
    	
    	return VelocityUtils.render("chart", properties);
    	
    }
    
    public String createTableHtml(BenchmarkJob bJob, JobDetailsVO baselineJob) {
    	
    	StringBuffer result = new StringBuffer();
    	
    	result.append("<tr><th nowrap=\"nowrap\">Cpus</th>");
    	result.append("<th nowrap=\"nowrap\">Execution time</th>");
    	result.append("<th nowrap=\"nowrap\">Total time</th>");
    	result.append("<th nowrap=\"nowrap\" >Efficiency</th></tr>\n");
    	
    	
    	for (JobDetailsVO j : bJob.getJobs()) {
    		result.append("<tr><td>");
    		result.append(j.getCpus());
    		result.append("</td><td>");
    		Long exe = j.getExecutionTime();
    		if ( exe == null || exe < JobDetailsVO.THRESHOLD ) {
    			result.append("n/a");
        		result.append("</td><td>");
        		result.append("n/a");
    		} else {
        		result.append(j.getExecutionTime());
        		result.append("</td><td>");
        		result.append(j.getTotalExecutionTime());
    		}
    		result.append("</td><td>");
    		if ( baselineJob != null && exe != null && exe > JobDetailsVO.THRESHOLD ) {
    			result.append(trimDouble(j.getEfficiency(baselineJob)));
    		} else {
    			result.append("n/a");
    		}
    		result.append("</td></tr>\n");
    	}
    	
    	
    	return result.toString();
    	
    }
    
	public static double trimDouble(double d) {
		DecimalFormat df = new DecimalFormat("#.##");
		return Double.valueOf(df.format(d));
	}
    
    public String createCombinedGraphHtml(List<BenchmarkJob> bJob) {
    	
    	String array = createCombinedGraphArrayString(bJob);
    	Map<String, Object> properties = Maps.newHashMap();
    	properties.put("table", array);
    	properties.put("name", "combined");
    	properties.put("title", "Total compute time across all benchmarks");
    	
    	return VelocityUtils.render("chart", properties);
    	
    }
    
    public String createCombinedEfficiencyGraphHtml(List<BenchmarkJob> bJob, JobDetailsVO baseline) {
    	
    	String array = createCombinedEfficiencyGraphArrayString(bJob, baseline);
    	Map<String, Object> properties = Maps.newHashMap();
    	properties.put("table", array);
    	properties.put("name", "combined_efficiency");
    	//properties.put("title", "Efficiency graph across all benchmarks (using baseline: "+baseline.getCpus()+" cpus, total execution time: "+baseline.getTotalExecutionTime()+")");
    	properties.put("title", "Efficiency graph across all benchmarks");
    	
    	return VelocityUtils.render("chart", properties);
    	
    }
    
    private String createCombinedEfficiencyGraphArrayString(List<BenchmarkJob> jobs, JobDetailsVO baseline) {
    	
    	StringBuffer graph = new StringBuffer("[\n['Cpus'"); 
    	
    	Map<Integer, Map<BenchmarkJob, Double>> efficiencies = new TreeMap<Integer, Map<BenchmarkJob,Double>>();
    	
    	for (BenchmarkJob bj : jobs) {
    		graph.append(",'"+bj.toString()+"'");
    		
    		for ( JobDetailsVO j : bj.getJobs() ) {
    			Integer cpus = j.getCpus();
    			Map<BenchmarkJob, Double> temp = efficiencies.get(cpus);
    			if ( temp == null ) {
    				temp = Maps.newLinkedHashMap();
    				efficiencies.put(cpus, temp);
    			}
    			temp.put(bj, j.getEfficiency(baseline));
    		}
    		
    	}
    	
    	graph.append("]");
    	
    	for ( Integer cpus : efficiencies.keySet() ) {
    		
    		StringBuffer row = new StringBuffer(",\n['"+cpus+"'");
    		for ( BenchmarkJob bj : jobs ) {
    			Double efficiency = efficiencies.get(cpus).get(bj);
    			String exectime = "null";
    			if ( efficiency != null  ) {
    				exectime = efficiency.toString();
    			}
    			row.append(","+exectime);
    		}
    		row.append("]");
    		graph.append(row);
    		
    	}
    	
    	graph.append("]");
    	
		return graph.toString();

    }
    
    private String createCombinedGraphArrayString(List<BenchmarkJob> jobs) {
    	
    	StringBuffer graph = new StringBuffer("[\n['Cpus'"); 
    	
    	Map<Integer, Map<BenchmarkJob, Long>> times = new TreeMap<Integer, Map<BenchmarkJob,Long>>();
    	
    	for (BenchmarkJob bj : jobs) {
    		graph.append(",'"+bj.toString()+"'");
    		
    		for ( JobDetailsVO j : bj.getJobs() ) {
    			Integer cpus = j.getCpus();
    			Map<BenchmarkJob, Long> temp = times.get(cpus);
    			if ( temp == null ) {
    				temp = Maps.newLinkedHashMap();
    				times.put(cpus, temp);
    			}
   				temp.put(bj, j.getTotalExecutionTime());
    		}
    		
    	}
    	
    	graph.append("]");
    	
    	for ( Integer cpus : times.keySet() ) {
    		
    		StringBuffer row = new StringBuffer(",\n['"+cpus+"'");
    		for ( BenchmarkJob bj : jobs ) {
    			Long time = times.get(cpus).get(bj);
    			String exectime = "null";
    			if ( time != null && time > JobDetailsVO.THRESHOLD  ) {
    				exectime = time.toString();
    			}
    			row.append(","+exectime);
    		}
    		row.append("]");
    		graph.append(row);
    		
    	}
    	
    	graph.append("]");
    	
		return graph.toString();

    }
    
    private String createGraphArrayString(BenchmarkJob bJob) {
    	
    	StringBuffer graph = new StringBuffer("[\n['Cpus','Execution time','Total execution time across all cpus']"); 
    	
    	String bJobName = bJob.getJobname();

		for (JobDetailsVO job : bJob.getJobs()) {
			Long executionTime = job.getExecutionTime();
			boolean finished = job.getStatus();

			if (executionTime != null && finished) 
			{
				String jobname = job.getJobName();
				int hosts = job.getHostCount();

				int cpus = job.getCpus();
				int walltime = job.getWallTime();

				Long totalExecutionTime = job.getTotalExecutionTime();


				int jobMinCpu=bJob.getMinCpus();
				Long jobMinRuntime=bJob.getMinRunTime();
				
//				double efficiency = (jobMinCpu * jobMinRuntime
//							.doubleValue()) / totalExecutionTime;
				
				if ( totalExecutionTime <= JobDetailsVO.THRESHOLD ) {
					totalExecutionTime = null;
				}
				if ( executionTime <= JobDetailsVO.THRESHOLD ) {
					executionTime = null;
				}


				graph.append(",\n['" + cpus + "', "	+ executionTime + "," + totalExecutionTime + "]");

			}
		}
		graph.append("]");
		return graph.toString();

    }
    




}
