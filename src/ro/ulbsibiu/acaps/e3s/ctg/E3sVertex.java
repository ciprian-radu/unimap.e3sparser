package ro.ulbsibiu.acaps.e3s.ctg;

/**
 * @author Ciprian Radu
 * 
 */
public class E3sVertex {

	private String name;

	private String type;

	public E3sVertex(String name, String type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return "TASK " + name + " TYPE " + type;
	}

}
