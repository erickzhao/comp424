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
	
	
	
	public Node(TablutBoardState bs, TablutMove m) {
        super();
        boardState = bs;
        previousMove = m;
        children = new ArrayList<Node>();
        winScore = 0;
        visitCount = 0;
	}
	
	@Override
	public int compareTo(Node other) {
		return (int) (winScore - other.getWinScore());
	}
	
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
	
	public void addChild(Node childNode) {
		children.add(childNode);
		childNode.setParent(this);
	}
	
	public TablutBoardState getBoardState() {
		return boardState;
	}
	
	public void setBoardState(TablutBoardState boardState) {
		this.boardState = boardState;
	}
	
	public void addResult(double ws) {
		winScore +=ws;
		visitCount++;
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
	
	
	
	public Node getBestChild() {
		double maxScore = 0;
		List<Node> children = this.getChildren();
		List<Node> unexplored = new ArrayList<Node>();
		Node bestChild = null;
		
		for (Node child : children) {
			double score = MyTools.getUCTScore(child);
			
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

}
