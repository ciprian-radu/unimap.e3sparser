package ro.ulbsibiu.acaps.e3s.ctg;

public class E3sCommunicationVolume {

	private String type;
	
	private Double volume;
	
	public E3sCommunicationVolume(String type, Double volume) {
		this.type = type;
		this.volume = volume;
	}
	
	public String getType() {
		return type;
	}
	
	public Double getVolume() {
		return volume;
	}

	@Override
	public String toString() {
		return "TYPE " + type + " " + volume;
	}
	
}
