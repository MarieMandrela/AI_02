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
	volatile int[][] board = new int[31][31];
	volatile float[][][] tokens = new float[4][3][2];
	volatile float[][][] directions = new float[4][3][2];
	
	Random rnd;
	
	public LookaheadTokenAI(int playerNum, int tokenNum, int[][] board, float[][][] tokens, float[][][] directions) {
		super();
		this.playerNum = playerNum;
		this.tokenNum = tokenNum;
		this.board = board;
		this.tokens = tokens;
		this.directions = directions;
		this.rnd = new Random();
		this.rnd.setSeed(playerNum + tokenNum);
	}

	@Override
	public void run() {
		int[] xy = new int[2];
		
		while (true) {		
			try {
				oneStepLookahead(xy);
				directions[this.playerNum][this.tokenNum][0] = xy[0];
				directions[this.playerNum][this.tokenNum][1] = xy[1];
            	if (this.tokenNum == 1) {
            		Thread.sleep(400);
            	}
				if (this.tokenNum == 2) {
            		Thread.sleep(800);
            	}
            	if (this.tokenNum == 0) {
            		Thread.sleep(200);
	            	directions[this.playerNum][this.tokenNum][0] = 0;
					directions[this.playerNum][this.tokenNum][1] = 0;
	            	Thread.sleep(100);
            	}
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    		}
		}
	}
	
	private void oneStepLookahead(int[] xy) {
		int best_score = Integer.MIN_VALUE;
		int score;
		
		for (int i = 0; i < this.DIRNUM; i++) {
			if (movePossible(getX(), getY(), this.dirs[i][0], this.dirs[i][1])) {
				score = evaluateField(getX() + this.dirs[i][0],  getY() + this.dirs[i][1]);
				if (score > best_score ||
					score == best_score && rnd.nextBoolean()) {
					best_score = score;
					xy[0] = this.dirs[i][0];
					xy[1] = this.dirs[i][1];
				}
			}
		}
	}
	
	private boolean movePossible(float x, float y, float dirX, float dirY) {
		if (moveDiagonal(dirX, dirY)) {
			moveThroughWalls(x, y, dirX, dirY);
		}
		return true;
	}
	
	private boolean moveThroughWalls(float x, float y, float dirX, float dirY) {
		return getBoard(0, y + dirY) == Constants.WALL || getBoard(x + dirX, 0) == Constants.WALL;
	}
	
	private boolean moveDiagonal(float dirX, float dirY) {
		return dirX != 0 && dirY != 0;
	}
	
	private int getBoard(float x, float y) {
		return this.board[(int)x][(int)y];
	}
	
	private float getX() {
		return this.tokens[this.playerNum][this.tokenNum][0];
	}
	
	private float getY() {
		return this.tokens[this.playerNum][this.tokenNum][1];
	}
	
	/**
	 * Evaluate how good its is to step on the given field.
	 * Prefer painting over enemy color > empty > my color > wall
	 */
	private int evaluateField(float x, float y) {
		int val = getBoard(x, y);
		if (val == Constants.WALL) {
			return 0;
		}
		if (val == this.playerNum) {
			return 1;
		}
		if (val == Constants.EMPTY) {
			return 2;
		}
		return 3;
	}
}
