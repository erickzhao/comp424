package student_player;

public class UpperConfidenceTree {
	private static final double SCALING_CONSTANT = Math.sqrt(2);
	
	public static double getScore(Node node) {
		int wins = node.getWinCount();
		int visits = node.getVisitCount();
		int parentVisits = node.getParent().getVisitCount();
		
		// explore if node is unvisited
		if (visits == 0) {
			return Integer.MAX_VALUE;
		}
		
		return (double) wins / (double) visits + SCALING_CONSTANT * Math.sqrt(Math.log(parentVisits)/visits);
	}
	
	public static Node getBestChild(Node node) {
		double maxScore = 0;
		Node bestChild = null;
		
		for (Node child : node.getChildren()) {
			double score = getScore(child);
			
			if (score > maxScore) {
				maxScore = score;
				bestChild = child;
			}
			
			if (score == Integer.MAX_VALUE) {
				break;
			}
		}
		
		return bestChild;
	}
	
	public static Node getWorstChild(Node node) {
		double minScore = Integer.MAX_VALUE;
		Node worstChild = null;
		
		for (Node child : node.getChildren()) {
			double score = getScore(child);
			
			if (score < minScore) {
				minScore = score;
				worstChild = child;
			}
		}
		
		return worstChild;
	}

}
