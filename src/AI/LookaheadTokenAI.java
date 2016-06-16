package AI;

import java.util.Random;

public class LookaheadTokenAI extends TokenAI {

	private int DIRNUM = 8;
	
	private int[][] dirs = {
		{0, 1}, 
		{1, 1}, 
		{1, 0},
		{1, -1},
		{0, -1},
		{-1, -1},
		{-1, 0},
		{-1, 1}
	};
	
	private int playerNum;
	private int tokenNum;
	volatile private int[] scores = new int[4];
	volatile int[][] board = new int[31][31];
	volatile float[][][] tokens = new float[4][3][2];
	volatile float[][] directions = new float[3][2];
	
	Random rnd;
	
	public LookaheadTokenAI(int playerNum, int tokenNum, int[][] board, float[][][] tokens, float[][] directions, int[] scores) {
		super();
		this.playerNum = playerNum;
		this.tokenNum = tokenNum;
		this.board = board;
		this.tokens = tokens;
		this.directions = directions;
		this.scores = scores;
		this.rnd = new Random();
		this.rnd.setSeed(playerNum + tokenNum);
	}

	@Override
	public void run() {
		float[] xy = new float[2];
		
		while (true) {		
			try {
				oneStepLookahead(xy);
				directions[this.tokenNum][0] = xy[0];
				directions[this.tokenNum][1] = xy[1];
            	if (this.tokenNum == 1) {
            		Thread.sleep(400);
            	}
				if (this.tokenNum == 2) {
            		Thread.sleep(800);
            	}
            	if (this.tokenNum == 0) {
            		Thread.sleep(150);
	            	directions[this.tokenNum][0] = 0;
					directions[this.tokenNum][1] = 0;
	            	Thread.sleep(100);
            	}
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    		}
		}
	}
	
	private void oneStepLookahead(float[] xy) {
		int best_score = Integer.MIN_VALUE;
		int score;
		float xTo, yTo;
		
		for (int i = 0; i < this.DIRNUM; i++) {
			if (movePossible(getX(), getY(), this.dirs[i][0], this.dirs[i][1])) {
				xTo = getX() + this.dirs[i][0];
				yTo = getY() + this.dirs[i][1];
				score = evaluateField(xTo, yTo);
				if (score > best_score ||
					score == best_score && rnd.nextBoolean()) {
					best_score = score;
					getDir(xy, getX(), getY(), xTo, yTo);
				}
			}
		}
	}
	
	private boolean movePossible(float x, float y, float dirX, float dirY) {
		if (moveDiagonal(dirX, dirY)) {
			return !moveThroughWalls(x, y, dirX, dirY);
		}
		return true;
	}
	
	private boolean moveThroughWalls(float x, float y, float dirX, float dirY) {
		return getBoard(x + 0, y + dirY) == Constants.WALL || getBoard(x + dirX, y + 0) == Constants.WALL;
	}
	
	private boolean moveDiagonal(float dirX, float dirY) {
		return dirX != 0 && dirY != 0;
	}
	
	private int getBoard(float x, float y) {
		return this.board[limit(x)][limit(y)];
	}
	
	private int limit(float value) {
	    return (int)Math.max(0, Math.min(value, 31));
	}
	
	private float getX() {
		return this.tokens[this.playerNum][this.tokenNum][0];
	}
	
	private float getY() {
		return this.tokens[this.playerNum][this.tokenNum][1];
	}
	
	private void getDir(float[] xy, float xFrom, float yFrom, float xTo, float yTo) {
		xy[0] = xTo - xFrom;
		xy[1] = yTo - yFrom;
	}
	
	private boolean fieldOccupied(float x, float y) {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 3; j++) {
				if (i == this.playerNum && j == this.tokenNum) {
					continue;
				}
				
				if ((int)this.tokens[i][j][0] == (int)x  &&
				    (int)this.tokens[i][j][1] == (int)y) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Evaluate how good its is to step on the given field.
	 * Prefer painting over enemy color > empty > my color > wall
	 * Try to avoid running into other tokens
	 */
	private int evaluateField(float x, float y) {
		int field = getBoard(x, y);
		
		if (field == Constants.WALL) {
			return 0;
		}
		if (field == this.playerNum) {
			return 1;
		}
		if (fieldOccupied(x, y)) {
			return 2;
		}
		if (field == Constants.EMPTY) {
			return 3;
		}
		if (field >= 0 && field <= 3) {
			return this.scores[field];
		}
		
		return 0;
	}
}
