package student_player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
}
