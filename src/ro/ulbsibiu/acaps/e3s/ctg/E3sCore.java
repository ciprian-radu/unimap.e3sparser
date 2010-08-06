package ro.ulbsibiu.acaps.e3s.ctg;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the parameters of an IP (Intellectual Property) core.
 * The following IP core parameters are specified in an E3S benchmark:
 * <ul>
 * <li>price</li>
 * <li>buffered</li>
 * <li>max_freq</li>
 * <li>width</li>
 * <li>height</li>
 * <li>density</li>
 * <li>preempt_power</li>
 * <li>commun_en_bit</li>
 * <li>io_en_bit</li>
 * <li>idle_power</li>
 * </ul>
 * 
 * @see E3sTaskCore
 * 
 * @author cipi
 *
 */
public class E3sCore {

	/**
	 * The parameters of the IP core
	 * 
	 * @author cipi
	 *
	 */
	public static enum E3sCoreParams {
		// note that their order must match the order from the .tgff file
		PRICE, BUFFERED, MAX_FREQ, WIDTH, HEIGHT, DENSITY, PREEMPT_POWER, COMMUN_EN_BIT, IO_EN_BIT, IDLE_POWER
	}
	
	private double price;
	
	private double buffered;
	
	private double maxFrequency;
	
	private double width;
	
	private double height;
	
	private double density;
	
	private double preemptPower;
	
	private double communicationEnergy;
	
	private double ioEnergy;
	
	private double idlePower;

	/** the list with performance metrics of tasks */
	private List<E3sTaskCore> tasks = new ArrayList<E3sTaskCore>();
	
	/**
	 * Adds an {@link E3sTaskCore} to the list of tasks processed by this core.
	 * 
	 * @param e3sTaskCore the task
	 */
	public void addE3sTaskCore (E3sTaskCore e3sTaskCore) {
		tasks.add(e3sTaskCore);
	}
	
	/**
	 * Retrieves the {@link E3sTaskCore} with the specified type.
	 * Note that <tt>null</tt> will be returned in case no task is found.
	 * It is assumed that a single task exists with an certain type.
	 * 
	 * @param type the type of the task
	 * @return the {@link E3sTaskCore} or <tt>null</tt>
	 */
	public E3sTaskCore getE3sTaskCore (double type) {
		E3sTaskCore task = null;
		for (int i = 0; i < tasks.size(); i++) {
			if (type == tasks.get(i).getType()) {
				task = tasks.get(i);
				break;
			}
		}
		return task;
	}
	
	/**
	 * Sets a core parameter
	 * 
	 * @param parameter the parameter
	 * @param value the parameter's value
	 */
	public void setCoreParameter (E3sCoreParams parameter, double value) {
		switch (parameter) {
		case BUFFERED:
			buffered = value;
			break;
		case COMMUN_EN_BIT:
			communicationEnergy = value;
			break;
		case DENSITY:
			density = value;
			break;
		case HEIGHT:
			height = value;
			break;
		case IDLE_POWER:
			idlePower = value;
			break;
		case IO_EN_BIT:
			ioEnergy = value;
			break;
		case MAX_FREQ:
			maxFrequency = value;
			break;
		case PREEMPT_POWER:
			preemptPower = value;
			break;
		case PRICE:
			price = value;
			break;
		case WIDTH:
			width = value;
			break;
		default:
		}
	}
	
	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public double getBuffered() {
		return buffered;
	}

	public void setBuffered(double buffered) {
		this.buffered = buffered;
	}

	public double getMaxFrequency() {
		return maxFrequency;
	}

	public void setMaxFrequency(double maxFrequency) {
		this.maxFrequency = maxFrequency;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public double getDensity() {
		return density;
	}

	public void setDensity(double density) {
		this.density = density;
	}

	public double getPreemptPower() {
		return preemptPower;
	}

	public void setPreemptPower(double preemptPower) {
		this.preemptPower = preemptPower;
	}

	public double getCommunicationEnergy() {
		return communicationEnergy;
	}

	public void setCommunicationEnergy(double communicationEnergy) {
		this.communicationEnergy = communicationEnergy;
	}

	public double getIoEnergy() {
		return ioEnergy;
	}

	public void setIoEnergy(double ioEnergy) {
		this.ioEnergy = ioEnergy;
	}

	public double getIdlePower() {
		return idlePower;
	}

	public void setIdlePower(double idlePower) {
		this.idlePower = idlePower;
	}
	
}
