package ro.ulbsibiu.acaps.e3s.ctg;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;

import ro.ulbsibiu.acaps.e3s.ctg.E3sDeadline.DeadlineType;

/**
 * The data held by an an E3S benchmark, for building Communication Task Graphs.
 * Each E3S benchmark data contains the data of a <i>single</i> CTG.
 * 
 * @author Ciprian Radu
 *
 */
public class E3sBenchmarkData {
	
	/** the name of the benchmark */
	private String name;
	
	/** the ID of the CTG (an E3S benchmark file may hold multiple CTGs) */
	private int ctgId; 

	private DefaultDirectedWeightedGraph<Object, Object>  ctg;
	
	/** the period of the CTG (measured in seconds). The root task node injects new data with this period */
	private double period;
	
	private List<E3sCommunicationVolume> communicationVolumes;
	
	private List<E3sVertex> vertices;
	
	private List<E3sEdge> edges;
	
	private List<E3sCore> cores;
	
	/** holds all the deadlines (hard and soft) associated to the tasks of this CTG */
	private List<E3sDeadline> deadlines;
	
	/**
	 * Constructor
	 * 
	 * @param name
	 *            the name of the benchmark (cannot be empty)
	 * @param ctgId
	 *            the ID of the CTG kept by this {@link E3sBenchmarkData}
	 */
	public E3sBenchmarkData(String name, int ctgId) {
		assert name != null && name.length() > 0;
		
		this.name = name;
		this.ctgId = ctgId;
		period = 0;
		ctg = new DefaultDirectedWeightedGraph<Object, Object> (Object.class);
		communicationVolumes = new ArrayList<E3sCommunicationVolume>();
		vertices = new ArrayList<E3sVertex>();
		edges = new ArrayList<E3sEdge>();
		cores = new ArrayList<E3sCore>();
		deadlines = new ArrayList<E3sDeadline>();
	}
	
	/**
	 * Sets the period of the CTG (in seconds).
	 * 
	 * @param period the period (must be a positive number)
	 */
	public void setPeriod(double period) {
		assert period >=0;
		this.period = period;
	}
	
	public double getPeriod() {
		return period;
	}
	
	public void addCommunicationVolume(String communicationType, Double communicationVolume) {
		communicationVolumes.add(new E3sCommunicationVolume(communicationType, communicationVolume));
	}

	public void setCommunicationVolumes(List<E3sCommunicationVolume> communicationVolumes) {
		this.communicationVolumes = communicationVolumes;
	}

	public List<E3sCommunicationVolume> getCommunicationVolumes() {
		return communicationVolumes;
	}
	
	public void addTask(String taskName, String taskType) {
		vertices.add(new E3sVertex(taskName, taskType));
	}
	
	public void addEdge(String edgeName, String from, String to, String edgeType) {
		edges.add(new E3sEdge(edgeName, from, to, edgeType));
	}
	
	public void addCore(E3sCore core) {
		cores.add(core);
	}
	
	/**
	 * Adds a deadline to a task of the CTG. The deadline can be hard of soft.
	 * 
	 * @param type the type of the deadline
	 * @param deadlineName the deadline's name
	 * @param taskName the name of the task
	 * @param time the deadline, expressed in seconds
	 */
	public void addDeadline(DeadlineType type, String deadlineName, String taskName, double time) {
		deadlines.add(new E3sDeadline(type, deadlineName, taskName, time));
	}
	
	private E3sCommunicationVolume findCommunicationVolume(String type) {
		E3sCommunicationVolume cv = null;
		
		for (int i = 0; i < communicationVolumes.size(); i++) {
			if (type.equals(communicationVolumes.get(i).getType())) {
				cv = communicationVolumes.get(i);
				break;
			}
		}
		
		return cv;
	}
	
	/**
	 * builds the Communication Task Graph
	 */
	public void buildCtg() {
		for (int i = 0; i < vertices.size(); i++) {
			// we do not add the E3sVertex but only its name
			ctg.addVertex(vertices.get(i).getName());
		}
		for (int i = 0; i < edges.size(); i++) {
			E3sCommunicationVolume e3sCommunicationVolume = findCommunicationVolume(edges.get(i).getType());
			double weight = 0;
			if (e3sCommunicationVolume != null) {
				weight = e3sCommunicationVolume.getVolume();
			}
			edges.get(i).setWeight(weight);
			ctg.addEdge(edges.get(i).getFrom(), edges.get(i).getTo(), edges.get(i));
			ctg.setEdgeWeight(edges.get(i), weight);
		}
	}
	
	public String getName() {
		return name;
	}

	public int getCtgId() {
		return ctgId;
	}

	public DefaultDirectedWeightedGraph<Object, Object>  getCtg() {
		return ctg;
	}

	public List<E3sVertex> getVertices() {
		return vertices;
	}

	public List<E3sCore> getCores() {
		return cores;
	}

	public List<E3sEdge> getEdges() {
		return edges;
	}

	public List<E3sDeadline> getDeadlines() {
		return deadlines;
	}
	
}
