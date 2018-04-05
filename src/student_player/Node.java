package student_player;

import java.util.ArrayList;
import java.util.List;
import tablut.TablutBoardState;

public class Node {
	private TablutBoardState boardState;
	private Node parent;
	private List<Node> children;
	
	public Node() {
        super();
        children = new ArrayList<Node>();
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
}
