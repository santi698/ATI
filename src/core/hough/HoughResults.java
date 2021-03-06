package core.hough;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

import javafx.geometry.Point2D;

public class HoughResults {
	private int votes;
	private Point2D parameters;
	private List<Point> points;
	public HoughResults(Point2D parameters) {
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
	public Point2D getParameters() {
		return parameters;
	}
	public String toString() {
		return "HoughResults(" + votes + " votes. Points:" + points.toString() + ")";
	}
}
