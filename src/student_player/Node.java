package student_player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import tablut.TablutBoardState;
import tablut.TablutMove;

public class Node implements Comparable<Node> {
	private TablutBoardState boardState;
	private TablutMove previousMove;
	private Node parent;
	private List<Node> children;
	private double winScore;
	private int visitCount;
	
	/**
	 * Creates new tree node
	 * @param initialBoardState	state of tablut game
	 * @param appliedMove		optional move that can be applied on board state
	 */
	public Node(TablutBoardState initialBoardState, TablutMove appliedMove) {
        super();
        
        boardState = initialBoardState;
        previousMove = appliedMove;
        children = new ArrayList<Node>();
        winScore = 0;
        visitCount = 0;
        
        // only try to apply move if one is given
        if (appliedMove != null) {
        	initialBoardState.processMove(appliedMove);
        }
	}
	
	// make comparable so that we can compare which child to choose at end of search
	@Override
	public int compareTo(Node other) {
		return (int) (visitCount - other.getVisitCount());
	}
	
	/**
	 * Indicates if node is root of the tree
	 * @return		if node has no parent
	 */
	public boolean isRoot() {
		return parent == null;
	}
	
	public Node getParent() {
		return parent;
	}

	public Node setParent(Node parentNode) {
		return parent = parentNode;
	}
	
	public List<Node> getChildren() {
		return children;
	}
	
	public TablutBoardState getBoardState() {
		return boardState;
	}
	
	public double getWinScore() {
		return winScore;
	}
	
	public int getVisitCount() {
		return visitCount;
	}
	
	public TablutMove getPreviousMove() {
		return previousMove;
	}
	
	
	/**
	 * Adds child to this node and sets child's parent as this node
	 * @param childNode		node to be appended as child
	 */
	public void addChild(Node childNode) {
		children.add(childNode);
		childNode.setParent(this);
	}
	
	/**
	 * Adds win score from simulation and increments number of visits to node
	 * @param ws	win score from random simulation
	 */
	public void addResult(double ws) {
		winScore +=ws;
		visitCount++;
	}
	
	/**
	 * Calculates best child according to Upper Confidence Tree equation
	 * Used for tree policy in Monte Carlo Tree Search
	 * @return	best child
	 */
	public Node getBestChild() {
		double maxScore = 0;
		List<Node> children = this.getChildren();
		List<Node> unexplored = new ArrayList<Node>();
		Node bestChild = children.get(0);
		
		for (Node child : children) {
			double score = MyTools.getUCTScore(child);
			
			// take note of all unexplored nodes
			if (score == Integer.MAX_VALUE) {
				unexplored.add(child);
			} else if (score > maxScore) {
				maxScore = score;
				bestChild = child;
			}
		}
		
		// randomly select an unexplored node if any exist
		if (unexplored.size() > 0) {
			int random = new Random().nextInt(unexplored.size());
			return unexplored.get(random);
		}
		
		return bestChild;
	}
	/**
	 * Calculates worst child according to Upper Confidence Tree equation
	 * Used for tree policy in Monte Carlo Tree Search
	 * (opponent wants to minimize our win rate)
	 * @return	worst child
	 */
	public Node getWorstChild() {
		double minScore = Integer.MAX_VALUE;
		List<Node> children = this.getChildren();
		Node worstChild = children.get(0);
		
		for (Node child : children) {
			double score = MyTools.getUCTScore(child);
			
			if (score < minScore) {
				minScore = score;
				worstChild = child;
			}
		}
		
		return worstChild;
	}
	
	/**
	 * Returns the current depth of node in tree
	 * @return	depth from root
	 */
	public int getDepth() {
		int depth = 0;
		
		Node currentNode = this;
		while (!currentNode.isRoot()) {
			depth++;
			currentNode = currentNode.getParent();
		}
		return depth;
	}

}
