package grisu.frontend;

public class JobDetailsVO
{
	
	public static final long THRESHOLD = 100;
	
	private String jobName;
	private int hostCount;
	private Boolean status;
	private int cpus;
	private int wallTime;
	private Long executionTime;
	private double efficiency;
	
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
		return this.executionTime * this.cpus;
	}

	public double getEfficiency(JobDetailsVO baselineJob) {
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