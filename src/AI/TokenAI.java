package AI;

public abstract class TokenAI implements Runnable{
	
	public static TokenAI getTokenAI(String type, int playerNum, int tokenNum, int[][] board, 
			float[][][] tokens, float[][] directions, int[] scores, boolean[][][][] adjacency) {
		if (type == null) {
			return null;
		}
		if (type.equalsIgnoreCase(Constants.RNG)) {
			return new RandomTokenAI(playerNum, tokenNum, board, tokens, directions);
		}
		if (type.equalsIgnoreCase(Constants.LOOK)) {
			return new LookaheadTokenAI(playerNum, tokenNum, board, tokens, directions, scores, adjacency);
		}
		if (type.equalsIgnoreCase(Constants.DIJK)) {
			return new DijkstraTokenAI(playerNum, tokenNum, board, tokens, directions, scores, adjacency);
		}
		
		return null;
	}
}
