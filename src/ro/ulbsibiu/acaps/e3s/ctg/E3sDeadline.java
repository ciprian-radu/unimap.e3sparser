package ro.ulbsibiu.acaps.e3s.ctg;

/**
 * Task deadline. The deadline can be hard of soft.
 * 
 * @author Ciprian Radu
 *
 */
public class E3sDeadline {

	/** Hard of soft deadlines */
	public enum DeadlineType {HARD, SOFT};
	
	/** the type of the deadline */
	private DeadlineType type;
	
	/** the deadline's name */
	private String deadlineName;
	
	/** the name of the task to which this deadline is attached */
	private String taskName;
	
	/** the value of the deadline, expressed in seconds */
	private double time;
	
	public E3sDeadline(DeadlineType type, String deadlineName, String taskName, double time) {
		this.type = type;
		this.deadlineName = deadlineName;
		this.taskName = taskName;
		this.time = time;
	}

	public DeadlineType getType() {
		return type;
	}

	public void setType(DeadlineType type) {
		this.type = type;
	}

	public String getDeadlineName() {
		return deadlineName;
	}

	public void setDeadlineName(String deadlineName) {
		this.deadlineName = deadlineName;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}
	
}
