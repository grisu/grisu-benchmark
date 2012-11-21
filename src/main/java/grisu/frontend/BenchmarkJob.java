package grisu.frontend;

import grisu.control.ServiceInterface;
import grisu.control.exceptions.NoSuchJobException;
import grisu.frontend.model.job.JobObject;
import grisu.model.GrisuRegistryManager;
import grisu.model.UserEnvironmentManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedSet;

public class BenchmarkJob
{
	private static final int waittime = 10;
	private final ServiceInterface si;
	private Boolean nowait;
	private final String jobname;
	private int minCpus;
	private Long minRunTime;
	private Map<JobObject, Long> jobs = new HashMap<JobObject, Long>();
	
	public BenchmarkJob(ServiceInterface si, String jobname, Boolean nowait) {
		this.si = si;
		this.jobname = jobname;
		this.nowait=nowait;
		minCpus=9999;
		init();
		jobs=sort();
	}

	
	private void init() 
	{
		UserEnvironmentManager uem = GrisuRegistryManager.getDefault(si).getUserEnvironmentManager();
		SortedSet<String> currentJobList = uem.getCurrentJobnames(true);	

		Long totalExecTime=0L;
		Boolean jobsInProgress=true;

		while(jobsInProgress)
		{
			jobsInProgress=false;
			for(String jname:currentJobList)
			{
				if(jname.contains(jobname))
				{
					System.out.println("Checking job: "+jname);
					try 
					{
						JobObject job = new JobObject(si, jname);
						if(!job.isFinished())
						{
							System.out.println("\tNot finished.");
							if ( !nowait) {
								jobsInProgress=true;
							}
						}
						else
						{
							System.out.println("\tFinished.");
							if(!jobs.containsKey(job))
							{
								Integer cpus=job.getCpus();
								try
								{
									totalExecTime=getExecutionTime(job);
									if ( cpus < minCpus ) 
									{
										minCpus = cpus;
										minRunTime = totalExecTime;
									}
									jobs.put(job, totalExecTime);
								}
								catch(Exception e)
								{
									System.out.println("job failed");
									jobs.put(job, null);
								}
							}
						}
					} catch (NoSuchJobException e) {
						e.printStackTrace();
					}
					catch (NullPointerException e) {
					}
				}
			}
			// wait a few seconds, so we don't overload the backend
			if ( jobsInProgress ) {
				try {
					Thread.sleep(waittime*1000);
				} catch (InterruptedException e) {
				}
			}

		}		
	}

	//Calculate the execution time for a job from the logs in benchmark.log
	private static Long getExecutionTime(JobObject job) throws Exception 
	{
		String jobLog=job.getFileContent("benchmark.log");
		String holder=jobLog.substring(jobLog.lastIndexOf("Started:"));
		Long long1=Long.parseLong(holder.substring(9,holder.indexOf("\n")));
		holder=jobLog.substring(jobLog.lastIndexOf("Finished:"));
		Long long2=Long.parseLong(holder.substring(10,holder.indexOf("\n")));

		System.out.println("Time taken for execution:"+(long2-long1));

		return (long2-long1);
	}

	//sort the list of jobs in ascending order of the number of CPUs
	public Map<JobObject, Long> sort()
	{
		ArrayList<JobObject> jobSet=new ArrayList<JobObject>(jobs.keySet());
		Collections.sort(jobSet, new Comparator<JobObject>(){
			public int compare(JobObject j1, JobObject j2)
			{
				return (j1.getCpus()-j2.getCpus());
			}
		});

		Iterator<JobObject> it=jobSet.iterator();
		Map<JobObject, Long> sortedMap=new LinkedHashMap<JobObject, Long>();
		JobObject jo;
		while(it.hasNext())
		{
			jo=it.next();
			sortedMap.put(jo, jobs.get(jo));
		}
		return sortedMap;
	}


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
	
	public Map<JobObject, Long> getJobs() 
	{
		return jobs;
	}

	public void setJobs(Map<JobObject, Long> jobs) {
		this.jobs = jobs;
	}

	public String getJobname() {
		return jobname;
	}

}