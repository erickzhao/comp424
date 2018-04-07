package student_player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import boardgame.Board;
import boardgame.Move;
import tablut.TablutBoardState;
import tablut.TablutMove;
import tablut.TablutPlayer;

public class TreeSearch {
	// define number codes
    private static final int SWEDE = 1;
    private static final int MUSCOVITE = 0;
    private static final int TIME_LIMIT_MS = 2000;
    private static final int TIME_BUFFER_MS = 100;
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
        	Node selectedNode = select(root);
        	expand(selectedNode);
        	boolean isWin = simulate(selectedNode);
        	backprop(selectedNode, isWin);
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
	
	private static boolean simulate(Node node) {
		TablutBoardState cloneBS = (TablutBoardState) node.getBoardState().clone();
		
		while (cloneBS.getWinner() == Board.NOBODY) {
			Move randomMove = cloneBS.getRandomMove();
			cloneBS.processMove((TablutMove) randomMove);
		}
		
		return (cloneBS.getWinner() == player_id);
	}
	
	private static void backprop(Node node, boolean isWin) {
		Node currentNode = node;
		
		if (isWin) {
			currentNode.addWin();
		} else {
			currentNode.addLoss();
		}
		
		if (!currentNode.isRoot()) {
			backprop(currentNode.getParent(), isWin);
		}
	}
}
