package communication;

import java.util.Date;

import structures.Location;

import jade.core.AID;
import jade.util.leap.Serializable;

public class Order extends Message implements Serializable, Comparable<Order> {
	
	protected String title;
	protected String description;
	protected OrderStatus status;

	public Order(AID aid, String title, String description) {
		super(aid);
		date = new Date();
		this.title = title;
		this.description = description;
		type = MessageType.ORDER;
		status = OrderStatus.NEW;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setStatus(OrderStatus status) {
		this.status = status;
	}
	
	public OrderStatus getStatus() {
		return status;
	}

	@Override
	public int compareTo(Order o2) {
		return this.getStatus().compareTo(o2.getStatus());
	}
	

}
