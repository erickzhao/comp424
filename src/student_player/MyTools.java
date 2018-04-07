package student_player;

import tablut.TablutMove;

public class MyTools {
    public static double getSomething() {
        return Math.random();
    }
    
    public static int getMoveLength(TablutMove m) {
    	return m.getStartPosition().distance(m.getEndPosition());
    }
}
