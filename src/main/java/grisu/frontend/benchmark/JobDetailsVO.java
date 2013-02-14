package grisu.frontend.benchmark;

public class JobDetailsVO
{
	
	public static final long THRESHOLD = 20;
	
	private String jobName;
	private int hostCount;
	private Boolean status;
	private Integer cpus;
	private int wallTime;
	private Long executionTime;

	private String description;
	
	public void setDescription(String d) {
		this.description = d;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	private final BenchmarkJob benchmark;
	
	public JobDetailsVO(BenchmarkJob bm) {
		this.benchmark = bm;
	}
	
	public BenchmarkJob getBenchmarkJob() {
		return benchmark;
	}
	
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	public int getHostCount() {
		return hostCount;
	}
	public void setHostCount(int hostCount) {
		this.hostCount = hostCount;
	}
	public Boolean getStatus() {
		return status;
	}
	public void setStatus(Boolean status) {
		this.status = status;
	}
	public int getCpus() {
		return cpus;
	}
	public void setCpus(int cpus) {
		this.cpus = cpus;
	}
	public int getWallTime() {
		return wallTime;
	}
	public void setWallTime(int wallTime) {
		this.wallTime = wallTime;
	}
	public Long getExecutionTime() {
		return executionTime;
	}
	public void setExecutionTime(Long executionTime) {
		if ( executionTime < THRESHOLD ) {
			executionTime = 0L;
		}
		this.executionTime = executionTime;
	}
	public Long getTotalExecutionTime() {
		if ( this.executionTime == null || this.cpus == null ) {
			return 0L;
		}
		return this.executionTime * this.cpus;
	}

	public double getEfficiency(JobDetailsVO baselineJob) {
		if (baselineJob.getTotalExecutionTime().equals(0L) ) {
			return 0;
		}
		if ( getTotalExecutionTime() == null || getTotalExecutionTime().equals(0L) ) {
			return 0;
		}
				
		double efficiency = baselineJob.getTotalExecutionTime()
				.doubleValue() / getTotalExecutionTime();
		return efficiency;
	}
//	public void setEfficiency(double efficiency) {
//		this.efficiency = efficiency;
//	}
		
}