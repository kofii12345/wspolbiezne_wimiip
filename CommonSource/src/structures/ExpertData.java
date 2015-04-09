package structures;

import jade.core.AID;
import jade.util.leap.Serializable;

public class ExpertData implements Serializable {
	
	protected String name;
	protected String category;
	protected AID aid;
	protected Location location;
	protected ExpertStatus status;
	
	public ExpertData(String name, String category, AID aid) {
		this.name = name;
		this.category = category;
		status = ExpertStatus.AVAILABLE;
		location = new Location();
		this.aid = aid;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public AID getAid() {
		return aid;
	}
	public void setAid(AID aid) {
		this.aid = aid;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
	
	public ExpertStatus getStatus() {
		return status;
	}
	
	public void setStatus(ExpertStatus status) {
		this.status = status;
	}
	

}
