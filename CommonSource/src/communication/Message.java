package communication;

import jade.core.AID;
import jade.util.leap.Serializable;

import java.util.Date;

import structures.Location;

public class Message implements Serializable {
	
	protected String text = null;
	protected AID aid;
	protected MessageType type;
	protected Date date;
	protected Location location;
	byte[] imageInByteCode;
		
	public Message(AID aid) {
		this.aid = aid;
		type = MessageType.MESSAGE;
		this.date = new Date();
		location = new Location();
	}
	
	public Message(AID aid, MessageType type) {
		this.aid = aid;
		this.type = type;
		this.date = new Date();
		location = new Location();
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public AID getAid() {
		return aid;
	}
	
	public void setAid(AID aid) {
		this.aid = aid;
	}
	
	public MessageType getType() {
		return type;
	}
	
	public void setType(MessageType type) {
		this.type = type;
	}
	
	public byte[] getImage() {
		return imageInByteCode;
	}

	public void setImage(byte[] image) {
		this.imageInByteCode = image;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
	

}
