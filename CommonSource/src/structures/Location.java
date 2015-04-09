package structures;

import jade.util.leap.Serializable;

public class Location implements Serializable {

	protected double latitude;
	protected double longitude;

	public Location() {
		latitude = 0;
		longitude = 0;
	}

	public Location(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

}
