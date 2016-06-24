package AI;

import lenz.htw.yakip.ColorChange;
import lenz.htw.yakip.net.NetworkClient;

public class Client implements Runnable {
	
	private int DIRNUM = 8;
	private int[][] DIRS = {
		{0, 1}, 
		{1, 1}, 
		{1, 0},
		{1, -1},
		{0, -1},
		{-1, -1},
		{-1, 0},
		{-1, 1}
	};
	
	private String teamName;
	private int playerNum;
	
	private String[] ais;
	
	private int[] scores = new int[4];
	private int[][]board = new int[31][31];
	private boolean[][][][] adjacency = new boolean[31][31][31][31];
	private float[][][] tokens = new float[4][3][2];
	private float[][] directions = new float[3][2];
	private NetworkClient network;
	
	public Client(String teamName, String[] ais) {
		super();
		this.teamName = teamName;
		this.ais = ais;
	}

	@Override
	public void run() {
		network = new NetworkClient(null, teamName);
		playerNum = network.getMyPlayerNumber();
        initWalls();
        initAdjacency();
        checkAllTokens();
        colorChange();
        initTokenAI();

		while (network.isAlive()) { 	        
	        checkAllTokens();
	        colorChange();
	        for (int i = 0; i < 3; i++) {
	        	network.setMoveDirection(i, directions[i][0], directions[i][1]);
	        }
		}
	} 
	
	private void initTokenAI() {
		TokenAI one = TokenAI.getTokenAI(this.ais[0], playerNum, 0, board, tokens, directions, scores, adjacency);
		TokenAI two = TokenAI.getTokenAI(this.ais[1], playerNum, 1, board, tokens, directions, scores, adjacency);
		TokenAI three =  TokenAI.getTokenAI(this.ais[2], playerNum, 2, board, tokens, directions, scores, adjacency);
		Thread t_one = new Thread(one);
		Thread t_two = new Thread(two);
		Thread t_three = new Thread(three);
		t_one.start();
		t_two.start();
		t_three.start();
	}

	private void colorChange() {
		ColorChange cc;
		int oldColor;
        while ((cc = network.getNextColorChange()) != null) {
        	oldColor = this.board[cc.x][cc.y];
        	if (oldColor >= 0 && oldColor <= 3) {
        		this.scores[oldColor]--;
        	}
        	this.scores[cc.newColor]++;
            this.board[cc.x][cc.y] = cc.newColor;
        }
	}
	
	private void initWalls() {
        for (int x = 0; x < 31; ++x) {
        	for (int y = 0; y < 31; ++y) {
        		this.board[x][y] = this.network.isWall(x, y) ? Constants.WALL : Constants.EMPTY;
        	}
        }
	}
	
	private void initAdjacency() {		
		int xTo, yTo;
		boolean possibleMove;
			
        for (int x = 0; x < 31; ++x) {
        	for (int y = 0; y < 31; ++y) {
        		this.adjacency[x][y][x][y] = true;
        		
        		for (int i = 0; i < this.DIRNUM; i++) {
        			xTo = x + this.DIRS[i][0];
        			yTo = y + this.DIRS[i][1];
 
        			if (xTo < 0 || xTo > 30 || yTo < 0 || yTo > 30) {
        				continue;
        			}
        			
        			possibleMove = this.board[xTo][yTo] != Constants.WALL;
        			possibleMove = this.board[x][yTo] == Constants.WALL || this.board[xTo][y] == Constants.WALL ? false : possibleMove;
        			
        			this.adjacency[x][y][xTo][yTo] = possibleMove;
        		}	
        	}
        }
	}
	
	private void checkAllTokens() {
		for (int i = 0; i < 4; ++i) {
			for (int j = 0; j < 3; ++j) {
				this.tokens[i][j][0] = this.network.getX(i, j);
				this.tokens[i][j][1] = this.network.getY(i, j);
			}
		}
	}

}
