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

	/** the name of the core */
	private String name;
	
	/** the ID of the core */
	private String id;
	
	/**
	 * The parameters of the IP core
	 * 
	 * @author cipi
	 *
	 */
	public static enum E3sCoreParams {
		// note that their order must match the order from the .tgff file
		
		/** the price of the core (probably in dollars) */
		PRICE,
		
		/** always set to 1 (it means that the core has some local memory where it can buffer data) */
		BUFFERED,
		
		/** the maximum operating frequency of the core */
		MAX_FREQ,
		
		/** the width of the core */
		WIDTH,
		
		/** the height of the core */
		HEIGHT,
		
		/** always set to 0.275 */
		DENSITY,
		
		/** the power required to preempt a task (always set to 0) */
		PREEMPT_POWER,
		
		/** the energy consumed for sending a bit (always set to 0) */
		COMMUN_EN_BIT,
		
		/** the energy consumed for I/O operations (always set to 0) */
		IO_EN_BIT,
		
		/** equals to (task_power / 10) The parameter task_power belongs to a task */
		IDLE_POWER
	}
	
	/** the price of the core (probably in dollars) */
	private double price;
	
	/** always set to 1 (it means that the core has some local memory where it can buffer data) */
	private double buffered;
	
	/** the maximum operating frequency of the core */
	private double maxFrequency;
	
	/** the width of the core */
	private double width;
	
	/** the height of the core */
	private double height;
	
	/** always set to 0.275 */
	private double density;
	
	/** the power required to preempt a task (always set to 0) */
	private double preemptPower;
	
	/** the energy consumed for sending a bit (always set to 0) */
	private double communicationEnergyBit;
	
	/** the energy consumed for I/O operations (always set to 0) */
	private double ioEnergy;
	
	/** equals to (task_power / 10) The parameter task_power belongs to a task */
	private double idlePower;

	// ********************************************************************* //
	
	/** keeps all the tasks associated to this IP core */
	private List<E3sTaskCore> tasks = new ArrayList<E3sTaskCore>();
	
	/**
	 * Constructor
	 * 
	 * @param name
	 *            the name of the IP core
	 * @param id
	 *            the ID of the IP core
	 */
	public E3sCore(String name, String id) {
		this.name = name;
		this.id = id;
	}
	
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
	public E3sTaskCore getE3sTaskCore (String type) {
		E3sTaskCore task = null;
		for (int i = 0; i < tasks.size(); i++) {
			if (type.equals(tasks.get(i).getType())) {
				task = tasks.get(i);
				break;
			}
		}
		return task;
	}
	
	/**
	 * Retrieves a list with all the {@link E3sTaskCore} belonging to this core.
	 * 
	 * @return a list with all the {@link E3sTaskCore}
	 */
	public List<E3sTaskCore> getE3sTaskCores() {
		return tasks;
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
			communicationEnergyBit = value;
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
	
	public String getName() {
		return name;
	}

	public String getId() {
		return id;
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

	public double getCommunicationEnergyBit() {
		return communicationEnergyBit;
	}

	public void setCommunicationEnergyBit(double communicationEnergy) {
		this.communicationEnergyBit = communicationEnergy;
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
