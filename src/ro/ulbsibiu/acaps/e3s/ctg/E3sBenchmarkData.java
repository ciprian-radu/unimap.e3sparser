package ro.ulbsibiu.acaps.e3s.ctg;

import java.util.ArrayList;
import java.util.List;

import ro.ulbsibiu.acaps.ctg.CommunicationTaskGraph;

/**
 * The data held by an an E3S benchmark, for building Communication Task Graphs 
 * 
 * @author Ciprian Radu
 *
 */
public class E3sBenchmarkData {

	private CommunicationTaskGraph ctg;
	
	private List<E3sCommunicationVolume> communicationVolumes;
	
	private List<E3sVertex> vertices;
	
	private List<E3sEdge> edges;
	
	public E3sBenchmarkData() {
		ctg = new CommunicationTaskGraph();
		communicationVolumes = new ArrayList<E3sCommunicationVolume>();
		vertices = new ArrayList<E3sVertex>();
		edges = new ArrayList<E3sEdge>();
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
	
	public CommunicationTaskGraph getCtg() {
		return ctg;
	}
	
}
