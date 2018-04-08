package student_player;

import coordinates.Coord;
import tablut.TablutBoardState;
import tablut.TablutBoardState.Piece;
import tablut.TablutMove;

public class MyTools {
	
	private static final double SCALING_CONSTANT = Math.sqrt(2);
	
    public static double getSomething() {
        return Math.random();
    }
    
    public static int getMoveLength(TablutMove m) {
    	return m.getStartPosition().distance(m.getEndPosition());
    }
    

    public static double getUCTScore(Node node) {
		double winScore = node.getWinScore();
		int visits = node.getVisitCount();
		
		// explore if node is unvisited
		if (visits == 0 || node.isRoot()) {
			return Integer.MAX_VALUE;
		}
		
		int parentVisits = node.getParent().getVisitCount();
		
		return winScore / visits + SCALING_CONSTANT * Math.sqrt(Math.log(parentVisits)/visits);
	}
    
    /**
     * Gets status of adjacent pieces
     * Out of bounds = Piece.EMPTY
     * @param state		current board state
     * @param c			desired coord
     * @return			array of pieces
     */
    public static Piece[] getAdjacentPieces(TablutBoardState state, Coord c) {
    	Piece[] arr = new Piece[4];
    	arr[0] = (c.x < 8) ? state.getPieceAt(c.x+1,c.y) : Piece.EMPTY;
    	arr[1] = (c.x > 0) ? state.getPieceAt(c.x-1,c.y) : Piece.EMPTY;
    	arr[2] = (c.y < 8) ? state.getPieceAt(c.x,c.y+1) : Piece.EMPTY;
    	arr[3] = (c.y > 0) ? state.getPieceAt(c.x,c.y-1) : Piece.EMPTY;
    	
    	return arr;
    }
    
    /**
     * Checks if a state is safe for the king
     * Currently, the heuristic is that an unsafe state is when it has a Muscovite next to it,
     * and with the opposing adjacent tile being empty (ripe for the taking)
     * @param state		current board state
     * @return			if the state is safe
     */
    public static boolean isStateSafeForKing(TablutBoardState state) {
    	Coord kingPos = state.getKingPosition();
    	Piece[] adj = getAdjacentPieces(state, kingPos);
    	
    	return !(adj[0] == Piece.EMPTY && adj[1] == Piece.BLACK || adj[1] == Piece.EMPTY && adj[0] == Piece.BLACK
    			|| adj[2] == Piece.EMPTY && adj[3] == Piece.BLACK || adj[3] == Piece.EMPTY && adj[2] == Piece.BLACK);
    }
}
