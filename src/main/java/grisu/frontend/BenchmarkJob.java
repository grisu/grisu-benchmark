package grisu.frontend;

import grisu.control.ServiceInterface;
import grisu.control.exceptions.NoSuchJobException;
import grisu.frontend.model.job.GrisuJob;
import grisu.model.GrisuRegistryManager;
import grisu.model.UserEnvironmentManager;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import COM.claymoresystems.util.Bench;
import au.com.bytecode.opencsv.CSVReader;

public class BenchmarkJob {
	
	
	public static JobDetailsVO findBaselineJob(List<BenchmarkJob> jobs) {
		
		int minCpus = Integer.MAX_VALUE;
		JobDetailsVO minJob = null;
		for ( BenchmarkJob j : jobs ) {
			for (JobDetailsVO temp : j.getJobs() ) {
				
				if (temp.getExecutionTime() == null || temp.getExecutionTime() < JobDetailsVO.THRESHOLD ) {
					continue;
				}
				
				if ( temp.getCpus() < minCpus) {
					minCpus = temp.getCpus();
					minJob = temp;
				} else if (temp.getCpus() == minCpus) {


					if ( temp.getTotalExecutionTime() < minJob.getTotalExecutionTime()  ) {
						minJob = temp;
					}

				}
			}
		}
		
		return minJob;
		
	}
	
	private static final int waittime = 10;
	private final ServiceInterface si;
	private Boolean nowait;
	private String jobname;
	private int minCpus;
	private Long minRunTime;
	// private Map<GrisuJob, Long> jobs = new HashMap<GrisuJob, Long>();
	// private Map<JobDetailsVO, Long> jobs = new HashMap<JobDetailsVO, Long>();
	private List<JobDetailsVO> jobs = new ArrayList<JobDetailsVO>();
	private List<String> jList = new ArrayList<String>();
	private List<String[]> csvReadList;
	
	private String description = Client.NO_DESCRIPTION;

	public BenchmarkJob(ServiceInterface si, String jobname, Boolean nowait) {
		this.si = si;
		this.jobname = jobname;
		this.nowait = nowait;
		minCpus = 9999;
		init();
		// jobs=sort();

		Collections.sort(jobs, new Comparator<JobDetailsVO>() {

			@Override
			public int compare(JobDetailsVO j1, JobDetailsVO j2) {
				// TODO Auto-generated method stub
				return (j1.getCpus() - j2.getCpus());
			}
		});
	}
	
	public String getDescription() {
		return this.description;
	}

	private void init() {

		Long totalExecTime = 0L;
		Boolean jobsInProgress = true;
		int tempCpu;
		Long tempRuntime;
		//if jobname specified at the command line is a csv file, populate the jobs list from this csv file
		if (jobname.endsWith(".csv")) {
			String desc = Client.NO_DESCRIPTION;
			CSVReader reader=null;
			try {
				reader = new CSVReader(new FileReader(jobname));
				csvReadList = reader.readAll();
				String[] jobDets;
				for (int i = 1; i < csvReadList.size(); i++) {
					jobDets = csvReadList.get(i);

					JobDetailsVO jDetails = new JobDetailsVO(this);
					jDetails.setJobName(jobDets[0]);
					jDetails.setHostCount(Integer.parseInt(jobDets[1]));
					jDetails.setStatus(Boolean.parseBoolean(jobDets[2]));
					tempCpu=Integer.parseInt(jobDets[3]);
					jDetails.setCpus(tempCpu);
					jDetails.setWallTime(Integer.parseInt(jobDets[4]));
					if (jobDets[5].length() > 0) {
						tempRuntime=Long.parseLong(jobDets[5]);
						jDetails.setExecutionTime(tempRuntime);
						
						if(tempCpu<minCpus)
						{
							minCpus = tempCpu;
							minRunTime = tempRuntime;
						}
					}
					jDetails.setDescription(desc);
					jobs.add(jDetails);
				}
				reader.close();
			} catch (FileNotFoundException e) {
				// e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
		//if the job name specified is not a csv file, find out the details of each job in that benchmark
		else {
			UserEnvironmentManager uem = GrisuRegistryManager.getDefault(si)
					.getUserEnvironmentManager();
			SortedSet<String> currentJobList = uem.getCurrentJobnames(true);

			while (jobsInProgress) {
				jobsInProgress = false;
				for (String jname : currentJobList) {
					String rex = jobname+"_\\d\\d\\d\\d_cpus_.*";
					if (jname.matches(rex)) {
//					if (jname.contains(jobname)) {
						System.out.println("Checking job: " + jname);
						try {
							GrisuJob job = new GrisuJob(si, jname);
							if (!job.isFinished()) {
								System.out.println("\tNot finished.");
								if (!nowait) {
									jobsInProgress = true;
								}
							} else {
								System.out.println("\tFinished.");
								if (!jList.contains(jname)) {
									Integer cpus = job.getCpus();

									JobDetailsVO jDetails = new JobDetailsVO(this);
									jDetails.setJobName(jname);
									jDetails.setHostCount(job.getHostCount());
									jDetails.setStatus(job.isSuccessful(true));
									jDetails.setWallTime(job
											.getWalltimeInSeconds());
									jDetails.setCpus(cpus);
									String desc = job.getDescription();
									if ( StringUtils.isBlank(desc) ) {
										desc = Client.NO_DESCRIPTION;
									} else {
										this.description = desc;
									}
									jDetails.setDescription(desc);
									try {
										totalExecTime = getExecutionTime(job);
										if (cpus < minCpus) {
											minCpus = cpus;
											minRunTime = totalExecTime;
										}

										jDetails.setExecutionTime(totalExecTime);
										// jobs.put(jDetails, totalExecTime);
										// jobs.add(jDetails);
									} catch (Exception e) {
										System.out.println("job failed");
										// jobs.add(jDetails);
									}
									jobs.add(jDetails);
									jList.add(jname);
								}
							}
						} catch (NoSuchJobException e) {
							e.printStackTrace();
						} catch (NullPointerException e) {
						}
					}
				}
				// wait a few seconds, so we don't overload the backend
				if (jobsInProgress) {
					try {
						Thread.sleep(waittime * 1000);
					} catch (InterruptedException e) {
					}
				}

			}
		}
	}

	// Calculate the execution time for a job from the logs in benchmark.log
	private static Long getExecutionTime(GrisuJob job) throws Exception {
		String jobLog = job.getFileContent("benchmark.log");
		String holder = jobLog.substring(jobLog.lastIndexOf("Started:"));
		Long long1 = Long.parseLong(holder.substring(9, holder.indexOf("\n")));
		holder = jobLog.substring(jobLog.lastIndexOf("Finished:"));
		Long long2 = Long.parseLong(holder.substring(10, holder.indexOf("\n")));

		System.out.println("Time taken for execution:" + (long2 - long1));

		return (long2 - long1);
	}

	/**
	 * //sort the list of jobs in ascending order of the number of CPUs public
	 * Map<JobDetailsVO, Long> sort() { ArrayList<JobDetailsVO> jobSet=new
	 * ArrayList<JobDetailsVO>(jobs.keySet()); Collections.sort(jobSet, new
	 * Comparator<JobDetailsVO>(){ public int compare(JobDetailsVO j1,
	 * JobDetailsVO j2) { return (j1.getCpus()-j2.getCpus()); } });
	 * 
	 * Iterator<JobDetailsVO> it=jobSet.iterator(); //Map<GrisuJob, Long>
	 * sortedMap=new LinkedHashMap<GrisuJob, Long>(); Map<JobDetailsVO, Long>
	 * sortedMap=new LinkedHashMap<JobDetailsVO, Long>(); JobDetailsVO jo;
	 * while(it.hasNext()) { jo=it.next(); sortedMap.put(jo, jobs.get(jo)); }
	 * return sortedMap; }
	 **/

	public void setMinRunTime(Long minRunTime) {
		this.minRunTime = minRunTime;
	}

	public int getMinCpus() {
		return minCpus;
	}

	public void setMinCpus(int minCpus) {
		this.minCpus = minCpus;
	}

	public Long getMinRunTime() {
		return minRunTime;
	}

	public List<JobDetailsVO> getJobs() {
		return jobs;
	}

	public void setJobs(List<JobDetailsVO> jobs) {
		this.jobs = jobs;
	}

	public String getJobname() {
		return jobname;
	}
	
	public void setJobname(String jobname) {
		this.jobname=jobname;
	}
	
	public String toString() {
		
		if ( getJobname().endsWith(".csv") ) {
			String filename = FilenameUtils.getName(getJobname());
			return FilenameUtils.removeExtension(filename);
		}
		return getJobname();
	}

}