package AI;

import java.util.Random;

public class TokenAI implements Runnable {

	private int playerNum;
	private int tokenNum;
	volatile int[][] board = new int[31][31];
	volatile float[][][] tokens = new float[4][3][2];
	volatile float[][][] directions = new float[4][3][2];
	
	public TokenAI(int playerNum, int tokenNum, int[][] board, float[][][] tokens, float[][][] directions) {
		super();
		this.playerNum = playerNum;
		this.tokenNum = tokenNum;
		this.board = board;
		this.tokens = tokens;
		this.directions = directions;
	}

	@Override
	public void run() {
		Random rnd = new Random();
		rnd.setSeed(playerNum + tokenNum);
		
		while (true) {		
			try {
				directions[this.playerNum][this.tokenNum][0] = rnd.nextFloat() - 0.5f;
				directions[this.playerNum][this.tokenNum][1] = rnd.nextFloat() - 0.5f;
            	Thread.sleep(500);
            	if (this.tokenNum == 0) {
	            	directions[this.playerNum][this.tokenNum][0] = 0;
					directions[this.playerNum][this.tokenNum][1] = 0;
	            	Thread.sleep(500);
            	}
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    		}
		}
		
	}

}
