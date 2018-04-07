package student_player;

import java.util.Collections;
import java.util.List;

import boardgame.Board;
import boardgame.Move;
import tablut.TablutBoardState;
import tablut.TablutMove;

public class TreeSearch {
	// define number codes
    private static final int SWEDE = 1;
    private static final int MUSCOVITE = 0;
    private static final int TIME_LIMIT_MS = 2000;
    private static final int TIME_BUFFER_MS = 800;
    private static int opp_id;
    private static int player_id;
	
	public static Move searchForMove(TablutBoardState boardState) {
        
		// make player IDs available for tree search use
        opp_id = boardState.getOpponent();
        player_id = (opp_id == SWEDE) ? MUSCOVITE : SWEDE;
        
        // initialize tree for tree search
        Node root = new Node(boardState, null);
        
        final long START_TIME = System.currentTimeMillis();
        // loop Monte Carlo simulations until time is a bit under 2s
        while (System.currentTimeMillis() - START_TIME < TIME_LIMIT_MS - TIME_BUFFER_MS) {
        	
        	// 1. selection
        	Node selectedNode = select(root);
        	
        	// 2. expansion
        	expand(selectedNode);
        	
        	// 3. simulation
        	double winScore = simulate(selectedNode);
        	
        	// 4. backpropagation
        	backprop(selectedNode, winScore);
        }
        
        // once time expired, select best move
        Node bestNode = Collections.max(root.getChildren());
        return bestNode.getPreviousMove();
	}
	
	private static Node select(Node node) {
		Node selected = node;
		List<Node> children = node.getChildren();
		if (children.size() > 0) {
			return (node.getBoardState().getTurnPlayer() == player_id) ?
					select(UpperConfidenceTree.getBestChild(node)) :
					select(UpperConfidenceTree.getWorstChild(node));
		}
		return selected;
	}
	
	private static void expand(Node node) {
		List<TablutMove> options = node.getBoardState().getAllLegalMoves();
		for (TablutMove move : options) {
			if (node.isRoot() || node.getPreviousMove().getStartPosition().distance(move.getEndPosition()) != 0) {
				TablutBoardState cloneBS = (TablutBoardState) node.getBoardState().clone();
				cloneBS.processMove(move);
				
				Node child = new Node(cloneBS, move);
				node.addChild(child);
			}
		}
	}
	
	private static double simulate(Node node) {
		TablutBoardState cloneBS = (TablutBoardState) node.getBoardState().clone();
		int initialTurnNumber = cloneBS.getTurnNumber();
		
		while (cloneBS.getWinner() == Board.NOBODY) {
			Move randomMove = cloneBS.getRandomMove();
			cloneBS.processMove((TablutMove) randomMove);
		}
		
		if (cloneBS.getWinner() == player_id) {
			return 40.0/(cloneBS.getTurnNumber()-initialTurnNumber);
		} else {
			return 0;
		}
	}
	
	private static void backprop(Node node, double winScore) {
		Node currentNode = node;
		
		currentNode.addResult(winScore);
		
		if (!currentNode.isRoot()) {
			backprop(currentNode.getParent(), winScore);
		}
	}
}
