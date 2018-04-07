package student_player;

import java.util.ArrayList;
import java.util.List;
import tablut.TablutBoardState;
import tablut.TablutMove;

public class Node implements Comparable<Node> {
	private TablutBoardState boardState;
	private TablutMove previousMove;
	private Node parent;
	private List<Node> children;
	private int winCount;
	private int visitCount;
	
	public Node(TablutBoardState bs, TablutMove m) {
        super();
        boardState = bs;
        previousMove = m;
        children = new ArrayList<Node>();
        winCount = 0;
        visitCount = 0;
	}
	
	@Override
	public int compareTo(Node other) {
		return winCount - other.getWinCount();
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
	
	public void addWin() {
		winCount++;
		visitCount++;
	}
	public void addLoss() {
		visitCount++;
	}
	
	public int getWinCount() {
		return winCount;
	}
	
	public int getVisitCount() {
		return visitCount;
	}
	
	public TablutMove getPreviousMove() {
		return previousMove;
	}

}
