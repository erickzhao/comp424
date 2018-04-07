package student_player;

import java.util.Collections;
import java.util.List;

import boardgame.Board;
import boardgame.BoardState;
import boardgame.Move;
import tablut.TablutBoardState;
import tablut.TablutMove;

public class TreeSearch {
	// define number codes
    private static final int SWEDE = 1;
    private static final int MUSCOVITE = 0;
    private static final int MAX_TREE_DEPTH = 10;
    private static final int MAX_SIMULATION_TURNS = 20;
    private static final int TIME_LIMIT_MS = 2000;
    private static final int TIME_BUFFER_MS = 1200;
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
			return select(node.getBestChild());
		}
		return selected;
	}
	
	private static void expand(Node node) {
		
		if (node.getDepth() < MAX_TREE_DEPTH) {

			TablutBoardState bs = node.getBoardState();
			List<TablutMove> options = bs.getAllLegalMoves();
			int n = 0;
			for (TablutMove move : options) {
				if (isGoodMove(node, move)) {
			    	long a = System.currentTimeMillis();
			    	n++;
					TablutBoardState cloneBS = (TablutBoardState) bs.clone();
			    	long b = System.currentTimeMillis();
					cloneBS.processMove(move);

					Node child = new Node(cloneBS, move);
					node.addChild(child);
					
			    	if (b-a > 50) {
			    		System.out.printf("It took %d ms to expand node number %d\n",b-a, n);
			    	}
				}
			}
		}
	}
	
	private static double simulate(Node node) {
		TablutBoardState cloneBS = (TablutBoardState) node.getBoardState().clone();
		int initialTurnNumber = cloneBS.getTurnNumber();
		
		while (cloneBS.getWinner() == Board.NOBODY && (cloneBS.getTurnNumber()-initialTurnNumber) < MAX_SIMULATION_TURNS) {
			Move randomMove = cloneBS.getRandomMove();
			cloneBS.processMove((TablutMove) randomMove);
		}
		
		if (cloneBS.getWinner() == player_id) {
			return 1;
		} else if (cloneBS.getWinner() == Board.NOBODY) {
			return 0;
		} else {
			return -1;
		}
	}
	
	private static void backprop(Node node, double winScore) {
		Node currentNode = node;
		
		currentNode.addResult(winScore);
		
		if (!currentNode.isRoot()) {
			backprop(currentNode.getParent(), winScore);
		}
	}
	
	private static boolean isGoodMove(Node node, TablutMove move) {
		return node.isRoot() || node.getPreviousMove().getStartPosition().distance(move.getEndPosition()) != 0;
	}
}
