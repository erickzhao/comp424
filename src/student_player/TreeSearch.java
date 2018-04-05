package student_player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import boardgame.Move;
import tablut.TablutBoardState;
import tablut.TablutMove;
import tablut.TablutPlayer;

public class TreeSearch {
    public static final int SWEDE = 1;
    public static final int MUSCOVITE = 0;
	
	public Move searchForMove(TablutBoardState boardState) {
        Move myMove = boardState.getRandomMove();
        
        int opponent_id = boardState.getOpponent();
        int player_id = (opponent_id == SWEDE) ? MUSCOVITE : SWEDE;
        
        List<TablutMove> options = boardState.getAllLegalMoves();
        List<TablutBoardState> states = new ArrayList<TablutBoardState>();
        
        Node tree = new Node();
        
        // process all legal moves to generate search tree
        for (TablutMove move : options) {
        	TablutBoardState cloneBS = (TablutBoardState) boardState.clone();
            cloneBS.processMove(move);
            // IF MOVE WINS, TAKE IT
            if (cloneBS.getWinner() == player_id) {
            	return move;
            }
            
            if (!(cloneBS.getWinner() == opponent_id)) {
                states.add(cloneBS);
            }
        }
		
		return myMove;
	}
	

}
