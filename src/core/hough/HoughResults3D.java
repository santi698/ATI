package core.hough;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

import javafx.geometry.Point3D;

public class HoughResults3D {
	private int votes;
	private Point3D parameters;
	private List<Point> points;
	public HoughResults3D(Point3D parameters) {
		points = new LinkedList<Point>();
		this.parameters = parameters;
	}
	public void vote(Point position) {
		votes++;
		points.add(position);
	}
	public int getVotes() {
		return votes;
	}
	public List<Point> getPoints() {
		return points;
	}
	public Point3D getParameters() {
		return parameters;
	}
	public String toString() {
		return "HoughResults(" + votes + " votes. Points:" + points.toString() + ")";
	}
}
