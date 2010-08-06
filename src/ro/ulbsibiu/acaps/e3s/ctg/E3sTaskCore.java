package ro.ulbsibiu.acaps.e3s.ctg;

import ro.ulbsibiu.acaps.e3s.ctg.E3sCore.E3sCoreParams;

/**
 * Keeps some performance metrics of a task, as it is processed by a certain IP core.
 * The following performance metrics are specified in an E3S benchmark:
 * <ul>
 * <li>type</li>
 * <li>version</li>
 * <li>valid</li>
 * <li>task_time</li>
 * <li>preempt_time</li>
 * <li>code_bits</li>
 * <li>task_power</li>
 * </ul>
 * 
 * @see E3sCore
 * 
 * @author cipi
 *
 */
public class E3sTaskCore {

	/**
	 * The performance metrics of a task
	 * 
	 * @author cipi
	 *
	 */
	public static enum E3sTaskCoreParams {
		TYPE, VERSION, VALID, TASK_TIME, PREEMPT_TIME, CODE_BITS, TASK_POWER
	}
	
	/** unique identifier for a task */
	private double type;
	
	private double version;
	
	private double valid;
	
	/** the execution time of a task (in seconds) */
	private double taskTime;
	
	private double preemtTime;
	
	/** the size of the executed code (in bits) */
	private double codeBits;
	
	/** the power consumption for the execution of this task (in Watt) */
	private double taskPower;

	/**
	 * Sets a task core parameter
	 * 
	 * @param parameter the parameter
	 * @param value the parameter's value
	 */
	public void setTaskCoreParameter (E3sTaskCoreParams parameter, double value) {
		switch (parameter) {
		case CODE_BITS:
			codeBits = value;
			break;
		case PREEMPT_TIME:
			preemtTime = value;
			break;
		case TASK_POWER:
			taskPower = value;
			break;
		case TASK_TIME:
			taskTime = value;
			break;
		case TYPE:
			type = value;
			break;
		case VALID:
			valid = value;
			break;
		case VERSION:
			version = value;
			break;
		default:
		}
	}
	
	public double getType() {
		return type;
	}

	public void setType(double type) {
		this.type = type;
	}

	public double getVersion() {
		return version;
	}

	public void setVersion(double version) {
		this.version = version;
	}

	public double getValid() {
		return valid;
	}

	public void setValid(double valid) {
		this.valid = valid;
	}

	public double getTaskTime() {
		return taskTime;
	}

	public void setTaskTime(double taskTime) {
		this.taskTime = taskTime;
	}

	public double getPreemtTime() {
		return preemtTime;
	}

	public void setPreemtTime(double preemtTime) {
		this.preemtTime = preemtTime;
	}

	public double getCodeBits() {
		return codeBits;
	}

	public void setCodeBits(double codeBits) {
		this.codeBits = codeBits;
	}

	public double getTaskPower() {
		return taskPower;
	}

	public void setTaskPower(double taskPower) {
		this.taskPower = taskPower;
	}
	
}
