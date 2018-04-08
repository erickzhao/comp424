package student_player;

import java.util.Collections;
import java.util.List;

import boardgame.Board;
import boardgame.Move;
import coordinates.Coord;
import coordinates.Coordinates;
import tablut.TablutBoardState;
import tablut.TablutMove;

public class TreeSearch {
    
    // defines which ID is player and opponent
    private static int opp_id;
    private static int player_id;
    
    // limits for computational budget purposes
    private static final int MAX_TREE_DEPTH = 15;
    private static final int MAX_SIMULATION_TURNS = 20;
    private static final int MAX_CHILDREN_TO_EXPAND = 50;
    private static final int MAX_VISIT_COUNT = 150;
    
    // timing constants to prevent loop from timing out
    private static final int TIME_LIMIT_MS = 2000;
    private static final int GRACE_PERIOD_MS = 1000;
	
    /**
     * Runs a Monte Carlo Tree Search to find next move
     * @param boardState	initial board state before move
     * @return				next best move as per MCTS simulations
     */
	public static Move searchForMove(TablutBoardState boardState) {
        
		// make player IDs available for tree search use
        opp_id = boardState.getOpponent();
        player_id = (opp_id == TablutBoardState.SWEDE) ? TablutBoardState.MUSCOVITE : TablutBoardState.SWEDE;
        
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
        	
        	// 4. grow tree with selected leaf node
        	growSearchTree(selectedLeaf);

            List<Node> children = root.getChildren();
            Node bestNode = Collections.max(children);
            
            // If we've decided to visit a node a certain number of times,
            // we can say that it was the clear choice
            if (bestNode.getVisitCount() >= MAX_VISIT_COUNT) {
            	System.out.println("YOUR ARMY'S ADVISORS HAVE COME TO AN EARLY CONSENSUS ON A TACTICAL DECISION");
            	break;
            }
        }
        
        // once time expired, select best move
        List<Node> children = root.getChildren();
        Node bestNode = Collections.max(children);
        
        // Trying to add manual heuristic stuff here...
        // Never override a winning move
        if (!(bestNode.getBoardState().getWinner() == player_id)) {
        	
        	Coord kingPos = boardState.getKingPosition();
        	
        	// SWEDE Tactics
            if (player_id == TablutBoardState.SWEDE) {
                // Encourage King movement (borrowed snippet from Greedy code)
            	// if a couple turns have passed and we have fewer pieces, go into desperation mode to help the king escape
                if (boardState.getTurnNumber() > 10 || boardState.getNumberPlayerPieces(opp_id) < boardState.getNumberPlayerPieces(player_id) ) {
                    // iterate over King's moves to see if he can move to an open coord on the side
                    Move manualMove = null;
                    for (TablutMove move : boardState.getLegalMovesForPosition(kingPos)) {
                    	Coord newPos = move.getEndPosition();
                    	
                    	// insta break if move is a winner
                    	if (Coordinates.isCorner(newPos)) {
                    		manualMove = move;
                    		break;
                		}
                    	
                    	// if move shifts king to a wall
                    	if (newPos.x == kingPos.x && newPos.y == 0 || newPos.x == kingPos.x && newPos.y == 8 || newPos.x == 0 && newPos.y == kingPos.y || newPos.x == 8 && newPos.y == kingPos.y) {
                    		manualMove = move;
                    	}
                    }
                    
                    if (manualMove != null) {
                    	System.out.println("THE COWARDLY KING ABANDONS THE REMAINS OF HIS ARMY.");
                        return manualMove;
                    }
                }
                
                // Attempt to protect king by pruning out states where it is endangered
                int nextBest = 1;
                
                // sort children by MCTS win score (descending order) and get next best if unsafe position
            	Collections.sort(children, Collections.reverseOrder());
                while(nextBest < children.size() && (MyTools.isStateSafeForKing(boardState) && !MyTools.isStateSafeForKing(bestNode.getBoardState()))) {
                	bestNode = children.get(nextBest);
                	nextBest++;
                }
                
                if (nextBest > 1) {
                	System.out.println("THE KING'S ADVISORS HAVE TALKED HIM OUT OF A POOR TACTICAL DECISION.");
                }
            	
            }
            
            // MUSCOVITE Tactics
            if (player_id == TablutBoardState.MUSCOVITE) {
            	
                // Go greedy: always try to corner the king if he's vulnerable
            	// but only if you aren't superseding a winning move!
            	if (bestNode.getBoardState().getWinner() != player_id) {
                	Move manualMove = null;
                	List<TablutMove> moveset = boardState.getAllLegalMoves();
                	
                	for (TablutMove move : moveset) {
                		if (move.getEndPosition().distance(kingPos) == 1 && !(MyTools.isStateSafeForKing(boardState))) {
                			manualMove = move;
                		}
                	}
                    
                    if (manualMove != null) {
                    	System.out.println("MONTE CARLO MOVE OVERRIDDEN BY MUSCOVITE GREED.");
                    	return manualMove;
                    }
                }
        	}
            
        }
        
        System.out.println("MONTE CARLO STATS FOR BEST NODE - WINSCORE: "+bestNode.getWinScore()+" VISITS: "+bestNode.getVisitCount()+"\n");
        
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
		int depth = node.getDepth();
		
		if (depth < MAX_TREE_DEPTH) {
			TablutBoardState bs = node.getBoardState();
			List<TablutMove> options = bs.getAllLegalMoves();
			Collections.shuffle(options);
			int childCount = 0;
			for (TablutMove move : options) {
				// avoid redundant moves
				// only choose 50 children randomly in deeper levels to limit computation time
				if (isNotRedundantMove(node, move) && (depth < 2 || childCount < MAX_CHILDREN_TO_EXPAND)) {
			    	childCount++;
					TablutBoardState cloneBS = (TablutBoardState) bs.clone();

					Node child = new Node(cloneBS, move);
					node.addChild(child);
				}
			}
		}
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
		
		// the getRandomMove() extremely rarely throws an error for me, so I try/catch so that
		// random move isn't generated just because a rollout fails...
		try {
			Move randomMove = cloneBS.getRandomMove();
			
			// keep simulating until cap of turns reached or no winner has been found
			while (cloneBS.getWinner() == Board.NOBODY && (cloneBS.getTurnNumber()-initialTurnNumber) < MAX_SIMULATION_TURNS) {
				randomMove = cloneBS.getRandomMove();
				cloneBS.processMove((TablutMove) randomMove);
			}
		} catch(IllegalArgumentException e) {
			// random move generation failed!
			// do nothing and chalk it up to a draw
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
	private static boolean isNotRedundantMove(Node node, TablutMove move) {
		return node.isRoot() || node.getPreviousMove().getStartPosition().distance(move.getEndPosition()) != 0;
	}
}
