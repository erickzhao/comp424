package student_player;

import java.util.Collections;
import java.util.List;

import boardgame.Board;
import boardgame.BoardState;
import boardgame.Move;
import tablut.TablutBoardState;
import tablut.TablutMove;

public class TreeSearch {
	// number codes for teams
    private static final int SWEDE = 1;
    private static final int MUSCOVITE = 0;
    
    // defines which ID is player and opponent
    private static int opp_id;
    private static int player_id;
    
    // limits for computational budget purposes
    private static final int MAX_TREE_DEPTH = 10;
    private static final int MAX_SIMULATION_TURNS = 20;
    
    // timing constants to prevent loop from timing out
    private static final int TIME_LIMIT_MS = 2000;
    private static final int GRACE_PERIOD_MS = 1200;
    
    private static TablutMove firstRolloutMove;
	
    /**
     * Runs a Monte Carlo Tree Search to find next move
     * @param boardState	initial board state before move
     * @return				next best move as per MCTS simulations
     */
	public static Move searchForMove(TablutBoardState boardState) {
        
		// make player IDs available for tree search use
        opp_id = boardState.getOpponent();
        player_id = (opp_id == SWEDE) ? MUSCOVITE : SWEDE;
        
        // initialize tree for tree search
        Node root = new Node(boardState, null);
        
        final long START_TIME = System.currentTimeMillis();
        // loop Monte Carlo simulations until time budget runs out
        // occasionally algorithm will get blocked for one reason or another, so add
        // grace period to let current iteration finish
        while (System.currentTimeMillis() - START_TIME < TIME_LIMIT_MS - GRACE_PERIOD_MS) {
        	
        	// 1. descent with tree policy (upper confidence trees)
        	Node selectedLeaf = descendAndGetBestLeaf(root);

        	// 2. roll out with default policy (random simulations)
        	double winScore = rollOutAndGetWinScore(selectedLeaf);

        	// 3. update win scores with backpropagation
        	backprop(selectedLeaf, winScore);
        	
        	// 4. grow tree with first simulated node from default policy
        	growSearchTree(selectedLeaf);
        }
        
        // once time expired, select best move
        Node bestNode = Collections.max(root.getChildren());
        return bestNode.getPreviousMove();
	}
	
	/**
	 * Descent phase of Monte Carlo Tree Search.
	 * Selects best child node according to UCT recursively until leaf is found
	 * @param node	current node
	 * @return		best leaf according to UCT
	 */
	private static Node descendAndGetBestLeaf(Node node) {
		Node selected = node;
		List<Node> children = node.getChildren();
		if (children.size() > 0) {
			Node bestChildNode = node.getBestChild();
			return descendAndGetBestLeaf(bestChildNode);
		}
		return selected;
	}
	
	/**
	 * Growth phase of Monte Carlo Tree Search
	 * Grows tree by all children of single parameter node
	 * @param node		node to be expanded
	 */
	private static void growSearchTree(Node node) {
		
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

					Node child = new Node(cloneBS, move);
					node.addChild(child);
					
			    	if (b-a > 50) {
			    		System.out.printf("It took %d ms to expand node number %d\n",b-a, n);
			    	}
				}
			}
		}
		
//		TablutBoardState bs = node.getBoardState();
//		TablutBoardState cloneBS = (TablutBoardState) bs.clone();
//		Node child = new Node(cloneBS, moveToChild);
//		node.addChild(child);
	}
	
	/**
	 * Rollout phase of Monte Carlo Tree Search.
	 * Simulates a match with random simulations.
	 * @param node
	 * @return
	 */
	private static double rollOutAndGetWinScore(Node node) {
		TablutBoardState cloneBS = (TablutBoardState) node.getBoardState().clone();
		int initialTurnNumber = cloneBS.getTurnNumber();
		
		Move randomMove = cloneBS.getRandomMove();
		cloneBS.processMove((TablutMove) randomMove);
		firstRolloutMove = (TablutMove) randomMove;
		
		// keep simulating until cap of turns reached or no winner has been found
		while (cloneBS.getWinner() == Board.NOBODY && (cloneBS.getTurnNumber()-initialTurnNumber) < MAX_SIMULATION_TURNS) {
			randomMove = cloneBS.getRandomMove();
			cloneBS.processMove((TablutMove) randomMove);
		}
		
		// 1 for win, 0 for draw, -1 for loss,
		if (cloneBS.getWinner() == player_id) {
			return 1;
		} else if (cloneBS.getWinner() == Board.NOBODY) {
			return 0;
		} else {
			return -1;
		}
	}
	
	/**
	 * Update phase of Monte Carlo Tree Search
	 * Backpropagates win score and number of visits recursively to parent nodes 
	 * @param node		current node
	 * @param winScore	win score from result rollout phase
	 */
	private static void backprop(Node node, double winScore) {
		Node currentNode = node;
		
		currentNode.addResult(winScore);
		
		if (!currentNode.isRoot()) {
			backprop(currentNode.getParent(), winScore);
		}
	}
	
	/**
	 * Determines if move is okay to play
	 * @param node	node with current state
	 * @param move	tentative move
	 * @return		if move can be played without harming player
	 */
	private static boolean isGoodMove(Node node, TablutMove move) {
		return node.isRoot() || node.getPreviousMove().getStartPosition().distance(move.getEndPosition()) != 0;
	}
}
