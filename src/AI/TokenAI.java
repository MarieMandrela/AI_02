package AI;

public abstract class TokenAI implements Runnable{
	
	public static TokenAI getTokenAI(String type, int playerNum, int tokenNum, int[][] board, float[][][] tokens, float[][] directions, int[] scores) {
		if (type == null) {
			return null;
		}
		if (type.equalsIgnoreCase(Constants.RNG)) {
			return new RandomTokenAI(playerNum, tokenNum, board, tokens, directions, scores);
		}
		if (type.equalsIgnoreCase(Constants.LOOK)) {
			return new LookaheadTokenAI(playerNum, tokenNum, board, tokens, directions, scores);
		}
		
		return null;
	}
}
