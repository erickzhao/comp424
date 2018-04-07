package student_player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UpperConfidenceTree {
	private static final double SCALING_CONSTANT = Math.sqrt(2);
	
	public static double getScore(Node node) {
		double winScore = node.getWinScore();
		int visits = node.getVisitCount();
		
		// explore if node is unvisited
		if (visits == 0 || node.isRoot()) {
			return Integer.MAX_VALUE;
		}
		
		int parentVisits = node.getParent().getVisitCount();
		
		return winScore / visits + SCALING_CONSTANT * Math.sqrt(Math.log(parentVisits)/visits);
	}
	
	public static Node getBestChild(Node node) {
		double maxScore = 0;
		List<Node> children = node.getChildren();
		List<Node> unexplored = new ArrayList<Node>();
		Node bestChild = null;
		
		for (Node child : children) {
			double score = getScore(child);
			
			if (score == Integer.MAX_VALUE) {
				unexplored.add(child);
			} else if (score > maxScore) {
				maxScore = score;
				bestChild = child;
			}
		}
		
		if (unexplored.size() > 0) {
			int random = new Random().nextInt(unexplored.size());
			return unexplored.get(random);
		}
		
		return bestChild;
	}
	
	public static Node getWorstChild(Node node) {
		double minScore = Integer.MAX_VALUE;
		List<Node> children = node.getChildren();
		Node worstChild = children.get(0);
		
		for (Node child : children) {
			double score = getScore(child);
			
			if (score < minScore) {
				minScore = score;
				worstChild = child;
			}
		}
		
		return worstChild;
	}

}
