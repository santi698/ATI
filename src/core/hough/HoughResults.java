package core.hough;

import java.util.LinkedList;
import java.util.List;

import javafx.geometry.Point2D;

public class HoughResults {
	private int votes;
	private List<Point2D> points;
	public HoughResults() {
		points = new LinkedList<Point2D>();
	}
	public void vote(Point2D point) {
		votes++;
		points.add(point);
	}
	public int getVotes() {
		return votes;
	}
	public List<Point2D> getPoints() {
		return points;
	}
}
