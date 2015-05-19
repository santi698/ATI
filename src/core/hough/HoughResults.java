package core.hough;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

public class HoughResults {
	private int votes;
	private List<Point> points;
	public HoughResults() {
		points = new LinkedList<Point>();
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
}
