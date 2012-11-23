package grisu.frontend;

public class JobDetailsVO
{
	
	private String jobName;
	private int hostCount;
	private Boolean status;
	private int cpus;
	private int wallTime;
	private Long executionTime;
	private Long totalExecutionTime;
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
		this.executionTime = executionTime;
	}
	public Long getTotalExecutionTime() {
		return totalExecutionTime;
	}
	public void setTotalExecutionTime(Long totalExecutionTime) {
		this.totalExecutionTime = totalExecutionTime;
	}
	public double getEfficiency() {
		return efficiency;
	}
	public void setEfficiency(double efficiency) {
		this.efficiency = efficiency;
	}
		
}