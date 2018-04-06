package student_player;

import java.util.ArrayList;
import java.util.List;
import tablut.TablutBoardState;

public class Node {
	private TablutBoardState boardState;
	private Node parent;
	private List<Node> children;
	private int winCount;
	private int visitCount;
	
	public Node(TablutBoardState bs) {
        super();
        boardState = bs;
        children = new ArrayList<Node>();
        winCount = 0;
        visitCount = 0;
	}
	
	public boolean isRoot() {
		return parent == null;
	}
	
	public Node getParent() {
		return parent;
	}
	
	public List<Node> getChildren() {
		return children;
	}
	
	public List<Node> addChild(Node node) {
		children.add(node);
		return children;
	}
	

	public List<Node> addChildren(List<Node> nodes) {
		children.addAll(nodes);
		return children;
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
}
