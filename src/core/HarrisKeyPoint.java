package core;

import java.awt.Point;

public class HarrisKeyPoint {
	private Point position;
	private double response;
	public HarrisKeyPoint(Point position, double response) {
		this.position = position;
		this.response = response;
	}
	public Point getPosition() {
		return position;
	}
	public double getResponse() {
		return response;
	}
}
